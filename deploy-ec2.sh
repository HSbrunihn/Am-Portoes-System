#!/usr/bin/env bash
# =============================================================================
# AM Portões — deploy em EC2 Ubuntu (22.04/24.04). Idempotente: pode reexecutar.
#   git clone https://github.com/HSbrunihn/Am-Portoes-System.git && cd Am-Portoes-System
#   sudo ./deploy-ec2.sh
# Opcional (na mesma linha, depois do sudo):
#   ADMIN_EMAIL / ADMIN_PASSWORD   admin inicial (senão, gera uma senha)
#   DB_URL / DB_USERNAME / DB_PASSWORD   usar banco já provisionado (ex.: RDS) em vez do local
#   SKIP_UPGRADE=1  pula o apt upgrade (deploy ao vivo mais rápido)
#   RUN_TESTS=1     roda ./mvnw verify (testes + cobertura) antes de publicar
# Arquitetura: Nginx :80 (Angular + proxy /api) -> Spring Boot 127.0.0.1:8080 (systemd)
#              -> PostgreSQL só em localhost (5432 nunca exposto)
# Saída: um card por etapa; o detalhe de apt/npm/maven vai para $LOG.
# =============================================================================
set -Eeuo pipefail
[[ $EUID -eq 0 ]] || { echo "Execute com sudo: sudo ./deploy-ec2.sh"; exit 1; }

REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP=amportoes; APP_DIR=/opt/$APP; WEB_DIR=/var/www/$APP
ENV_DIR=/etc/$APP; ENV_FILE=$ENV_DIR/$APP.env
LOG=/var/log/$APP-deploy.log
TOTAL=9
export DEBIAN_FRONTEND=noninteractive NEEDRESTART_MODE=a
export LC_CTYPE=C.UTF-8   # símbolos dos cards (✔ ┌ │) mesmo quando o sudo limpa o locale

# ---------------------------------------------------------------- interface --
# >>> ui
exec 3>&1   # fd 3 = tela: o card de erro aparece mesmo com a etapa redirecionada para o log
if [[ -t 1 ]]; then
  B=$'\e[1m'; D=$'\e[2m'; R=$'\e[0m'
  GRN=$'\e[32m'; RED=$'\e[31m'; YLW=$'\e[33m'; BLU=$'\e[34m'; CYN=$'\e[36m'
else
  B=; D=; R=; GRN=; RED=; YLW=; BLU=; CYN=
fi
SPIN_PID=; STEP_N=0; STEP_LABEL=; STEP_T0=0

