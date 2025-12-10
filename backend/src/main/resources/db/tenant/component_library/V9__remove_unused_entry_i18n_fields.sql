-- Remove unused button/image fields from component_entry_i18n
-- These fields exist in DB but are not used by frontend

-- Idempotent DROP COLUMN procedure
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

CALL DropColumnIfExists(DATABASE(), 'component_entry_i18n', 'image_url');
CALL DropColumnIfExists(DATABASE(), 'component_entry_i18n', 'button_text');
CALL DropColumnIfExists(DATABASE(), 'component_entry_i18n', 'button_url');

DROP PROCEDURE IF EXISTS DropColumnIfExists;

