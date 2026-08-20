#!/usr/bin/env bash
# =============================================================================
# configure-rclone.sh — Configure Cloudflare R2 backup
# =============================================================================
# Usage: bash scripts/server/configure-rclone.sh
#
# Requirements:
#   - R2 bucket: craftive-backups-prod
#   - R2 account endpoint, Access Key ID, and Secret Access Key
# =============================================================================

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BOLD='\033[1m'
NC='\033[0m'

log()  { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }

echo -e "\n${BOLD}rclone — Cloudflare R2 Configuration${NC}\n"

command -v rclone &>/dev/null || { echo "rclone is not installed. Run provision-droplet.sh first."; exit 1; }

DEPLOY_USER="${DEPLOY_USER:-deploy}"
CURRENT_USER="$(id -un)"

if ! id "${DEPLOY_USER}" &>/dev/null; then
  echo "User not found: ${DEPLOY_USER}"
  exit 1
fi

if [[ "${CURRENT_USER}" != "root" && "${CURRENT_USER}" != "${DEPLOY_USER}" ]]; then
  echo "Run this script as root or ${DEPLOY_USER}."
  exit 1
fi

DEPLOY_HOME="$(getent passwd "${DEPLOY_USER}" | cut -d: -f6)"
RCLONE_CONFIG_DIR="${DEPLOY_HOME}/.config/rclone"

# Interactive input
read -rp "Cloudflare account ID: " R2_ACCOUNT_ID
read -rp "R2 Access Key ID: " R2_ACCESS_KEY
read -rsp "R2 Secret Access Key: " R2_SECRET_KEY
echo

mkdir -p "${RCLONE_CONFIG_DIR}"

[ -f "${RCLONE_CONFIG_DIR}/rclone.conf" ] && \
  cp "${RCLONE_CONFIG_DIR}/rclone.conf" \
     "${RCLONE_CONFIG_DIR}/rclone.conf.bak.$(date +%Y%m%d_%H%M%S)"

cat > "${RCLONE_CONFIG_DIR}/rclone.conf" <<EOF
[r2]
type = s3
provider = Cloudflare
access_key_id = ${R2_ACCESS_KEY}
secret_access_key = ${R2_SECRET_KEY}
endpoint = https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com
region = auto
acl = private
no_check_bucket = true
EOF

chmod 600 "${RCLONE_CONFIG_DIR}/rclone.conf"
if [[ "${CURRENT_USER}" == "root" ]]; then
  chown -R "${DEPLOY_USER}:${DEPLOY_USER}" "${DEPLOY_HOME}/.config"
fi
log "rclone configuration saved"

# Test
echo -e "\n${BOLD}Testing connection...${NC}"
if [[ "${CURRENT_USER}" == "root" ]]; then
  if command -v runuser &>/dev/null; then
    if runuser -u "${DEPLOY_USER}" -- rclone ls r2:craftive-backups-prod --max-depth 1 &>/dev/null; then
      log "Cloudflare R2 connection successful: craftive-backups-prod is reachable"
    else
      warn "Connection failed — check Access Key, Secret Key, and bucket name"
      warn "Bucket: Cloudflare R2 -> craftive-backups-prod"
    fi
  else
    if su -s /bin/bash -c "rclone ls r2:craftive-backups-prod --max-depth 1" "${DEPLOY_USER}" &>/dev/null; then
      log "Cloudflare R2 connection successful: craftive-backups-prod is reachable"
    else
      warn "Connection failed — check Access Key, Secret Key, and bucket name"
      warn "Bucket: Cloudflare R2 -> craftive-backups-prod"
    fi
  fi
elif rclone ls r2:craftive-backups-prod --max-depth 1 &>/dev/null; then
  log "Cloudflare R2 connection successful: craftive-backups-prod is reachable"
else
  warn "Connection failed — check Access Key, Secret Key, and bucket name"
  warn "Bucket: Cloudflare R2 -> craftive-backups-prod"
fi

echo -e "\n${BOLD}Backup test reminder...${NC}"
warn "To run a real backup test: bash /opt/craftive/backup.sh"
