# Database Migrations (Flyway)

AdminCraft uses **Flyway** for database migrations with a **modular, per-tenant** approach. Each tenant database is migrated independently with module-specific migration scripts.

## Migration Architecture

### File Structure

```
backend/src/main/resources/db/
├── platform/           # Platform database migrations
│   └── V1__baseline.sql
└── tenant/             # Tenant database migrations (per module)
    ├── core/           # Always runs first
    ├── media/          # Media assets, responsive sets
    ├── component_library/
    ├── pagebuilder/
    └── product/
```

### Naming Convention

| Type       | Pattern                   | Example                       |
| ---------- | ------------------------- | ----------------------------- |
| Versioned  | `V{n}__{description}.sql` | `V23__responsive_media.sql`   |
| Repeatable | `R__{description}.sql`    | `R__seed_component_types.sql` |

- **Versioned migrations** run once, in order
- **Repeatable migrations** run whenever their checksum changes

---

## Module Execution Order

**CRITICAL**: Modules are executed in this fixed order to ensure dependencies are satisfied:

```
1. core           → Base tables (users, sites)
2. media          → Media, responsive_media_set
3. component_library → Components, entries (references media)
4. pagebuilder    → Pages, slots (references components)
5. product        → Product catalog
```

This order is enforced in `TenantMigrationService.java`:

```java
private static final List<String> MODULE_ORDER = List.of(
    "core", "media", "component_library", "pagebuilder", "product"
);
```

Provisioning request mapping:

- Provisioning catalog exposes `core` and `product` as selectable modules.
- Before `MODULE_ORDER` is applied, backend canonicalizes `core` selection to execution modules:
  - `core`, `media`, `component_library`, `pagebuilder`
- Optional `product` is appended when requested.

---

## Migration Rules

### ✅ DO

1. **Follow module order for foreign keys**
   - If Table A references Table B, Table B's migration must be in an earlier module
   - Example: `component_entries.responsive_id → media.responsive_media_set.id`
   - Solution: Put the FK migration in the `media` module, not `component_library`

2. **Use explicit column lists in INSERT statements**

   ```sql
   -- ✅ Correct
   INSERT INTO table (id, name, code) VALUES (1, 'Name', 'code');
   ```

3. **Keep migrations atomic and focused**
   - One logical change per migration
   - Include rollback-safe operations where possible

4. **Update seed files after schema changes**
   - When removing/adding columns, update corresponding `R__seed_*.sql` files
5. **Use forward-only repair migrations for legacy drift**
   - Do not edit old released `V*.sql`
   - Add a new `V*__repair_*.sql` with `INFORMATION_SCHEMA` guards

### ❌ DON'T

1. **No direct `IF NOT EXISTS` on `ALTER TABLE`** (MySQL support is limited/inconsistent)

   ```sql
   -- ❌ Wrong (MySQL doesn't support this for ALTER)
   ALTER TABLE pages ADD COLUMN IF NOT EXISTS robot_tag VARCHAR(50);

   -- ✅ Correct (for legacy drift): use forward-only repair migration with INFORMATION_SCHEMA guards
   -- e.g. AddColumnIfNotExists procedure in V*__repair_*.sql
   ```

2. **No cross-module foreign keys in wrong order**

   ```sql
   -- ❌ Wrong: V14 (component_library) references media table created in V23 (media)
   -- media module runs AFTER component_library!

   -- ✅ Correct: Move to media module as V25
   ALTER TABLE component_entries ADD COLUMN responsive_id BIGINT;
   ```

3. **No assumptions about column existence**
   - If a column was removed in migration VN, update all R\_\_ files that reference it
4. **Do not modify historical versioned migrations**
   - This causes checksum drift and unstable sync behavior in legacy tenants

---

## Governance

- Policy and CI guardrails: [`migration-governance.md`](migration-governance.md)

---

## Troubleshooting

### Common Errors

| Error                                     | Cause                                    | Solution                                            |
| ----------------------------------------- | ---------------------------------------- | --------------------------------------------------- |
| `Failed to open the referenced table 'X'` | FK references table from later module    | Move migration to the module that creates table X   |
| `Unknown column 'X' in field list`        | Column removed but seed file not updated | Update R\__seed_\*.sql to match current schema      |
| `Duplicate column name 'X'`               | Legacy drift / partial previous schema   | Add forward-only `V*__repair_*.sql` guard migration |

### Repair Failed Migration (Forward-Only)

```powershell
# Check migration history
docker exec -it admincraft-mysql mysql -u root -p1234 -e \
  "USE tenant_democompany_db; SELECT * FROM flyway_<module>_history ORDER BY installed_rank DESC LIMIT 5;"

# Prefer: add a new repair migration and rerun sync.
# Avoid editing old migration files.
```

---

## Testing Migrations

Before committing a new migration:

1. **Fresh database test**

   ```powershell
   # Drop and recreate tenant database
   docker exec -it admincraft-mysql mysql -u root -p1234 -e "DROP DATABASE IF EXISTS tenant_test_db; CREATE DATABASE tenant_test_db;"
   ```

2. **Run application**

   ```powershell
   mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

3. **Verify all modules migrated**

   ```powershell
   docker exec -it admincraft-mysql mysql -u root -p1234 -e \
     "USE tenant_democompany_db; SHOW TABLES LIKE 'flyway_%';"
   ```

---

## Version Bundles (Current State)

| Version Range | Module            | Notes                                                       |
| ------------- | ----------------- | ----------------------------------------------------------- |
| V1-V35        | core              | Baseline + navigation + site technical + recaptcha + repair |
| V1-V21        | component_library | Baseline + responsive links + navigation bindings + cleanup + profile simplification + `is_navigation_aware` boolean |
| V1-V35        | pagebuilder       | Baseline + templates + page type + legacy page repair + page_i18n name/canonical_url + restore description |
| V20-V24       | media             | Baseline + responsive media + link type alignment           |
| V27-V34       | product           | Baseline + responsive refactor + fields + legacy repair     |
