#!/usr/bin/env bash
# =============================================================================
# AM Portões — deploy em EC2 Ubuntu (22.04/24.04). Idempotente: pode reexecutar.
#   git clone https://github.com/HSbrunihn/Am-Portoes-System.git && cd Am-Portoes-System
#   sudo ./deploy-ec2.sh
# Opcional (antes do sudo -E ou na mesma linha):
#   ADMIN_EMAIL / ADMIN_PASSWORD   admin inicial (senão, gera uma senha)
#   DB_URL / DB_USERNAME / DB_PASSWORD   usar banco já provisionado (ex.: RDS) em vez do local
#   SKIP_UPGRADE=1  pula o apt upgrade (deploy ao vivo mais rápido)
#   RUN_TESTS=1     roda ./mvnw verify (testes + cobertura) antes de publicar
# Arquitetura: Nginx :80 (Angular + proxy /api) -> Spring Boot 127.0.0.1:8080 (systemd)
#              -> PostgreSQL só em localhost (5432 nunca exposto)
# =============================================================================
set -euo pipefail
[[ $EUID -eq 0 ]] || { echo "Execute com sudo: sudo ./deploy-ec2.sh"; exit 1; }

REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP=amportoes; APP_DIR=/opt/$APP; WEB_DIR=/var/www/$APP
ENV_DIR=/etc/$APP; ENV_FILE=$ENV_DIR/$APP.env
export DEBIAN_FRONTEND=noninteractive NEEDRESTART_MODE=a
log()  { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }
warn() { printf '\033[1;33m[aviso] %s\033[0m\n' "$*"; }
val()  { grep -m1 "^$1=" "$ENV_FILE" 2>/dev/null | cut -d= -f2- || true; }   # valor já salvo
pg()   { (cd /tmp && sudo -u postgres "$@"); }
as_user() { if [[ -n "${SUDO_USER:-}" && "$SUDO_USER" != root ]]; then sudo -u "$SUDO_USER" -H "$@"; else "$@"; fi; }

log "1/9 Atualizando pacotes"
apt-get update -y
[[ "${SKIP_UPGRADE:-0}" == 1 ]] || apt-get -y -o Dpkg::Options::=--force-confold upgrade

log "2/9 Instalando Java 21, Nginx, PostgreSQL e Node 24"
apt-get install -y openjdk-21-jdk-headless nginx postgresql curl ca-certificates openssl rsync
if ! command -v node >/dev/null || (( $(node -p 'process.versions.node.split(".")[0]') < 24 )); then
  curl -fsSL https://deb.nodesource.com/setup_24.x | bash -
  apt-get install -y nodejs
fi
# Instâncias de 1 GB (t2/t3.micro) não compilam Angular + Maven sem swap
if [[ -z "$(swapon --show)" ]] && (( $(awk '/MemTotal/{print $2}' /proc/meminfo) < 2000000 )); then
  fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
  grep -q '^/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

log "3/9 Banco de dados"
DB_URL=${DB_URL:-$(val DB_URL)}; DB_USERNAME=${DB_USERNAME:-$(val DB_USERNAME)}; DB_PASSWORD=${DB_PASSWORD:-$(val DB_PASSWORD)}
if [[ -z "$DB_URL" || "$DB_URL" == *"//localhost:5432/"* ]]; then
  DB_URL=jdbc:postgresql://localhost:5432/am_portoes; DB_USERNAME=${DB_USERNAME:-amportoes}
  DB_PASSWORD=${DB_PASSWORD:-$(openssl rand -hex 24)}
  systemctl enable --now postgresql
  pg psql -qc "ALTER SYSTEM SET listen_addresses = 'localhost'"   # nunca escuta fora
  systemctl restart postgresql
  pg psql -tAc "SELECT 1 FROM pg_roles WHERE rolname='$DB_USERNAME'" | grep -q 1 \
    || pg psql -qc "CREATE ROLE $DB_USERNAME LOGIN"
  pg psql -qc "ALTER ROLE $DB_USERNAME PASSWORD '$DB_PASSWORD'"
  pg psql -tAc "SELECT 1 FROM pg_database WHERE datname='am_portoes'" | grep -q 1 \
    || pg createdb -O "$DB_USERNAME" am_portoes
  echo "PostgreSQL local pronto (dados preservados entre execuções; Flyway cria as tabelas)."
else
  echo "Usando banco externo: $DB_URL"
fi

