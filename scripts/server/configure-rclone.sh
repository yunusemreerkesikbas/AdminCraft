#!/usr/bin/env bash
# =============================================================================
# configure-rclone.sh — Configure DO Spaces backup
# =============================================================================
# Usage: bash scripts/server/configure-rclone.sh
#
# Requirements:
#   - DO Spaces bucket: craftive-backups (Frankfurt / FRA1)
#   - DO Spaces Access Key + Secret Key
#     (DigitalOcean → API → Spaces Keys → Generate New Key)
# =============================================================================

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BOLD='\033[1m'
NC='\033[0m'

log()  { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }

echo -e "\n${BOLD}rclone — DO Spaces Configuration${NC}\n"

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
read -rp "DO Spaces Access Key: " SPACES_ACCESS_KEY
read -rsp "DO Spaces Secret Key: " SPACES_SECRET_KEY
echo

mkdir -p "${RCLONE_CONFIG_DIR}"

cat > "${RCLONE_CONFIG_DIR}/rclone.conf" <<EOF
[spaces]
type = s3
provider = DigitalOcean
access_key_id = ${SPACES_ACCESS_KEY}
secret_access_key = ${SPACES_SECRET_KEY}
endpoint = fra1.digitaloceanspaces.com
region = fra1
acl = private
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
    if runuser -u "${DEPLOY_USER}" -- rclone ls spaces:craftive-backups --max-depth 1 &>/dev/null; then
      log "DO Spaces connection successful: craftive-backups bucket is reachable"
    else
      warn "Connection failed — check Access Key, Secret Key, and bucket name"
      warn "Bucket: DigitalOcean -> Spaces -> craftive-backups (Frankfurt/FRA1)"
    fi
  else
    if su -s /bin/bash -c "rclone ls spaces:craftive-backups --max-depth 1" "${DEPLOY_USER}" &>/dev/null; then
      log "DO Spaces connection successful: craftive-backups bucket is reachable"
    else
      warn "Connection failed — check Access Key, Secret Key, and bucket name"
      warn "Bucket: DigitalOcean -> Spaces -> craftive-backups (Frankfurt/FRA1)"
    fi
  fi
elif rclone ls spaces:craftive-backups --max-depth 1 &>/dev/null; then
  log "DO Spaces connection successful: craftive-backups bucket is reachable"
else
  warn "Connection failed — check Access Key, Secret Key, and bucket name"
  warn "Bucket: DigitalOcean -> Spaces -> craftive-backups (Frankfurt/FRA1)"
fi

echo -e "\n${BOLD}Backup test reminder...${NC}"
warn "To run a real backup test: bash /opt/craftive/backup.sh"
