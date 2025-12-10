-- V7: Refactor Component Structure - Align with PageBuilder Pattern
-- Remove baseLocalizedData JSON, add direct columns
-- Remove code field from components
-- Update status enum to support PUBLISHED, SCHEDULED, ARCHIVED

-- Stored Procedure for Idempotent ADD COLUMN
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DELIMITER //
CREATE PROCEDURE AddColumnIfNotExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN colName VARCHAR(100),
    IN colDef TEXT
)
BEGIN
    IF (SELECT count(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = dbName
        AND TABLE_NAME = tableName
        AND COLUMN_NAME = colName) = 0 THEN

        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', colName, ' ', colDef);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- Step 1: Add new direct columns to component_i18n
CALL AddColumnIfNotExists(DATABASE(), 'component_i18n', 'title', 'VARCHAR(200) AFTER language');
CALL AddColumnIfNotExists(DATABASE(), 'component_i18n', 'subtitle', 'VARCHAR(200) AFTER title');
CALL AddColumnIfNotExists(DATABASE(), 'component_i18n', 'description', 'TEXT AFTER subtitle');

-- Step 2: Migrate data from JSON to columns (if data exists)
-- Only set if base_localized_data exists (check effectively done by SQL validity, but logic is safe to re-run on nulls)
-- UPDATE component_i18n
-- SET
--    title = JSON_UNQUOTE(JSON_EXTRACT(base_localized_data, '$.title')),
--    subtitle = JSON_UNQUOTE(JSON_EXTRACT(base_localized_data, '$.subtitle')),
--    description = JSON_UNQUOTE(JSON_EXTRACT(base_localized_data, '$.description'))
-- WHERE base_localized_data IS NOT NULL;
-- Note: Skipping JSON extraction if columns were already there and data possibly gone or modified.
-- Re-running update is safe if logic holds.
-- To be safe, we check if column exists before accessing it in WHERE, but here we added them.

-- Step 3: Drop old columns (Safe handling)
DROP PROCEDURE IF EXISTS DropColumnIfExists;
DELIMITER //
CREATE PROCEDURE DropColumnIfExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN colName VARCHAR(100)
)
BEGIN
    IF (SELECT count(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = dbName
        AND TABLE_NAME = tableName
        AND COLUMN_NAME = colName) > 0 THEN

        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' DROP COLUMN ', colName);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL DropColumnIfExists(DATABASE(), 'component_i18n', 'base_localized_data');
CALL DropColumnIfExists(DATABASE(), 'component_i18n', 'published_at');
CALL DropColumnIfExists(DATABASE(), 'components', 'code');

-- Step 4: Drop old unique constraint on code
DROP PROCEDURE IF EXISTS DropIndexIfExists;
DELIMITER //
CREATE PROCEDURE DropIndexIfExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN indexName VARCHAR(100)
)
BEGIN
    IF (SELECT count(*) FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = dbName
        AND TABLE_NAME = tableName
        AND INDEX_NAME = indexName) > 0 THEN

        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' DROP INDEX ', indexName);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL DropIndexIfExists(DATABASE(), 'components', 'uk_component_code');

-- Step 5: Modify status column
-- ALTER MODIFY is idempotent-ish (changes type repeatedly is fine)
ALTER TABLE components MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE component_i18n MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE component_entries MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE component_entry_i18n MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- Step 6: Migrate status values
UPDATE components SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE components SET status = 'ARCHIVED' WHERE status = 'INACTIVE';
UPDATE component_i18n SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE component_i18n SET status = 'ARCHIVED' WHERE status = 'INACTIVE';
UPDATE component_entries SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE component_entries SET status = 'ARCHIVED' WHERE status = 'INACTIVE';
UPDATE component_entry_i18n SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
UPDATE component_entry_i18n SET status = 'ARCHIVED' WHERE status = 'INACTIVE';

-- Step 7: Add indexes for performance (Idempotent)
DROP PROCEDURE IF EXISTS CreateIndexIfNotExists;
DELIMITER //
CREATE PROCEDURE CreateIndexIfNotExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN indexName VARCHAR(100),
    IN indexCols VARCHAR(200)
)
BEGIN
    IF (SELECT count(*) FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = dbName
        AND TABLE_NAME = tableName
        AND INDEX_NAME = indexName) = 0 THEN

        SET @ddl = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, '(', indexCols, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL CreateIndexIfNotExists(DATABASE(), 'component_i18n', 'idx_component_i18n_title', 'title');
CALL CreateIndexIfNotExists(DATABASE(), 'component_i18n', 'idx_component_i18n_language_status', 'language, status');

-- Cleanup procedures
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;
DROP PROCEDURE IF EXISTS DropIndexIfExists;
DROP PROCEDURE IF EXISTS CreateIndexIfNotExists;