log "4/9 Variáveis de ambiente ($ENV_FILE)"
TOKEN=$(curl -s -m 2 -X PUT http://169.254.169.254/latest/api/token -H 'X-aws-ec2-metadata-token-ttl-seconds: 60' || true)
PUBLIC_IP=$(curl -s -m 2 -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/public-ipv4 || true)
[[ "$PUBLIC_IP" =~ ^[0-9.]+$ ]] || PUBLIC_IP=$(hostname -I | awk '{print $1}')
JWT_SECRET=$(val JWT_SECRET); JWT_SECRET=${JWT_SECRET:-$(openssl rand -base64 48 | tr -d '\n')}
ADMIN_EMAIL=${ADMIN_EMAIL:-$(val ADMIN_EMAIL)}; ADMIN_EMAIL=${ADMIN_EMAIL:-admin@amportoes.com.br}
ADMIN_PASSWORD=${ADMIN_PASSWORD:-$(val ADMIN_PASSWORD)}; ADMIN_PASSWORD=${ADMIN_PASSWORD:-$(openssl rand -hex 8)Aa1}
install -d -m 750 "$ENV_DIR"
umask 077
cat > "$ENV_FILE" <<EOF
SERVER_PORT=8080
SERVER_ADDRESS=127.0.0.1
DB_URL=$DB_URL
DB_USERNAME=$DB_USERNAME
DB_PASSWORD=$DB_PASSWORD
JWT_SECRET=$JWT_SECRET
JWT_EXPIRATION_MINUTES=60
CORS_ALLOWED_ORIGINS=http://$PUBLIC_IP
ADMIN_EMAIL=$ADMIN_EMAIL
ADMIN_PASSWORD=$ADMIN_PASSWORD
SHOW_SQL=false
EOF
umask 022
chmod 600 "$ENV_FILE"   # segredos só para root (o systemd lê como root)

log "5/9 Build do Angular"
as_user bash -c "cd '$REPO/frontend' && npm ci --no-audit --no-fund && npx ng build --configuration production"
install -d "$WEB_DIR"
rsync -a --delete "$REPO/frontend/dist/am-portoes-frontend/browser/" "$WEB_DIR/"

log "6/9 Build do Spring Boot"
GOAL="package -DskipTests"; [[ "${RUN_TESTS:-0}" == 1 ]] && GOAL=verify
as_user bash -c "cd '$REPO/backend' && bash ./mvnw -B -q clean $GOAL"
id -u $APP >/dev/null 2>&1 || useradd --system --no-create-home --shell /usr/sbin/nologin $APP
install -d "$APP_DIR"
install -m 644 "$(find "$REPO/backend/target" -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' | head -1)" "$APP_DIR/app.jar"

log "7/9 Serviço systemd"
cat > /etc/systemd/system/$APP.service <<EOF
[Unit]
Description=AM Portões API (Spring Boot)
After=network-online.target postgresql.service
Wants=network-online.target

[Service]
User=$APP
EnvironmentFile=$ENV_FILE
ExecStart=/usr/bin/java ${JAVA_OPTS:--Xms128m -Xmx512m} -jar $APP_DIR/app.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=5
NoNewPrivileges=true
ProtectSystem=full
PrivateTmp=true

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable $APP >/dev/null
systemctl restart $APP

log "8/9 Nginx (porta 80, fallback SPA e proxy /api)"
cat > /etc/nginx/sites-available/$APP <<'EOF'
server {
    listen 80 default_server;
    server_name _;
    root /var/www/amportoes;
    index index.html;
    gzip_types text/css application/javascript application/json;

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        # Front e API na MESMA origem: sem Origin o Spring não trata como CORS (evita 403 "Invalid CORS request")
        proxy_set_header Origin "";
    }

    location / {
        try_files $uri $uri/ /index.html;   # rotas do Angular (/produtos, /movimentacoes...) ao recarregar
    }
}
EOF
ln -sf /etc/nginx/sites-available/$APP /etc/nginx/sites-enabled/$APP
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl enable nginx >/dev/null
systemctl reload nginx || systemctl restart nginx

log "9/9 Verificando saúde"
code() { curl -s -o /dev/null -w '%{http_code}' "$@" || true; }
for _ in $(seq 1 60); do [[ $(code http://127.0.0.1:8080/api/auth/me) == 401 ]] && break; sleep 3; done
ok=1
check() { if [[ "$2" == "$3" ]]; then echo "  OK   $1"; else echo "  FALHA $1 (esperado $3, veio $2)"; ok=0; fi; }
check "Spring Boot responde (sem token = 401)" "$(code http://127.0.0.1:8080/api/auth/me)" 401
check "Nginx serve o Angular"                  "$(code http://127.0.0.1/)" 200
check "Fallback SPA (/movimentacoes)"          "$(code http://127.0.0.1/movimentacoes)" 200
check "Proxy /api pelo Nginx"                  "$(code http://127.0.0.1/api/auth/me)" 401
if [[ "$DB_URL" == *"//localhost:5432/"* ]]; then
  check "Flyway aplicou as migrations" "$(pg psql -d am_portoes -tAc 'SELECT count(*) > 0 FROM flyway_schema_history WHERE success' 2>/dev/null)" t
  if ss -ltnH '( sport = :5432 )' | awk '{print $4}' | grep -qvE '^(127\.0\.0\.1|\[::1\]):'; then
    echo "  FALHA PostgreSQL escutando fora do localhost"; ok=0
  else echo "  OK   PostgreSQL só em localhost"; fi
fi
LOGIN=$(code -X POST -H 'Content-Type: application/json' \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"senha\":\"$ADMIN_PASSWORD\"}" http://127.0.0.1/api/auth/login)
if [[ "$LOGIN" == 200 ]]; then echo "  OK   Login do admin (banco + JWT)"
else warn "login do admin retornou $LOGIN (o admin só é criado se não existir; a senha pode ter sido trocada pela tela)"; fi

if (( ok )); then
  log "Pronto: http://$PUBLIC_IP  (admin: $ADMIN_EMAIL | senha: sudo grep ADMIN_PASSWORD $ENV_FILE)"
else
  journalctl -u $APP -n 40 --no-pager
  echo "Deploy com falhas. Logs: journalctl -u $APP -f | tail -f /var/log/nginx/error.log"; exit 1
fi
