#!/usr/bin/env bash
# =============================================================================
# deploy-tenant-storefront.sh — Deploy/Update Tenant Storefront (Stage/Prod)
# =============================================================================
# Usage:
#   bash scripts/server/deploy-tenant-storefront.sh <stage|prod> <tenant_slug> <image_ref> [primary_domain] [extra_domains_csv]
#
# Examples:
#   bash scripts/server/deploy-tenant-storefront.sh stage democompany ghcr.io/craftive/democompany-storefront:stage-a3f9c12
#   bash scripts/server/deploy-tenant-storefront.sh prod democompany ghcr.io/craftive/democompany-storefront:release-27.02.2026 democompany.com "www.democompany.com,democompany.craftive.io"
#
# Notes:
#   - This script is designed to run on the target droplet.
#   - Traefik must already be running in the same Docker network (craftive-network).
# =============================================================================

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BOLD='\033[1m'
NC='\033[0m'

log()   { echo -e "${GREEN}[+]${NC} $1"; }
warn()  { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[x]${NC} $1"; exit 1; }

usage() {
  cat <<'EOF'
Usage:
  bash scripts/server/deploy-tenant-storefront.sh <stage|prod> <tenant_slug> <image_ref> [primary_domain] [extra_domains_csv]

Arguments:
  stage|prod        Target environment
  tenant_slug       Lowercase tenant slug (letters, numbers, hyphen)
  image_ref         Full image reference (example: ghcr.io/craftive/acme-storefront:stage-a1b2c3d)
  primary_domain    Optional. Defaults to:
                    - stage: s1-<tenant_slug>.craftive.io
                    - prod : <tenant_slug>.craftive.io
  extra_domains_csv Optional comma-separated extra domains.
EOF
}

[[ $# -lt 3 ]] && usage && error "Missing required arguments."

ENV="$1"
TENANT_SLUG="$2"
IMAGE_REF="$3"
PRIMARY_DOMAIN="${4:-}"
EXTRA_DOMAINS_CSV="${5:-}"
BASE_DOMAIN="${BASE_DOMAIN:-craftive.io}"

[[ "${ENV}" != "stage" && "${ENV}" != "prod" ]] && error "Environment must be 'stage' or 'prod'."
[[ "${TENANT_SLUG}" =~ ^[a-z0-9]+(-[a-z0-9]+)*$ ]] || error "Invalid tenant slug: ${TENANT_SLUG}"
[[ "${IMAGE_REF}" =~ ^[^[:space:]]+$ ]] || error "Image reference must not contain spaces."

if [[ "${IMAGE_REF}" != *:* && "${IMAGE_REF}" != *@* ]]; then
  warn "Image reference has no explicit tag/digest: ${IMAGE_REF}"
fi

command -v docker >/dev/null 2>&1 || error "Docker is not installed."
docker network inspect craftive-network >/dev/null 2>&1 || error "Docker network 'craftive-network' was not found."

if [[ -z "${PRIMARY_DOMAIN}" ]]; then
  if [[ "${ENV}" == "stage" ]]; then
    PRIMARY_DOMAIN="s1-${TENANT_SLUG}.${BASE_DOMAIN}"
  else
    PRIMARY_DOMAIN="${TENANT_SLUG}.${BASE_DOMAIN}"
  fi
fi

declare -A DOMAIN_SET=()
DOMAINS=()

add_domain() {
  local domain="$1"
  domain="$(echo "${domain}" | tr '[:upper:]' '[:lower:]' | xargs)"
  [[ -z "${domain}" ]] && return 0
  [[ "${domain}" =~ ^[a-z0-9.-]+$ ]] || error "Invalid domain: ${domain}"
  [[ "${domain}" == .* || "${domain}" == *..* || "${domain}" == *-.* || "${domain}" == *.-* ]] && error "Invalid domain: ${domain}"
  if [[ -z "${DOMAIN_SET[${domain}]:-}" ]]; then
    DOMAIN_SET["${domain}"]=1
    DOMAINS+=("${domain}")
  fi
}

add_domain "${PRIMARY_DOMAIN}"

if [[ -n "${EXTRA_DOMAINS_CSV}" ]]; then
  IFS=',' read -r -a EXTRA_DOMAINS <<< "${EXTRA_DOMAINS_CSV}"
  for d in "${EXTRA_DOMAINS[@]}"; do
    add_domain "${d}"
  done
fi

[[ "${#DOMAINS[@]}" -gt 0 ]] || error "No valid domains provided."

build_host_rule() {
  local rule=""
  local domain
  for domain in "$@"; do
    if [[ -n "${rule}" ]]; then
      rule="${rule} || "
    fi
    rule="${rule}Host(\`${domain}\`)"
  done
  printf '%s' "${rule}"
}

HOST_RULE="$(build_host_rule "${DOMAINS[@]}")"
SERVICE_NAME="storefront-${TENANT_SLUG}"
ROUTER_NAME="tenant-${ENV}-${TENANT_SLUG}"
PROJECT_NAME="${ROUTER_NAME}"
TARGET_DIR="/opt/craftive/${ENV}/tenant-storefronts"
COMPOSE_FILE="${TARGET_DIR}/${TENANT_SLUG}.yml"

mkdir -p "${TARGET_DIR}"

cat > "${COMPOSE_FILE}" <<EOF
services:
  ${SERVICE_NAME}:
    image: ${IMAGE_REF}
    restart: unless-stopped
    environment:
      NODE_ENV: production
    networks:
      - craftive-network
    labels:
      traefik.enable: "true"
      traefik.http.routers.${ROUTER_NAME}.rule: "${HOST_RULE}"
      traefik.http.routers.${ROUTER_NAME}.entrypoints: "websecure"
      traefik.http.routers.${ROUTER_NAME}.tls.certresolver: "letsencrypt"
      traefik.http.routers.${ROUTER_NAME}.priority: "20"
      traefik.http.services.${ROUTER_NAME}.loadbalancer.server.port: "3000"

networks:
  craftive-network:
    external: true
EOF

log "Rendered compose: ${COMPOSE_FILE}"
log "Deploying tenant storefront (${TENANT_SLUG}) to ${ENV}..."

docker compose -p "${PROJECT_NAME}" -f "${COMPOSE_FILE}" pull
docker compose -p "${PROJECT_NAME}" -f "${COMPOSE_FILE}" up -d --remove-orphans

echo -e "\n${BOLD}Deployed${NC}"
echo "  Environment : ${ENV}"
echo "  Tenant      : ${TENANT_SLUG}"
echo "  Image       : ${IMAGE_REF}"
echo "  Project     : ${PROJECT_NAME}"
echo "  Domains     : ${DOMAINS[*]}"

