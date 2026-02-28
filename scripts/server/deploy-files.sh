#!/usr/bin/env bash
# =============================================================================
# deploy-files.sh — Copy compose files to droplet
# =============================================================================
# Usage (from project root):
#   bash scripts/server/deploy-files.sh <prod|stage> <DROPLET_IP>
#
# Example:
#   bash scripts/server/deploy-files.sh prod 134.122.x.x
#   bash scripts/server/deploy-files.sh stage 134.122.x.x
# =============================================================================

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BOLD='\033[1m'
NC='\033[0m'

log()   { echo -e "${GREEN}[✓]${NC} $1"; }
warn()  { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[✗]${NC} $1"; exit 1; }

[[ $# -lt 2 ]] && error "Usage: bash scripts/server/deploy-files.sh <prod|stage> <DROPLET_IP>"
[[ "$1" != "prod" && "$1" != "stage" ]] && error "Environment must be 'prod' or 'stage'."

ENV="$1"
DROPLET_IP="$2"
SSH_KEY="${HOME}/.ssh/craftive_deploy"
REMOTE_DIR="/opt/craftive/${ENV}"
REMOTE="deploy@${DROPLET_IP}"

echo -e "\n${BOLD}Craftive — ${ENV^^} File Sync${NC}"
echo -e "Target: ${REMOTE}:${REMOTE_DIR}\n"

[[ -f "${SSH_KEY}" ]] || error "SSH key not found: ${SSH_KEY}"
[[ -f ".env.${ENV}" ]] || error ".env.${ENV} was not found. Create it first."

# Files to copy
FILES=(
  "docker-compose.yml"
  "docker-compose.prod.yml"
)

[[ "${ENV}" == "stage" ]] && FILES+=("docker-compose.stage.yml")

echo -e "${BOLD}Files to copy:${NC}"
for f in "${FILES[@]}"; do
  echo "  - ${f}"
done
echo "  - .env.${ENV}"
echo

# Copy files
for f in "${FILES[@]}"; do
  scp -i "${SSH_KEY}" -o StrictHostKeyChecking=accept-new \
    "${f}" "${REMOTE}:${REMOTE_DIR}/${f}"
  log "Copied ${f}"
done

# Copy .env file
scp -i "${SSH_KEY}" -o StrictHostKeyChecking=accept-new \
  ".env.${ENV}" "${REMOTE}:${REMOTE_DIR}/.env.${ENV}"
log "Copied .env.${ENV}"

# Copy Docker MySQL config files
ssh -i "${SSH_KEY}" "${REMOTE}" "mkdir -p ${REMOTE_DIR}/docker/mysql/conf ${REMOTE_DIR}/docker/mysql/init"
scp -i "${SSH_KEY}" -r docker/mysql "${REMOTE}:${REMOTE_DIR}/docker/"
log "Copied docker/mysql"

# Copy tenant storefront operation scripts
REMOTE_SCRIPT_DIR="/opt/craftive/scripts"
ssh -i "${SSH_KEY}" "${REMOTE}" "mkdir -p ${REMOTE_SCRIPT_DIR}"
scp -i "${SSH_KEY}" -o StrictHostKeyChecking=accept-new \
  scripts/server/deploy-tenant-storefront.sh \
  scripts/server/remove-tenant-storefront.sh \
  "${REMOTE}:${REMOTE_SCRIPT_DIR}/"
ssh -i "${SSH_KEY}" "${REMOTE}" "chmod +x ${REMOTE_SCRIPT_DIR}/deploy-tenant-storefront.sh ${REMOTE_SCRIPT_DIR}/remove-tenant-storefront.sh"
log "Copied tenant storefront scripts (${REMOTE_SCRIPT_DIR})"

echo -e "\n${GREEN}${BOLD}All files copied successfully.${NC}"
echo -e "\nRun on the server:"
echo -e "${YELLOW}  ssh -i ${SSH_KEY} ${REMOTE}"
echo -e "  cd ${REMOTE_DIR}"

if [[ "${ENV}" == "stage" ]]; then
  echo -e "  docker compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.stage.yml \\"
else
  echo -e "  docker compose -f docker-compose.yml -f docker-compose.prod.yml \\"
fi

echo -e "    --env-file .env.${ENV} up -d --build${NC}"

echo -e "\nTenant storefront deploy command (on server):"
echo -e "${YELLOW}  bash /opt/craftive/scripts/deploy-tenant-storefront.sh ${ENV} <tenant-slug> <image-ref> [primary-domain] [extra-domains-csv]${NC}"
