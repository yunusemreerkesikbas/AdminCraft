# Migration Fix: CMS-41 - Remove Legacy Slug Column from Pages Table

## Issue Description

**Error Message:**
```
{
    "result": "ERROR",
    "message": "Sayfa oluşturulamadı: could not execute statement [Field 'slug' doesn't have a default value] [insert into pages (...) values (...)]"
}
```

**Root Cause:**

The `pages` table in the database contains a legacy `slug` column that:
- Was created during an earlier single-language architecture version
- Is NOT in the current `Page` entity definition (`Page.java`)
- Is NOT in the current schema definition (`schema-page-builder.sql`)
- Is marked as NOT NULL without a default value
- Was not automatically dropped by Hibernate because `ddl-auto: update` mode never drops columns

According to the Multi-Language Page Builder specification (`.sprint-workflow.md`):
- The `pages` table should NOT have a `slug` field
- URL paths should be stored in the `page_i18n` table as `url_path` (language-specific)

## Solution

The fix removes the `slug` column from the `pages` table to align the database with the current architecture.

## Application Method

### Option 1: Run Standalone Migration Script (RECOMMENDED)

Execute the migration script directly on the database:

```bash
# Navigate to backend directory
cd backend

# Run the migration script using MySQL client
mysql -h localhost -P 3307 -u root -p1234 admincraft-db < src/main/resources/migrations/001_remove_slug_from_pages.sql
```

**Or using MySQL Workbench or any MySQL client:**
1. Connect to `admincraft-db` database on `localhost:3307`
2. Open and execute: `backend/src/main/resources/migrations/001_remove_slug_from_pages.sql`

### Option 2: Restart Application with 'seed' Profile

If you want the schema file to run automatically:

```bash
# Stop the current application
# Then start with seed profile
mvn spring-boot:run -Dspring-boot.run.profiles=seed
```

**Note:** The `seed` profile has `spring.sql.init.mode: always` which will execute `schema-page-builder.sql` on startup.

### Option 3: Manual SQL Execution

Connect to your MySQL database and run:

```sql
USE `admincraft-db`;

ALTER TABLE pages DROP COLUMN IF EXISTS slug;
```

## Verification

After applying the fix, verify the column is removed:

```sql
-- Check if slug column exists
SELECT COUNT(*) as column_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'admincraft-db'
  AND TABLE_NAME = 'pages'
  AND COLUMN_NAME = 'slug';

-- Should return 0
```

## Testing

After applying the migration, test page creation:

1. Start the application
2. Create a new page via the API or UI
3. Verify the page is created successfully without errors

## Files Modified

1. **`schema-page-builder.sql`** (lines 107-122)
   - Added idempotent migration logic to drop `slug` column if it exists

2. **`migrations/001_remove_slug_from_pages.sql`** (NEW)
   - Standalone migration script for manual execution

## Architecture Alignment

After this fix, the system correctly follows the Multi-Language Page Builder architecture:

### pages table (language-agnostic):
- id, uuid, uid, tenant_id, category_id, status, featured_image, style_classes, is_home, sort_order, timestamps
- **NO slug field** ✓

### page_i18n table (per language):
- id, uuid, uid, page_id, tenant_id, language, **url_path**, title, subtitle, meta_title, meta_description, description, description_html, status, published_at, scheduled_at, updated_at
- **url_path field stores language-specific URLs** ✓

## Prevention

This issue occurred because:
- Hibernate's `ddl-auto: update` mode never drops columns
- Entity changes (removing fields) don't automatically update the database schema

**Best Practices:**
1. Use proper database migration tools (Flyway/Liquibase) for schema changes
2. Document all schema modifications
3. Review database schema periodically to ensure alignment with entities
4. Consider using `ddl-auto: validate` in production to catch schema mismatches early

## Related Files

- Entity: `backend/src/main/java/com/backend/domain/entity/Page.java`
- Entity: `backend/src/main/java/com/backend/domain/entity/PageI18n.java`
- Schema: `backend/src/main/resources/schema-page-builder.sql`
- Service: `backend/src/main/java/com/backend/application/service/PageServiceImpl.java`
