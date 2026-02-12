-- Add reCAPTCHA columns to sites table (guarded for legacy tenant DBs)
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

CALL AddColumnIfNotExists(DATABASE(), 'sites', 'recaptcha_enabled', 'BOOLEAN DEFAULT FALSE');
CALL AddColumnIfNotExists(DATABASE(), 'sites', 'recaptcha_site_key', 'VARCHAR(255)');
CALL AddColumnIfNotExists(DATABASE(), 'sites', 'recaptcha_secret_key_encrypted', 'TEXT');
CALL AddColumnIfNotExists(DATABASE(), 'sites', 'recaptcha_threshold', 'DECIMAL(3,2) DEFAULT 0.5');

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;

-- Add index for performance (guarded)
DROP PROCEDURE IF EXISTS AddIndexIfNotExists;
DELIMITER //
CREATE PROCEDURE AddIndexIfNotExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN indexName VARCHAR(100),
    IN indexDef TEXT
)
BEGIN
    IF (SELECT count(*) FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = dbName
        AND TABLE_NAME = tableName
        AND INDEX_NAME = indexName) = 0 THEN

        SET @ddl = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, ' (', indexDef, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL AddIndexIfNotExists(DATABASE(), 'sites', 'idx_sites_recaptcha_enabled', 'recaptcha_enabled');

DROP PROCEDURE IF EXISTS AddIndexIfNotExists;
