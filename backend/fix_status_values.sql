-- Manual fix for existing status values in tenant database
-- Run this on your tenant database: tenant_democompany_db

USE tenant_democompany_db;

-- Step 1: Drop code column from components table
-- First drop the index, then the column
SET @drop_index = IF(
    EXISTS(SELECT 1 FROM information_schema.statistics
           WHERE table_schema = 'tenant_democompany_db'
           AND table_name = 'components'
           AND index_name = 'uk_component_code'),
    'ALTER TABLE components DROP INDEX uk_component_code',
    'SELECT "Index does not exist"'
);
PREPARE stmt FROM @drop_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_column = IF(
    EXISTS(SELECT 1 FROM information_schema.columns
           WHERE table_schema = 'tenant_democompany_db'
           AND table_name = 'components'
           AND column_name = 'code'),
    'ALTER TABLE components DROP COLUMN code',
    'SELECT "Column does not exist"'
);
PREPARE stmt FROM @drop_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 2: Add missing columns to component_i18n (from V7 migration)
SET @add_title = IF(
    NOT EXISTS(SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'tenant_democompany_db'
               AND table_name = 'component_i18n'
               AND column_name = 'title'),
    'ALTER TABLE component_i18n ADD COLUMN title VARCHAR(200) AFTER language',
    'SELECT "Column already exists"'
);
PREPARE stmt FROM @add_title;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_subtitle = IF(
    NOT EXISTS(SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'tenant_democompany_db'
               AND table_name = 'component_i18n'
               AND column_name = 'subtitle'),
    'ALTER TABLE component_i18n ADD COLUMN subtitle VARCHAR(200) AFTER title',
    'SELECT "Column already exists"'
);
PREPARE stmt FROM @add_subtitle;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_description = IF(
    NOT EXISTS(SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'tenant_democompany_db'
               AND table_name = 'component_i18n'
               AND column_name = 'description'),
    'ALTER TABLE component_i18n ADD COLUMN description TEXT AFTER subtitle',
    'SELECT "Column already exists"'
);
PREPARE stmt FROM @add_description;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 3: Drop old base_localized_data column if it exists
SET @drop_base_data = IF(
    EXISTS(SELECT 1 FROM information_schema.columns
           WHERE table_schema = 'tenant_democompany_db'
           AND table_name = 'component_i18n'
           AND column_name = 'base_localized_data'),
    'ALTER TABLE component_i18n DROP COLUMN base_localized_data',
    'SELECT "Column does not exist"'
);
PREPARE stmt FROM @drop_base_data;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 4: Convert status column from ENUM to VARCHAR
ALTER TABLE components MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE component_i18n MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE component_entries MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE component_entry_i18n MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- Step 5: Update status values
UPDATE components SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE components SET status = 'ARCHIVED' WHERE status = 'INACTIVE';

UPDATE component_i18n SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE component_i18n SET status = 'ARCHIVED' WHERE status = 'INACTIVE';

UPDATE component_entries SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE component_entries SET status = 'ARCHIVED' WHERE status = 'INACTIVE';

UPDATE component_entry_i18n SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE component_entry_i18n SET status = 'ARCHIVED' WHERE status = 'INACTIVE';

-- Verify the updates
SELECT 'components' as table_name, status, COUNT(*) as count FROM components GROUP BY status
UNION ALL
SELECT 'component_i18n', status, COUNT(*) FROM component_i18n GROUP BY status
UNION ALL
SELECT 'component_entries', status, COUNT(*) FROM component_entries GROUP BY status
UNION ALL
SELECT 'component_entry_i18n', status, COUNT(*) FROM component_entry_i18n GROUP BY status;
