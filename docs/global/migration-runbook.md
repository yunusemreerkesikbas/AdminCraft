# Migration Runbook

Operational runbook for tenant migration rollout and incident response.

## Rollout Strategy (Canary First)

1. Run sync on a small canary tenant set.
2. Validate key APIs (`/api/pages`, provisioning status endpoints).
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
