#!/usr/bin/env bash
# =============================================================================
# remove-tenant-storefront.sh — Remove Tenant Storefront Deployment
# =============================================================================
# Usage:
#   bash scripts/server/remove-tenant-storefront.sh <stage|prod> <tenant_slug>
#
# Example:
#   bash scripts/server/remove-tenant-storefront.sh stage democompany
# =============================================================================

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
BOLD='\033[1m'
NC='\033[0m'

log()   { echo -e "${GREEN}[+]${NC} $1"; }
error() { echo -e "${RED}[x]${NC} $1"; exit 1; }

usage() {
  cat <<'EOF'
Usage:
  bash scripts/server/remove-tenant-storefront.sh <stage|prod> <tenant_slug>
EOF
}

[[ $# -lt 2 ]] && usage && error "Missing required arguments."

ENV="$1"
TENANT_SLUG="$2"

[[ "${ENV}" != "stage" && "${ENV}" != "prod" ]] && error "Environment must be 'stage' or 'prod'."
[[ "${TENANT_SLUG}" =~ ^[a-z0-9]+(-[a-z0-9]+)*$ ]] || error "Invalid tenant slug: ${TENANT_SLUG}"

TARGET_DIR="/opt/craftive/${ENV}/tenant-storefronts"
COMPOSE_FILE="${TARGET_DIR}/${TENANT_SLUG}.yml"
PROJECT_NAME="tenant-${ENV}-${TENANT_SLUG}"

[[ -f "${COMPOSE_FILE}" ]] || error "Tenant compose file not found: ${COMPOSE_FILE}"

log "Stopping tenant storefront project: ${PROJECT_NAME}"
docker compose -p "${PROJECT_NAME}" -f "${COMPOSE_FILE}" down --remove-orphans

rm -f "${COMPOSE_FILE}"
log "Removed compose file: ${COMPOSE_FILE}"

echo -e "\n${BOLD}Removed${NC}"
echo "  Environment : ${ENV}"
echo "  Tenant      : ${TENANT_SLUG}"
echo "  Project     : ${PROJECT_NAME}"

