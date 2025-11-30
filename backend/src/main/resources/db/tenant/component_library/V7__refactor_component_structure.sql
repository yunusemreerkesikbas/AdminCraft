-- V7: Refactor Component Structure - Align with PageBuilder Pattern
-- Remove baseLocalizedData JSON, add direct columns
-- Remove code field from components
-- Update status enum to support PUBLISHED, SCHEDULED, ARCHIVED

-- Step 1: Add new direct columns to component_i18n
ALTER TABLE component_i18n
    ADD COLUMN title VARCHAR(200) AFTER language,
    ADD COLUMN subtitle VARCHAR(200) AFTER title,
    ADD COLUMN description TEXT AFTER subtitle;

-- Step 2: Migrate data from JSON to columns (if data exists)
UPDATE component_i18n
SET
    title = JSON_UNQUOTE(JSON_EXTRACT(base_localized_data, '$.title')),
    subtitle = JSON_UNQUOTE(JSON_EXTRACT(base_localized_data, '$.subtitle')),
    description = JSON_UNQUOTE(JSON_EXTRACT(base_localized_data, '$.description'))
WHERE base_localized_data IS NOT NULL;

-- Step 3: Drop old columns
ALTER TABLE component_i18n DROP COLUMN base_localized_data;
ALTER TABLE component_i18n DROP COLUMN published_at;
ALTER TABLE components DROP COLUMN code;

-- Step 4: Drop old unique constraint on code
ALTER TABLE components DROP INDEX uk_component_code;

-- Step 5: Modify status column from ENUM to VARCHAR for flexibility
ALTER TABLE components MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE component_i18n MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE component_entries MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE component_entry_i18n MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- Step 6: Migrate status values
-- Convert old status values to new ones
UPDATE components SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE components SET status = 'ARCHIVED' WHERE status = 'INACTIVE';

UPDATE component_i18n SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE component_i18n SET status = 'ARCHIVED' WHERE status = 'INACTIVE';

UPDATE component_entries SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE component_entries SET status = 'ARCHIVED' WHERE status = 'INACTIVE';

UPDATE component_entry_i18n SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE component_entry_i18n SET status = 'ARCHIVED' WHERE status = 'INACTIVE';

-- Step 6: Add indexes for performance
CREATE INDEX idx_component_i18n_title ON component_i18n(title);
CREATE INDEX idx_component_i18n_language_status ON component_i18n(language, status);
