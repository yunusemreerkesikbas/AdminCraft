-- V16: Remove page_categories tables (Sprint 31)
-- PageTemplate system (Sprint 27) replaces the need for page categories

-- Stored Procedure for Idempotent DROP FOREIGN KEY by name or by column
DROP PROCEDURE IF EXISTS DropForeignKeyIfExists;
DELIMITER //
CREATE PROCEDURE DropForeignKeyIfExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN fkName VARCHAR(100)
)
BEGIN
    DECLARE actualFkName VARCHAR(100);
    
    SELECT CONSTRAINT_NAME INTO actualFkName
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = dbName
        AND TABLE_NAME = tableName
        AND CONSTRAINT_NAME = fkName
        AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1;
    
    IF actualFkName IS NOT NULL THEN
        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' DROP FOREIGN KEY ', actualFkName);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- Stored Procedure to drop FK by column name (fallback)
DROP PROCEDURE IF EXISTS DropForeignKeyByColumn;
DELIMITER //
CREATE PROCEDURE DropForeignKeyByColumn(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN columnName VARCHAR(100)
)
BEGIN
    DECLARE actualFkName VARCHAR(100);
    
    SELECT CONSTRAINT_NAME INTO actualFkName
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = dbName
        AND TABLE_NAME = tableName
        AND COLUMN_NAME = columnName
        AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1;
    
    IF actualFkName IS NOT NULL THEN
        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' DROP FOREIGN KEY ', actualFkName);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- Stored Procedure for Idempotent DROP INDEX
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

-- Stored Procedure for Idempotent DROP COLUMN
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

-- Step 1: Remove FK constraint from pages table (must be done first)
-- Try by name first, then by column name as fallback
CALL DropForeignKeyIfExists(DATABASE(), 'pages', 'fk_page_category');
CALL DropForeignKeyByColumn(DATABASE(), 'pages', 'category_id');

-- Step 2: Remove index from pages table (only if it still exists after FK drop)
-- This handles the case where index was created separately, not as part of FK
CALL DropIndexIfExists(DATABASE(), 'pages', 'idx_page_category');

-- Step 3: Remove category_id column from pages table
CALL DropColumnIfExists(DATABASE(), 'pages', 'category_id');

-- Step 4: Drop page_category_i18n table (has FK to page_categories, must be dropped first)
DROP TABLE IF EXISTS page_category_i18n;

-- Step 5: Drop page_categories table
DROP TABLE IF EXISTS page_categories;

-- Cleanup stored procedures
DROP PROCEDURE IF EXISTS DropForeignKeyIfExists;
DROP PROCEDURE IF EXISTS DropForeignKeyByColumn;
DROP PROCEDURE IF EXISTS DropIndexIfExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;
