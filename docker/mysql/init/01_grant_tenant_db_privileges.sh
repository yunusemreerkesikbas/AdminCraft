#!/bin/bash
# Grant the application user rights to create and manage per-tenant databases.
# MySQL Docker image only grants MYSQL_USER access to MYSQL_DATABASE (platform_management).
# Provisioning creates databases matching ac_<subdomain>_<tenantId>; this grant covers that namespace.
set -euo pipefail

mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" <<-EOSQL
    GRANT ALL PRIVILEGES ON \`ac_%\`.* TO '${MYSQL_USER}'@'%';
    FLUSH PRIVILEGES;
EOSQL
