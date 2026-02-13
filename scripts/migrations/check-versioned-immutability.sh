#!/usr/bin/env bash
set -euo pipefail

# Enforce forward-only policy:
# Existing versioned migration files (V*.sql) must not be modified or deleted.
# Adding new versioned migrations is allowed.

BASE_REF="${1:-${GITHUB_BASE_REF:-master}}"

if ! git rev-parse --verify "origin/${BASE_REF}" >/dev/null 2>&1; then
  echo "Fetching origin/${BASE_REF} for diff baseline..."
  git fetch origin "${BASE_REF}" --depth=1
fi

DIFF_OUTPUT="$(git diff --name-status "origin/${BASE_REF}...HEAD")"

VIOLATIONS="$(
  printf '%s\n' "${DIFF_OUTPUT}" | awk '
    $2 ~ /^backend\/src\/main\/resources\/db\/.*\/V[0-9]+__.*\.sql$/ {
      status=$1
      # Added (A) is allowed; any other status is forbidden.
      if (status != "A") print $0
    }'
)"

if [[ -n "${VIOLATIONS}" ]]; then
  echo "ERROR: Existing versioned migrations were modified/deleted. Forward-only policy violated."
  echo "Violations:"
  echo "${VIOLATIONS}"
  exit 1
fi

echo "OK: Versioned migration immutability check passed."
