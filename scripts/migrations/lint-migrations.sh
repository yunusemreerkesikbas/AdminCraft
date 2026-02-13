#!/usr/bin/env bash
set -euo pipefail

ROOT="backend/src/main/resources/db/tenant"

echo "Running migration lint checks..."

# 1) Repeatable seeds must use explicit column list in INSERT.
BAD_INSERTS="$(
  rg -n --glob '**/R__*.sql' 'INSERT\s+INTO\s+[a-zA-Z0-9_]+\s*(SELECT|VALUES)' "${ROOT}" || true
)"
if [[ -n "${BAD_INSERTS}" ]]; then
  echo "ERROR: Repeatable seeds must use explicit column lists in INSERT INTO."
  echo "${BAD_INSERTS}"
  exit 1
fi

# 2) Guard against known strict schema issue: pages.created_by is required in some tenants.
#    Any repeatable seed inserting into pages must provide created_by.
PAGE_INSERTS_WITHOUT_CREATED_BY="$(
  rg -n -P --glob '**/R__*.sql' 'INSERT\s+INTO\s+pages\s*\((?:(?!created_by).)*\)' "${ROOT}" -U || true
)"
if [[ -n "${PAGE_INSERTS_WITHOUT_CREATED_BY}" ]]; then
  echo "ERROR: INSERT INTO pages in repeatable seeds must include created_by."
  echo "${PAGE_INSERTS_WITHOUT_CREATED_BY}"
  exit 1
fi

# 3) Guard against high-risk direct ALTER ADD COLUMN in new repair files without INFORMATION_SCHEMA checks.
#    Repair migrations should be idempotent. We only lint repair files.
NON_GUARDED_REPAIR_ALTERS="$(
  rg -n --glob '**/V*__*repair*.sql' 'ALTER\s+TABLE\s+.*\s+ADD\s+COLUMN' "${ROOT}" || true
)"
if [[ -n "${NON_GUARDED_REPAIR_ALTERS}" ]]; then
  # We allow them only if file also contains INFORMATION_SCHEMA checks.
  while IFS=: read -r file _; do
    [[ -z "${file}" ]] && continue
    if ! rg -q 'INFORMATION_SCHEMA\.(COLUMNS|STATISTICS|TABLES|REFERENTIAL_CONSTRAINTS)' "${file}"; then
      echo "ERROR: Repair migration has non-guarded ALTER ADD COLUMN: ${file}"
      exit 1
    fi
  done < <(printf '%s\n' "${NON_GUARDED_REPAIR_ALTERS}")
fi

echo "OK: Migration lint checks passed."
