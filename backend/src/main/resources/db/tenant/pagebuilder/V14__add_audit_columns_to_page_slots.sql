-- Add missing audit columns to page_slots and slot_components tables
-- Sprint 26: Fix BaseEntity column mismatch

-- Reuse idempotent column addition procedure
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

-- Add audit columns to page_slots
CALL AddColumnIfNotExists(DATABASE(), 'page_slots', 'created_by', "BIGINT NULL COMMENT 'User ID who created this record'");
CALL AddColumnIfNotExists(DATABASE(), 'page_slots', 'updated_by', "BIGINT NULL COMMENT 'User ID who last updated this record'");

-- Add updated_at to slot_components for consistency
CALL AddColumnIfNotExists(DATABASE(), 'slot_components', 'updated_at', "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
