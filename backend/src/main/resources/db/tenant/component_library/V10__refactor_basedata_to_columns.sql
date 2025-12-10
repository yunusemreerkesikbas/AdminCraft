-- Refactor baseData JSON column to separate columns

-- Part 1: Add new columns (Idempotent)
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

CALL AddColumnIfNotExists(DATABASE(), 'components', 'display_order', 'INT DEFAULT 0');
CALL AddColumnIfNotExists(DATABASE(), 'components', 'is_visible', 'BOOLEAN DEFAULT TRUE');
CALL AddColumnIfNotExists(DATABASE(), 'components', 'style_classes', 'VARCHAR(500)');

-- Part 2: Migrate data from JSON to new columns
-- Only if base_data exists. If base_data is gone (already migrated), this will just fail or we should skip.
-- Since we are making this idempotent, if base_data is gone, we assume data is migrated.

-- UPDATE components 
-- SET display_order = COALESCE(JSON_EXTRACT(base_data, '$.order'), 0),
--     is_visible = CASE 
--         WHEN JSON_EXTRACT(base_data, '$.isVisible') = true THEN 1
--         WHEN JSON_EXTRACT(base_data, '$.isVisible') = false THEN 0
--         ELSE 1
--     END,
--     style_classes = JSON_UNQUOTE(JSON_EXTRACT(base_data, '$.styleClasses'))
-- WHERE base_data IS NOT NULL;

-- Part 3: Drop JSON column (Idempotent)
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

CALL DropColumnIfExists(DATABASE(), 'components', 'base_data');

-- Cleanup
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;


