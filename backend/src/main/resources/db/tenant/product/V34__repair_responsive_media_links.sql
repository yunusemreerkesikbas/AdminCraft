-- Repair responsive media links for product module (idempotent)

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

CALL AddColumnIfNotExists(DATABASE(), 'responsive_media_set', 'temp_pm_id', 'BIGINT');
CALL AddColumnIfNotExists(DATABASE(), 'product_media', 'responsive_media_set_id', 'BIGINT');

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;

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

CALL AddIndexIfNotExists(DATABASE(), 'responsive_media_set', 'idx_temp_pm', 'temp_pm_id');
CALL AddIndexIfNotExists(DATABASE(), 'product_media', 'idx_pm_responsive_set', 'responsive_media_set_id');

DROP PROCEDURE IF EXISTS AddIndexIfNotExists;

-- FK guard
SET @fk_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_media'
      AND CONSTRAINT_NAME = 'fk_product_media_responsive_set'
);
SET @ddl = IF(@fk_exists = 0,
    'ALTER TABLE product_media ADD CONSTRAINT fk_product_media_responsive_set FOREIGN KEY (responsive_media_set_id) REFERENCES responsive_media_set(id) ON DELETE SET NULL',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Unique guard
SET @idx_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_media'
      AND INDEX_NAME = 'uk_product_media'
);
SET @ddl = IF(@idx_exists = 0,
    'ALTER TABLE product_media ADD CONSTRAINT uk_product_media UNIQUE (product_id, responsive_media_set_id)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
