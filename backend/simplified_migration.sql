-- Simplified Migration for Page Categories i18n

-- Step 1: Backfill uuid, uid, created_at, updated_at
UPDATE page_categories 
SET uuid = UUID() 
WHERE uuid IS NULL OR uuid = '';

UPDATE page_categories 
SET uid = CONCAT('cat_', LPAD(id, 8, '0'))
WHERE uid IS NULL OR uid = '';

UPDATE page_categories 
SET created_at = NOW() 
WHERE created_at IS NULL;

UPDATE page_categories 
SET updated_at = NOW() 
WHERE updated_at IS NULL;

-- Step 2: Migrate existing data to page_category_i18n (only if not already migrated)
INSERT IGNORE INTO page_category_i18n (uuid, uid, tenant_id, category_id, language, url, title, active, updated_at)
SELECT 
    UUID() as uuid,
    CONCAT('catitem_', LPAD(pc.id, 8, '0')) as uid,
    pc.tenant_id,
    pc.id as category_id,
    'TR' as language,
    pc.slug as url,
    pc.name as title,
    CASE WHEN pc.status = 'ACTIVE' THEN TRUE ELSE FALSE END as active,
    NOW() as updated_at
FROM page_categories pc;

-- Step 3: Drop old constraints and indexes first
ALTER TABLE page_categories DROP INDEX uk_page_category_slug_tenant;
ALTER TABLE page_categories DROP INDEX idx_page_category_path;

-- Step 4: Drop old language-specific columns
ALTER TABLE page_categories 
DROP COLUMN name,
DROP COLUMN slug,
DROP COLUMN path,
DROP COLUMN level,
DROP COLUMN status;

-- Step 4: Drop old page_category_translations table if exists
DROP TABLE IF EXISTS page_category_translations;

