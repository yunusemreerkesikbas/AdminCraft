-- Sprint 25: CMS Delivery API - Add custom_data JSON column
-- This column stores dynamic custom fields for component entries per language

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

CALL AddColumnIfNotExists(DATABASE(), 'component_entry_i18n', 'custom_data', 'JSON DEFAULT NULL');

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;

-- Add index for JSON queries if needed in the future
-- CREATE INDEX idx_entry_i18n_custom_data ON component_entry_i18n ((CAST(custom_data AS CHAR(512))));

