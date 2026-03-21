# Database Migrations (Flyway)

## 1. What it is / why it exists

AdminCraft uses **Flyway** for database migrations with a **modular, per-tenant** approach. Each tenant database is migrated independently with module-specific migration scripts.

This architecture ensures that tenant databases can be provisioned with only the required modules and allows the platform to maintain schema consistency across isolated databases while providing a robust mechanism to manage schema evolution.

Legacy tenants may experience schema drift. The migration system includes governance rules (forward-only, idempotent repairs) to ensure that sync operations remain deterministic and safe across all environments.

## 2. Source of truth

| Concern | File |
|---------|------|
| Migration Files | `backend/src/main/resources/db/` |
| Module Execution Order | `backend/src/main/java/com/backend/application/service/provisioning/TenantMigrationService.java` |
| CI Governance Script | `scripts/migrations/check-versioned-immutability.sh` |
| CI Lint Script | `scripts/migrations/lint-migrations.sh` |
| GitHub Workflow | `.github/workflows/migration-guardrails.yml` |

## 3. Rules and invariants

### Core Policy

1. **Forward-only**: Existing versioned migrations (`V*.sql`) are immutable after merge. Modifying or deleting previously released `V*.sql` files is prohibited.
2. **Repair via new versions**: Legacy drift fixes must be implemented as new `V*__repair_*.sql` migrations.
3. **Repeatable safety**: `R__*.sql` must use explicit column lists for every `INSERT`. Do not rely on implicit column order.
4. **Module order**: The execution order `core -> mail_marketing -> media -> component_library -> pagebuilder -> product` is mandatory. Do not create cross-module FK changes that violate this order.
5. **Idempotent Repairs**: Repair migrations must use `INFORMATION_SCHEMA` checks to guard DDL operations (`AddColumnIfNotExists`, `AddIndexIfNotExists`, `CreateTableIfNotExists`).

> **Pre-launch note:** All pre-launch migrations were squashed into `V1.0.0__baseline.sql` per module. The forward-only policy applies strictly to any changes merged after this baseline.

### CI Requirements

Every PR touching migration files must pass the Migration Guardrails workflow, which includes:
1. Versioned migration immutability check
2. Migration lint checks
3. Backend compile sanity

## 4. Common patterns

### File Structure and Naming

```text
backend/src/main/resources/db/
├── platform/           # Platform database migrations
│   └── V1.0.0__baseline.sql
└── tenant/             # Tenant database migrations (per module)
    ├── core/           # Always runs first
    ├── mail_marketing/ # Optional mail marketing module
    ├── media/          # Media assets, responsive sets
    ├── component_library/
    ├── pagebuilder/
    └── product/
```

| Type       | Pattern                   | Example                       |
| ---------- | ------------------------- | ----------------------------- |
| Versioned  | `V{n}__{description}.sql` | `V2__add_new_feature.sql`     |
| Repeatable | `R__{description}.sql`    | `R__seed_component_types.sql` |

- **Versioned migrations** run once, in order.
- **Repeatable migrations** run whenever their checksum changes. Only structural system data (e.g., component types, media formats, product types) remains in `R__*.sql` files. Content-heavy seeds are managed via **ImpEx** (`backend/src/main/resources/impex/`).

### Module Execution Order

Modules are executed in this fixed order to ensure dependencies are satisfied:

```text
1. core              → Base tables (users, sites)
2. mail_marketing    → Optional tenant mail marketing tables
3. media             → Media, responsive_media_set
4. component_library → Components, entries (references media)
5. pagebuilder       → Pages, slots (references components)
6. product           → Product catalog
```

### Migration Runbook (Rollout Strategy)

When deploying new migrations to production:
1. **Canary First:** Run sync on a small canary tenant set.
2. **Validate:** Check key APIs (`/api/cms/pages`, provisioning status endpoints).
3. **Expand:** Roll out to a medium tenant wave.
4. **Global:** Roll out globally.

## 5. Gotchas

### Common Errors

| Error                                     | Cause                                    | Solution                                            |
| ----------------------------------------- | ---------------------------------------- | --------------------------------------------------- |
| `Failed to open the referenced table 'X'` | FK references table from later module    | Move migration to the module that creates table X   |
| `Unknown column 'X' in field list`        | Column removed but seed file not updated | Update `R__seed_*.sql` to match current schema      |
| `Duplicate column name 'X'`               | Legacy drift / partial previous schema   | Add forward-only `V*__repair_*.sql` guard migration |

### `IF NOT EXISTS` limitation
Do not use direct `IF NOT EXISTS` on `ALTER TABLE` operations, as MySQL support is limited/inconsistent. Use a forward-only repair migration with `INFORMATION_SCHEMA` guards (e.g., `AddColumnIfNotExists` procedure).

### Failure Triage and Recovery

When a sync job fails:
1. Capture `jobId`, module name, migration name, and SQL error.
2. Check Flyway history tables (e.g., `flyway_pagebuilder_history`) for the specific tenant database.
3. Classify error (Duplicate column/table, Unknown column in repeatable seed, FK constraint error).
4. **Recovery:** Do not edit the old `V*.sql` file to fix the drift. Instead, add a new `V*__repair_*.sql` migration, redeploy, and rerun the sync.

```sql
-- Useful SQL for triage
SELECT version, description, success, installed_on
FROM flyway_pagebuilder_history
ORDER BY installed_rank DESC
LIMIT 20;

SELECT table_name, column_name, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'tenant_<db_name>'
  AND column_name IN ('created_by', 'page_type', 'responsive_id');
```