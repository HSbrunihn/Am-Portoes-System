# AM Portões - System

Sistema de gestão para instalação e manutenção de portões: tarefas, produtos, **movimentações de estoque**, orçamentos e usuários.

| Camada | Tecnologia |
|---|---|
| Back-end | Java 17+ (deploy usa 21), Spring Boot 4.1, Spring Security + JWT, JPA, Flyway, PostgreSQL |
| Front-end | Angular 22 (Node `^22.22.3` ou `^24.15`) |
| Produção | AWS EC2 Ubuntu: Nginx :80 → Spring Boot 127.0.0.1:8080 (systemd) → PostgreSQL só em localhost |

```
backend/        API REST (src/main/resources/db/migration = migrations Flyway)
frontend/       Angular (o build de produção chama /api na mesma origem)
deploy-ec2.sh   Deploy idempotente na EC2
.env.example    Variáveis de ambiente do back-end
```

## Rodar localmente

```bash
# 1) PostgreSQL com um banco vazio "am_portoes" (o Flyway cria as tabelas)
# 2) Back-end na porta 8081 (o Spring não lê .env sozinho)
cp .env.example .env               # preencha DB_PASSWORD, JWT_SECRET (openssl rand -base64 48) e ADMIN_PASSWORD
set -a; source .env; set +a
cd backend && ./mvnw spring-boot:run

# 3) Front-end (proxy /api -> localhost:8081)
cd frontend && npm ci && npm start  # http://localhost:4200
```

Testes: `cd backend && ./mvnw verify` (H2 em memória + cobertura JaCoCo ≥ 90%) · `cd frontend && npx ng test --watch=false`.

## Deploy na AWS EC2

1. Instância **Ubuntu 22.04/24.04** (t3.small recomendado; em t2/t3.micro o script cria swap de 2 GB).
2. Security Group: entrada **22** (só seu IP) e **80** (0.0.0.0/0). **Não** abra 5432 nem 8080.
3. Na instância:

```bash
git clone https://github.com/HSbrunihn/Am-Portoes-System.git && cd Am-Portoes-System
sudo ./deploy-ec2.sh                  # ou: sudo SKIP_UPGRADE=1 ./deploy-ec2.sh (mais rápido ao vivo)
sudo grep ADMIN_PASSWORD /etc/amportoes/amportoes.env   # senha do admin gerada
```

O script atualiza o Ubuntu, instala Java/Nginx/PostgreSQL/Node, cria banco e usuário, gera os segredos em `/etc/amportoes/amportoes.env` (permissão 600), compila Angular e Spring Boot, cria o serviço `amportoes` no systemd, configura o Nginx com fallback para `index.html` e termina com um health check. **Reexecutar é seguro**: segredos e dados são preservados — para atualizar a versão, `git pull && sudo ./deploy-ec2.sh`.
Banco já provisionado (RDS): `sudo DB_URL=jdbc:postgresql://host:5432/am_portoes DB_USERNAME=... DB_PASSWORD=... ./deploy-ec2.sh`.

### Logs e operação

```bash
journalctl -u amportoes -f                 # logs do Spring Boot
sudo systemctl restart amportoes           # reiniciar a API
sudo tail -f /var/log/nginx/access.log /var/log/nginx/error.log
sudo -u postgres psql am_portoes           # console do banco
```

### Apresentação com duas contas AWS

- **Conta A (já online):** deploy feito e testado antes. É onde a demonstração das telas acontece — não depende do deploy ao vivo.
- **Conta B (ao vivo):** instância nova, mesmo Security Group, `git clone` + `sudo SKIP_UPGRADE=1 ./deploy-ec2.sh`. Leva ~5–10 min; enquanto roda, apresente na conta A e volte para mostrar o health check e abrir `http://IP-DA-CONTA-B`.
- Ensaie na conta B no dia anterior e encerre a instância depois (custo). As duas contas usam o **mesmo commit**.

## Validação rápida (roteiro)

```bash
API=http://IP-DA-EC2/api
TOKEN=$(curl -s -X POST $API/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"admin@amportoes.com.br","senha":"SENHA"}' | sed -E 's/.*"token" *: *"([^"]+)".*/\1/')
curl -s $API/auth/me -H "Authorization: Bearer $TOKEN"                     # 200: dados do admin
curl -s -o /dev/null -w '%{http_code}\n' $API/produtos                      # 401: sem token
curl -s -X POST $API/usuarios -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"nome":"Ana","email":"ana@teste.com","senha":"segredo1"}'            # 201 (repetir => 409 e-mail duplicado)
```

No navegador: cadastrar produto → **Movimentações** → entrada de 10, saída de 3 (o estoque do produto muda) → saída maior que o estoque (mensagem "Estoque insuficiente para saída"). No banco, a senha nunca aparece em texto puro:
`sudo -u postgres psql am_portoes -c "select email, left(senha, 7) from usuario"` → `$2a$10$` (hash BCrypt, irreversível).

## Segurança

- Segredos só em variáveis de ambiente (`.env` local, `/etc/amportoes/amportoes.env` na EC2); `.env` e `*.pem` estão no `.gitignore`.
- O repositório é público e o histórico contém a antiga senha fixa `Admin@123` e `spring.datasource.password=admin`. **Qualquer ambiente que já subiu com essas senhas deve trocá-las** (o admin existente não é alterado pelo `ADMIN_PASSWORD`; troque pela tela de Usuários). Não há chaves AWS no histórico.
- Token JWT (Bearer) no `sessionStorage`; o back-end é stateless.

## Fluxo de branches e commits

- `main` (estável) ← `develop` (integração) ← `feat/*` via Pull Request.
- [Conventional Commits](https://www.conventionalcommits.org/): `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:` — um assunto por commit, mensagem no imperativo, nada de `.env`, `node_modules`, `target/` ou logs.
