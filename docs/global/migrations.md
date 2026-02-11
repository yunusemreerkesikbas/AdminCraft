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

### ❌ DON'T

1. **No idempotent DDL syntax** (MySQL doesn't fully support it)

   ```sql
   -- ❌ Wrong (MySQL doesn't support this for ALTER)
   ALTER TABLE pages ADD COLUMN IF NOT EXISTS robot_tag VARCHAR(50);

   -- ✅ Correct (Flyway tracks execution, no need for IF NOT EXISTS)
   ALTER TABLE pages ADD COLUMN robot_tag VARCHAR(50);
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

---

## Troubleshooting

### Common Errors

| Error                                     | Cause                                    | Solution                                          |
| ----------------------------------------- | ---------------------------------------- | ------------------------------------------------- |
| `Failed to open the referenced table 'X'` | FK references table from later module    | Move migration to the module that creates table X |
| `Unknown column 'X' in field list`        | Column removed but seed file not updated | Update R\__seed_\*.sql to match current schema    |
| `Duplicate column name 'X'`               | Column already exists                    | Remove ADD COLUMN or check if already migrated    |

### Repair Failed Migration

```powershell
# Check migration history
docker exec -it admincraft-mysql mysql -u root -p1234 -e \
  "USE tenant_democompany_db; SELECT * FROM flyway_<module>_history ORDER BY installed_rank DESC LIMIT 5;"

# Delete failed migration record
docker exec -it admincraft-mysql mysql -u root -p1234 -e \
  "USE tenant_democompany_db; DELETE FROM flyway_<module>_history WHERE version = 'X' AND success = 0;"
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

| Version Range | Module            | Tables Created                                                    |
| ------------- | ----------------- | ----------------------------------------------------------------- |
| V1-V2         | core              | users, sites, site_languages                                      |
| V10-V12       | component_library | components, component_types, entries                              |
| V12-V26       | pagebuilder       | pages, page_slots, slot_components                                |
| V20-V26       | media             | media, media_formats, responsive_media_set, component_media_links |
| V27-V33       | product           | products, product_types, product_attributes                       |