dur() { local s=$1; (( s >= 60 )) && printf '%dm%02ds' $((s / 60)) $((s % 60)) || printf '%ds' "$s"; }
pad() { local s=$1 w=$2; printf '%s%*s' "$s" $(( w > ${#s} ? w - ${#s} : 0 )) ''; }   # conta caracteres, não bytes

card() {   # card "titulo" "linha" "linha"...
  printf '\n %s┌%s %s%s%s\n' "$BLU" "$R" "$B" "$1" "$R"; shift
  local l; for l in "$@"; do printf ' %s│%s %s\n' "$BLU" "$R" "$l"; done
  printf ' %s└──────────────────────────────────────────%s\n' "$BLU" "$R"
}

spin_start() {
  [[ -t 1 ]] || return 0
  ( trap - ERR EXIT
    local f=(⠋ ⠙ ⠹ ⠸ ⠼ ⠴ ⠦ ⠧ ⠇ ⠏) i=0
    while :; do
      printf '\r %s%s%s %s[%d/%d]%s %s %s%s%s\e[K' "$CYN" "${f[i++ % 10]}" "$R" "$D" "$STEP_N" "$TOTAL" "$R" \
        "$STEP_LABEL" "$D" "$(dur $((SECONDS - STEP_T0)))" "$R"
      sleep 0.1
    done ) &
  SPIN_PID=$!
}

spin_stop() {
  [[ -n "$SPIN_PID" ]] || return 0
  kill "$SPIN_PID" 2>/dev/null || true; wait "$SPIN_PID" 2>/dev/null || true
  SPIN_PID=; printf '\r\e[K' >&3
}

step() {   # step "rótulo" função
  STEP_N=$((STEP_N + 1)); STEP_LABEL=$1; STEP_T0=$SECONDS; shift
  printf '\n── [%d/%d] %s\n' "$STEP_N" "$TOTAL" "$STEP_LABEL" >> "$LOG"
  spin_start
  "$@" >> "$LOG" 2>&1
  spin_stop
  printf ' %s✔%s %s[%d/%d]%s %s %s%s%s\n' "$GRN" "$R" "$D" "$STEP_N" "$TOTAL" "$R" \
    "$(pad "$STEP_LABEL" 38)" "$D" "$(dur $((SECONDS - STEP_T0)))" "$R"
}

fail() {   # qualquer comando que falhar cai aqui (trap ERR)
  local rc=$? erro=()
  trap - ERR; spin_stop
  # últimas linhas não vazias da etapa que falhou (depois do último marcador "── [n/9]")
  mapfile -t erro < <(awk '/^── \[/ {n=0; delete l; next} NF {l[++n]=$0} END {for (i = (n > 10 ? n - 9 : 1); i <= n; i++) print l[i]}' "$LOG" 2>/dev/null || true)
  {
    printf ' %s✖%s %s[%d/%d]%s %s\n' "$RED" "$R" "$D" "$STEP_N" "$TOTAL" "$R" "${STEP_LABEL:-início}"
    card "${RED}Falhou${R}  ${D}código $rc${R}" "${erro[@]/#/${D}}" \
      "" "log completo  ${CYN}sudo less $LOG${R}" "reexecutar    ${CYN}sudo ./deploy-ec2.sh${R}"
  } >&3
  exit "$rc"
}
trap fail ERR
trap 'spin_stop' EXIT

check() {  # check "rótulo" obtido esperado
  if [[ "$2" == "$3" ]]; then printf '   %s✔%s %s\n' "$GRN" "$R" "$1"
  else printf '   %s✖%s %s %s(esperado %s, veio %s)%s\n' "$RED" "$R" "$1" "$D" "$3" "${2:-nada}" "$R"; ok=0; fi
}
# <<< ui

# ------------------------------------------------------------------ helpers --
val()  { grep -m1 "^$1=" "$ENV_FILE" 2>/dev/null | cut -d= -f2- || true; }   # valor já salvo
pg()   { (cd /tmp && sudo -u postgres "$@"); }
as_user() { if [[ -n "${SUDO_USER:-}" && "$SUDO_USER" != root ]]; then sudo -u "$SUDO_USER" -H "$@"; else "$@"; fi; }
code() { curl -s -o /dev/null -w '%{http_code}' "$@" || true; }

# ------------------------------------------------------------------- etapas --
pacotes() {
  apt-get update -y
  [[ "${SKIP_UPGRADE:-0}" == 1 ]] || apt-get -y -o Dpkg::Options::=--force-confold upgrade
}

dependencias() {
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
}

banco() {
  DB_URL=${DB_URL:-$(val DB_URL)}; DB_USERNAME=${DB_USERNAME:-$(val DB_USERNAME)}; DB_PASSWORD=${DB_PASSWORD:-$(val DB_PASSWORD)}
  if [[ -z "$DB_URL" || "$DB_URL" == *"//localhost:5432/"* ]]; then
    DB_URL=jdbc:postgresql://localhost:5432/am_portoes; DB_USERNAME=${DB_USERNAME:-amportoes}
    DB_PASSWORD=${DB_PASSWORD:-$(openssl rand -hex 24)}
    systemctl enable --now postgresql
    pg psql -qc "ALTER SYSTEM SET listen_addresses = 'localhost'"   # nunca escuta fora
    systemctl restart postgresql
    [[ $(pg psql -tAc "SELECT 1 FROM pg_roles WHERE rolname='$DB_USERNAME'") == 1 ]] \
      || pg psql -qc "CREATE ROLE $DB_USERNAME LOGIN"
    pg psql -qc "ALTER ROLE $DB_USERNAME PASSWORD '$DB_PASSWORD'"
    [[ $(pg psql -tAc "SELECT 1 FROM pg_database WHERE datname='am_portoes'") == 1 ]] \
      || pg createdb -O "$DB_USERNAME" am_portoes
    echo "PostgreSQL local pronto (dados preservados entre execuções; Flyway cria as tabelas)."
  else
    echo "Usando banco externo: $DB_URL"
  fi
}

ambiente() {
  local token
  token=$(curl -s -m 2 -X PUT http://169.254.169.254/latest/api/token -H 'X-aws-ec2-metadata-token-ttl-seconds: 60' || true)
  PUBLIC_IP=$(curl -s -m 2 -H "X-aws-ec2-metadata-token: $token" http://169.254.169.254/latest/meta-data/public-ipv4 || true)
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
}

build_angular() {
  as_user bash -c "cd '$REPO/frontend' && npm ci --no-audit --no-fund && npx ng build --configuration production"
  install -d "$WEB_DIR"
  rsync -a --delete "$REPO/frontend/dist/am-portoes-frontend/browser/" "$WEB_DIR/"
}

build_spring() {
  local goal="package -DskipTests" jar
  [[ "${RUN_TESTS:-0}" == 1 ]] && goal=verify
  as_user bash -c "cd '$REPO/backend' && bash ./mvnw -B -q clean $goal"
  id -u $APP >/dev/null 2>&1 || useradd --system --no-create-home --shell /usr/sbin/nologin $APP
  install -d "$APP_DIR"
  jar=$(find "$REPO/backend/target" -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' -print -quit)
  install -m 644 "$jar" "$APP_DIR/app.jar"
}

servico() {
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
  systemctl enable $APP
  systemctl restart $APP
}

nginx_site() {
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
  systemctl enable nginx
  systemctl reload nginx || systemctl restart nginx
}

aguardar_api() {   # o Spring leva ~15-40 s para subir
  local _
  for _ in $(seq 1 60); do [[ $(code http://127.0.0.1:8080/api/auth/me) == 401 ]] && return 0; sleep 3; done
  return 0   # os checks abaixo mostram o que falhou
}

# --------------------------------------------------------------- execução --
install -m 600 /dev/null "$LOG"
card "AM PORTÕES · deploy na EC2" \
  "${D}Angular · Spring Boot · PostgreSQL · Nginx · systemd${R}" \
  "${D}detalhes em $LOG${R}"
echo

step "Pacotes do sistema$([[ "${SKIP_UPGRADE:-0}" == 1 ]] && echo ' (sem upgrade)')" pacotes
step "Java 21 · Nginx · PostgreSQL · Node 24" dependencias
step "Banco de dados"                         banco
step "Segredos e variáveis"                   ambiente
step "Build do Angular"                       build_angular
step "Build do Spring Boot"                   build_spring
step "Serviço systemd"                        servico
step "Nginx · SPA + proxy /api"               nginx_site
step "Subindo a API"                          aguardar_api
trap - ERR

ok=1
printf '\n %sSaúde%s\n' "$B" "$R"
check "API responde (sem token = 401)"   "$(code http://127.0.0.1:8080/api/auth/me)" 401
check "Nginx serve o Angular"            "$(code http://127.0.0.1/)" 200
check "Rotas do Angular ao recarregar"   "$(code http://127.0.0.1/movimentacoes)" 200
check "Proxy /api pelo Nginx"            "$(code http://127.0.0.1/api/auth/me)" 401
if [[ "$DB_URL" == *"//localhost:5432/"* ]]; then
  check "Flyway aplicou as migrations" \
    "$(pg psql -d am_portoes -tAc 'SELECT count(*) > 0 FROM flyway_schema_history WHERE success' 2>/dev/null || true)" t
  if ss -ltnH '( sport = :5432 )' | awk '{print $4}' | grep -qvE '^(127\.0\.0\.1|\[::1\]):'; then
    check "PostgreSQL só em localhost" "exposto" "localhost"
  else
    check "PostgreSQL só em localhost" "localhost" "localhost"
  fi
fi
LOGIN=$(code -X POST -H 'Content-Type: application/json' \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"senha\":\"$ADMIN_PASSWORD\"}" http://127.0.0.1/api/auth/login)
if [[ "$LOGIN" == 200 ]]; then printf '   %s✔%s %s\n' "$GRN" "$R" "Login do admin (banco + JWT)"
else printf '   %s!%s %s %s(veio %s: o admin já existia com outra senha)%s\n' "$YLW" "$R" "Login do admin" "$D" "$LOGIN" "$R"; fi

if (( ok )); then
  card "${GRN}No ar${R}  ${D}em $(dur "$SECONDS")${R}" \
    "site    ${B}${CYN}http://$PUBLIC_IP${R}" \
    "admin   $ADMIN_EMAIL" \
    "senha   ${CYN}sudo grep ADMIN_PASSWORD $ENV_FILE${R}" \
    "logs    ${CYN}sudo journalctl -u $APP -f${R}"
else
  mapfile -t api_log < <(journalctl -u $APP -n 15 --no-pager 2>/dev/null || true)
  card "${RED}Deploy com falhas${R}" "${api_log[@]/#/${D}}" "" \
    "logs    ${CYN}sudo journalctl -u $APP -f${R}" \
    "nginx   ${CYN}sudo tail -f /var/log/nginx/error.log${R}"
  exit 1
fi
