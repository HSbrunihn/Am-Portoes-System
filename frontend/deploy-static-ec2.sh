#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="${FRONTEND_DIR:-$SCRIPT_DIR}"
BUILD_DIR="${BUILD_DIR:-$FRONTEND_DIR/dist/am-portoes-frontend/browser}"
PACKAGE_DIR="${PACKAGE_DIR:-$FRONTEND_DIR/dist/ec2-static-package}"

if [[ ! -f "$BUILD_DIR/index.html" ]]; then
  printf 'Build Angular não encontrado em: %s\n' "$BUILD_DIR" >&2
  printf 'Execute npm run build -- --configuration production ou defina BUILD_DIR.\n' >&2
  exit 1
fi

mkdir -p "$PACKAGE_DIR/site" "$PACKAGE_DIR/nginx"
cp -a "$BUILD_DIR/." "$PACKAGE_DIR/site/"

cat > "$PACKAGE_DIR/nginx/site.conf.template" <<'NGINX'
server {
    listen __LISTEN_PORT__;
    server_name __SERVER_NAME__;
    root __WEB_ROOT__;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
NGINX

printf 'Pacote estático preparado em: %s\n' "$PACKAGE_DIR"
printf 'Arquivos Angular: %s/site\n' "$PACKAGE_DIR"
printf 'Template Nginx: %s/nginx/site.conf.template\n' "$PACKAGE_DIR"
printf 'Antes do uso, substitua __LISTEN_PORT__, __SERVER_NAME__ e __WEB_ROOT__ pelos valores definidos para o ambiente.\n'
