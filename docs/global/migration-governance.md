# Migration Governance

This document defines the permanent rules for safe tenant migrations in AdminCraft.

## Core Policy

1. `Forward-only`: Existing versioned migrations (`V*.sql`) are immutable after merge.
2. `Repair via new versions`: Legacy drift fixes must be implemented as new `V*__repair_*.sql` migrations.
3. `Repeatable safety`: `R__*.sql` must use explicit column lists for every `INSERT`.
4. `Module order`: `core -> mail_marketing -> media -> component_library -> pagebuilder -> product` is mandatory.

## Why this policy exists

Legacy tenants may have partial/manual schema changes. Editing old migrations creates checksum churn and unstable sync behavior. Forward-only repairs keep history deterministic.

## Repair Migration Standard

Use `INFORMATION_SCHEMA` checks to guard:

- `AddColumnIfNotExists`
- `AddIndexIfNotExists`
- `CreateTableIfNotExists`
- FK existence checks via `REFERENTIAL_CONSTRAINTS`

Repair migrations must be idempotent and non-destructive by default.

## Prohibited

- Modifying or deleting previously released `V*.sql` files
- Relying on implicit column order in repeatable seeds
- Cross-module FK changes that violate module order

## CI Requirements

Every PR touching migration files must pass:

1. Versioned migration immutability check
2. Migration lint checks
3. Backend compile sanity

Workflow: `.github/workflows/migration-guardrails.yml`

Scripts:

- `scripts/migrations/check-versioned-immutability.sh`
- `scripts/migrations/lint-migrations.sh`

## Manual Execution (Local)

Run these before opening a migration PR:

```bash
bash scripts/migrations/check-versioned-immutability.sh
bash scripts/migrations/lint-migrations.sh
```

Notes:

- `check-versioned-immutability.sh` compares your branch against `origin/master` by default.
- If your base branch is different, pass it explicitly:

```bash
bash scripts/migrations/check-versioned-immutability.sh release/2026.02
```

- On Windows, use Git Bash or WSL for `bash` scripts.

# Migration Runbook

Operational runbook for tenant migration rollout and incident response.

## Rollout Strategy (Canary First)

1. Run sync on a small canary tenant set.
2. Validate key APIs (`/api/cms/pages`, provisioning status endpoints).
3. Expand to medium tenant wave.
4. Roll out globally.

## Pre-checks

1. Ensure backend version with latest repair migrations is deployed.
2. Verify migration guardrails are green in CI.
3. Confirm module order is unchanged.

## Failure Triage

When a sync job fails:

1. Capture `jobId`, module name, migration name, SQL error.
2. Check Flyway history tables:
   - `flyway_core_history`
   - `flyway_mail_marketing_history`
   - `flyway_media_history`
   - `flyway_component_library_history`
   - `flyway_pagebuilder_history`
   - `flyway_product_history`
3. Classify error:
   - Duplicate column/table -> add forward-only repair migration.
   - Unknown column in repeatable seed -> update seed to explicit compatible insert.
   - FK constraint error -> verify referenced table/column order and data type.

## Useful SQL

```sql
SELECT version, description, success, installed_on
FROM flyway_pagebuilder_history
ORDER BY installed_rank DESC
LIMIT 20;
```

```sql
SELECT table_name, column_name, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'tenant_<db_name>'
  AND column_name IN ('created_by', 'page_type', 'responsive_id');
```

## Recovery Rules

1. Do not edit old `V*.sql` to fix production drift.
2. Add a new `V*__repair_*.sql`.
3. Redeploy and rerun sync.
