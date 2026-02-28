#!/usr/bin/env bash
# =============================================================================
# provision-droplet.sh — Craftive server setup
# =============================================================================
# Target OS : Ubuntu 24.04 LTS
# Usage     : sudo bash provision-droplet.sh <prod|stage> "<deploy_ssh_pubkey>"
#
# Example:
#   sudo bash provision-droplet.sh prod "ssh-ed25519 AAAA... craftive-deploy"
#   sudo bash provision-droplet.sh stage "ssh-ed25519 AAAA... craftive-deploy"
# =============================================================================

set -euo pipefail

# -----------------------------------------------------------------------------
# Colors & helpers
# -----------------------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

log()     { echo -e "${GREEN}[✓]${NC} $1"; }
warn()    { echo -e "${YELLOW}[!]${NC} $1"; }
error()   { echo -e "${RED}[✗]${NC} $1"; exit 1; }
section() { echo -e "\n${BLUE}${BOLD}▶ $1${NC}"; }

# -----------------------------------------------------------------------------
# Argument validation
# -----------------------------------------------------------------------------
[[ $EUID -ne 0 ]] && error "Run as root: sudo bash provision-droplet.sh"
[[ $# -lt 2 ]]   && error "Usage: sudo bash provision-droplet.sh <prod|stage> \"<deploy_ssh_pubkey>\""
[[ "$1" != "prod" && "$1" != "stage" ]] && error "Environment must be 'prod' or 'stage'."

ENV="$1"
DEPLOY_PUBKEY="$2"
APP_DIR="/opt/craftive/${ENV}"
BACKUP_DIR="/opt/craftive/backups/${ENV}"
DEPLOY_USER="deploy"

echo -e "\n${BOLD}═══════════════════════════════════════════${NC}"
echo -e "${BOLD}  Craftive — ${ENV^^} Server Setup${NC}"
echo -e "${BOLD}═══════════════════════════════════════════${NC}"
echo -e "  Environment : ${BOLD}${ENV}${NC}"
echo -e "  Directory   : ${BOLD}${APP_DIR}${NC}"
echo -e "  OS      : $(lsb_release -ds)"
echo -e "${BOLD}═══════════════════════════════════════════${NC}\n"

# -----------------------------------------------------------------------------
# 1. System update
# -----------------------------------------------------------------------------
section "Updating system packages"
apt-get update -qq
apt-get upgrade -y -qq
apt-get install -y -qq \
  curl wget git unzip \
  ca-certificates gnupg \
  ufw fail2ban \
  jq htop ncdu \
  logrotate cron
log "System packages updated"

# -----------------------------------------------------------------------------
# 2. Docker CE install (official Docker repo, not snap)
# -----------------------------------------------------------------------------
section "Installing Docker CE"

if command -v docker &>/dev/null; then
  log "Docker is already installed: $(docker --version)"
else
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
    | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg

  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    | tee /etc/apt/sources.list.d/docker.list > /dev/null

  apt-get update -qq
  apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  systemctl enable --now docker
  log "Docker CE installed: $(docker --version)"
fi

# Docker daemon — log limits and safety settings
cat > /etc/docker/daemon.json <<'EOF'
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "live-restore": true,
  "userland-proxy": false
}
EOF
systemctl is-active --quiet docker || systemctl start docker
systemctl reload docker
log "Docker daemon configured (log limit: 10MB x 3)"

# -----------------------------------------------------------------------------
# 3. Deploy user
# -----------------------------------------------------------------------------
section "Configuring deploy user"

if id "${DEPLOY_USER}" &>/dev/null; then
  warn "User '${DEPLOY_USER}' already exists, updating"
else
  useradd -m -s /bin/bash "${DEPLOY_USER}"
  log "User '${DEPLOY_USER}' created"
fi

# Add to docker group (can run Docker without sudo)
usermod -aG docker "${DEPLOY_USER}"
log "'${DEPLOY_USER}' added to docker group"

# Install SSH public key (GitHub Actions deploy key)
DEPLOY_HOME="/home/${DEPLOY_USER}"
mkdir -p "${DEPLOY_HOME}/.ssh"
chmod 700 "${DEPLOY_HOME}/.ssh"

if grep -qF "${DEPLOY_PUBKEY}" "${DEPLOY_HOME}/.ssh/authorized_keys" 2>/dev/null; then
  warn "SSH public key already exists"
else
  echo "${DEPLOY_PUBKEY}" >> "${DEPLOY_HOME}/.ssh/authorized_keys"
  log "SSH public key added"
fi

chmod 600 "${DEPLOY_HOME}/.ssh/authorized_keys"
chown -R "${DEPLOY_USER}:${DEPLOY_USER}" "${DEPLOY_HOME}/.ssh"

# -----------------------------------------------------------------------------
# 4. SSH hardening
# -----------------------------------------------------------------------------
section "Applying SSH hardening"

SSH_CONFIG="/etc/ssh/sshd_config"

# Force secure SSH settings
sed -i 's/^#*PermitRootLogin.*/PermitRootLogin no/' "${SSH_CONFIG}"
sed -i 's/^#*PasswordAuthentication.*/PasswordAuthentication no/' "${SSH_CONFIG}"
sed -i 's/^#*PubkeyAuthentication.*/PubkeyAuthentication yes/' "${SSH_CONFIG}"
sed -i 's/^#*X11Forwarding.*/X11Forwarding no/' "${SSH_CONFIG}"
sed -i 's/^#*MaxAuthTries.*/MaxAuthTries 3/' "${SSH_CONFIG}"

systemctl reload sshd
log "SSH hardened: root login and password auth disabled"

# -----------------------------------------------------------------------------
# 5. Firewall (UFW)
# -----------------------------------------------------------------------------
section "Configuring UFW firewall"

ufw --force reset
ufw default deny incoming
ufw default allow outgoing

ufw allow 22/tcp comment "SSH"
ufw allow 80/tcp comment "HTTP (Traefik)"
ufw allow 443/tcp comment "HTTPS (Traefik)"

# Prevent external access to internal container ports
if ! grep -q "DOCKER-USER" /etc/ufw/after.rules 2>/dev/null; then
  cat >> /etc/ufw/after.rules <<'EOF'

# Block external access to Docker internal ports
*filter
:DOCKER-USER - [0:0]
-A DOCKER-USER -i eth0 -p tcp --dport 3306 -j DROP
-A DOCKER-USER -i eth0 -p tcp --dport 8080 -j DROP
-A DOCKER-USER -i eth0 -p tcp --dport 3000 -j DROP
COMMIT
EOF
fi

ufw --force enable
log "UFW enabled: only 22/80/443 are open"

# -----------------------------------------------------------------------------
# 6. Fail2ban (brute-force protection)
# -----------------------------------------------------------------------------
section "Configuring Fail2ban"

cat > /etc/fail2ban/jail.local <<'EOF'
[DEFAULT]
bantime  = 1h
findtime = 10m
maxretry = 5

[sshd]
enabled = true
port    = ssh
logpath = %(sshd_log)s
backend = %(syslog_backend)s
EOF

systemctl enable --now fail2ban
log "Fail2ban enabled: 5 failed attempts -> 1 hour ban"

# -----------------------------------------------------------------------------
# 7. Directory structure
# -----------------------------------------------------------------------------
section "Creating application directories"

mkdir -p "${APP_DIR}"
mkdir -p "${BACKUP_DIR}"
mkdir -p /opt/craftive/logs

# Ensure deploy user can manage compose files
chown -R "${DEPLOY_USER}:${DEPLOY_USER}" /opt/craftive
chmod -R 750 /opt/craftive

log "Directory structure created:"
log "  ${APP_DIR}        <- Compose files will be copied here"
log "  ${BACKUP_DIR}     <- Temporary DB dumps"

# -----------------------------------------------------------------------------
# 8. Docker network
# -----------------------------------------------------------------------------
section "Creating Docker network"

if docker network inspect craftive-network &>/dev/null; then
  warn "craftive-network already exists"
else
  docker network create craftive-network
  log "craftive-network created"
fi

# -----------------------------------------------------------------------------
# 9. Backup script
# -----------------------------------------------------------------------------
section "Setting up backup system"

cat > /opt/craftive/backup.sh <<BACKUP_EOF
#!/usr/bin/env bash
# Craftive — MySQL Backup to DO Spaces
# Cron: runs daily at 03:00

set -euo pipefail

ENV="${ENV}"
BACKUP_DIR="${BACKUP_DIR}"
DATE=\$(date +%Y-%m-%d)
TIMESTAMP=\$(date +%Y-%m-%d_%H-%M)
CONTAINER="craftive-mysql"
ENV_FILE="${APP_DIR}/.env.${ENV}"

# Read DB_PASSWORD from env file (safe: no shell execution of .env content)
DB_PASSWORD=\$(grep "^DB_PASSWORD=" "\${ENV_FILE}" | cut -d'=' -f2-)
export DB_PASSWORD

# MySQL dump — all databases
DUMP_FILE="\${BACKUP_DIR}/\${TIMESTAMP}.sql.gz"
docker exec "\${CONTAINER}" mysqldump \\
  -uroot -p"\${DB_PASSWORD}" \\
  --all-databases \\
  --single-transaction \\
  --quick \\
  --lock-tables=false \\
  | gzip > "\${DUMP_FILE}"

echo "\$(date): Backup created: \${DUMP_FILE} (\$(du -sh \${DUMP_FILE} | cut -f1))"

# Upload to DO Spaces (rclone must be installed/configured)
if command -v rclone &>/dev/null; then
  rclone copy "\${DUMP_FILE}" "spaces:craftive-backups/\${ENV}/\${DATE}/" --quiet
  echo "\$(date): Uploaded to DO Spaces"
  rm -f "\${DUMP_FILE}"
else
  echo "\$(date): WARNING: rclone is not installed, backup kept locally"
fi

# Delete local backups older than 7 days (Spaces handles 30-day retention)
find "\${BACKUP_DIR}" -name "*.sql.gz" -mtime +7 -delete
BACKUP_EOF

chmod +x /opt/craftive/backup.sh
chown "${DEPLOY_USER}:${DEPLOY_USER}" /opt/craftive/backup.sh

# Cron job — daily at 03:00 (UTC+3 = 00:00 UTC)
CRON_JOB="0 0 * * * /opt/craftive/backup.sh >> /opt/craftive/logs/backup.log 2>&1"
if ! crontab -l -u "${DEPLOY_USER}" 2>/dev/null | grep -qF "backup.sh"; then
  (crontab -l -u "${DEPLOY_USER}" 2>/dev/null; echo "${CRON_JOB}") | crontab -u "${DEPLOY_USER}" -
  log "Backup cron job added: daily at 03:00 (Istanbul)"
else
  warn "Backup cron job already exists"
fi

# -----------------------------------------------------------------------------
# 10. Log rotation
# -----------------------------------------------------------------------------
section "Configuring log rotation"

cat > /etc/logrotate.d/craftive <<EOF
/opt/craftive/logs/*.log {
    daily
    missingok
    rotate 14
    compress
    delaycompress
    notifempty
    create 0640 ${DEPLOY_USER} ${DEPLOY_USER}
}
EOF
log "Log rotation: 14 days, daily compression"

# -----------------------------------------------------------------------------
# 11. rclone install (for DO Spaces)
# -----------------------------------------------------------------------------
section "Installing rclone (DO Spaces backup)"

if command -v rclone &>/dev/null; then
  warn "rclone is already installed: $(rclone --version | head -1)"
else
  apt-get install -y -qq rclone
  log "rclone installed: $(rclone --version | head -1)"
fi

warn "Next step: configure rclone with scripts/server/configure-rclone.sh"

# -----------------------------------------------------------------------------
# Summary
# -----------------------------------------------------------------------------
echo -e "\n${BOLD}${GREEN}═══════════════════════════════════════════${NC}"
echo -e "${BOLD}${GREEN}  Setup Complete — ${ENV^^}${NC}"
echo -e "${BOLD}${GREEN}═══════════════════════════════════════════${NC}"
echo -e "
${BOLD}Next Steps:${NC}

  1. Copy compose files:
     ${YELLOW}scp docker-compose.yml docker-compose.prod.yml .env.${ENV} \\
       deploy@<IP>:${APP_DIR}/${NC}

  2. Configure rclone (DO Spaces backup):
     ${YELLOW}bash scripts/server/configure-rclone.sh${NC}

  3. Run the first deployment:
     ${YELLOW}cd ${APP_DIR}
     docker compose -f docker-compose.yml -f docker-compose.prod.yml \\
       --env-file .env.${ENV} up -d${NC}

  4. Health check:
     ${YELLOW}curl https://api.craftive.io/api/actuator/health${NC}

${BOLD}Security Status:${NC}
  UFW        : Enabled (22/80/443)
  Fail2ban   : Enabled (5 failures -> 1 hour ban)
  SSH        : Password auth disabled
  Docker     : Log limit enabled (10MB x 3)
  Deploy user: '${DEPLOY_USER}' (docker group, no sudo)

${BOLD}Backup:${NC}
  Cron: Daily at 00:00 UTC -> ${BACKUP_DIR}
  Log : /opt/craftive/logs/backup.log
"
