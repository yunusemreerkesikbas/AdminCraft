-- V26: Add component-level custom_data for shared layout roles and future component metadata

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;

DELIMITER //

CREATE PROCEDURE AddColumnIfNotExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN colName VARCHAR(100),
    IN colDef TEXT
)
BEGIN
    IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = dbName AND TABLE_NAME = tableName AND COLUMN_NAME = colName) = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', colName, ' ', colDef);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

CALL AddColumnIfNotExists(DATABASE(), 'components', 'custom_data', 'JSON NULL AFTER style_classes');

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
