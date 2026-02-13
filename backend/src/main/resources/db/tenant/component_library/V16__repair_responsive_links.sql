-- Repair responsive link columns and component_media_links table (idempotent)

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

CALL AddColumnIfNotExists(DATABASE(), 'components', 'responsive_id', 'BIGINT NULL');
CALL AddColumnIfNotExists(DATABASE(), 'component_entries', 'responsive_id', 'BIGINT NULL');

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

CALL AddIndexIfNotExists(DATABASE(), 'components', 'idx_component_responsive', 'responsive_id');
CALL AddIndexIfNotExists(DATABASE(), 'component_entries', 'idx_entry_responsive', 'responsive_id');

DROP PROCEDURE IF EXISTS AddIndexIfNotExists;

-- FK guards
SET @fk_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'components'
      AND CONSTRAINT_NAME = 'fk_component_responsive'
);
SET @ddl = IF(@fk_exists = 0,
    'ALTER TABLE components ADD CONSTRAINT fk_component_responsive FOREIGN KEY (responsive_id) REFERENCES responsive_media_set(id) ON DELETE SET NULL',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'component_entries'
      AND CONSTRAINT_NAME = 'fk_entry_responsive'
);
SET @ddl = IF(@fk_exists = 0,
    'ALTER TABLE component_entries ADD CONSTRAINT fk_entry_responsive FOREIGN KEY (responsive_id) REFERENCES responsive_media_set(id) ON DELETE SET NULL',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- component_media_links table
DROP PROCEDURE IF EXISTS CreateTableIfNotExists;
DELIMITER //
CREATE PROCEDURE CreateTableIfNotExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN tableDef TEXT
)
BEGIN
    IF (SELECT count(*) FROM INFORMATION_SCHEMA.TABLES
        WHERE TABLE_SCHEMA = dbName
        AND TABLE_NAME = tableName) = 0 THEN

        SET @ddl = CONCAT('CREATE TABLE ', tableName, ' ', tableDef);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL CreateTableIfNotExists(DATABASE(), 'component_media_links', '(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    component_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    link_type VARCHAR(20) NOT NULL DEFAULT ''ENTRY_MEDIA'',
    entry_id BIGINT NULL,
    responsive_set_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cml_component
        FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE,
    CONSTRAINT fk_cml_media
        FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE,
    CONSTRAINT fk_cml_entry
        FOREIGN KEY (entry_id) REFERENCES component_entries(id) ON DELETE CASCADE,
    CONSTRAINT fk_cml_responsive_set
        FOREIGN KEY (responsive_set_id) REFERENCES responsive_media_set(id) ON DELETE CASCADE,
    INDEX idx_cml_media (media_id),
    INDEX idx_cml_component (component_id),
    INDEX idx_cml_entry (entry_id),
    INDEX idx_cml_responsive_set (responsive_set_id),
    UNIQUE KEY uk_cml_component_media_type (component_id, media_id, link_type, entry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci');

DROP PROCEDURE IF EXISTS CreateTableIfNotExists;
