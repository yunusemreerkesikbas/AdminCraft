-- Add missing audit fields to i18n tables required by BaseI18nEntity

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

-- page_i18n
CALL AddColumnIfNotExists(DATABASE(), 'page_i18n', 'created_at', 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP');
CALL AddColumnIfNotExists(DATABASE(), 'page_i18n', 'created_by', 'BIGINT NULL');
CALL AddColumnIfNotExists(DATABASE(), 'page_i18n', 'updated_by', 'BIGINT NULL');

-- page_category_i18n
CALL AddColumnIfNotExists(DATABASE(), 'page_category_i18n', 'created_at', 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP');
CALL AddColumnIfNotExists(DATABASE(), 'page_category_i18n', 'created_by', 'BIGINT NULL');
CALL AddColumnIfNotExists(DATABASE(), 'page_category_i18n', 'updated_by', 'BIGINT NULL');

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
