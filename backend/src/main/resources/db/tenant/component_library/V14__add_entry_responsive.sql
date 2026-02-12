-- =====================================================
-- V14: Add Responsive Media to Component Entries
-- Sprint 37: Entry-Level Media Integration
-- =====================================================

-- Add responsive_id to component_entries table
-- This links an entry to its Desktop/Mobile image pair
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

CALL AddColumnIfNotExists(DATABASE(), 'component_entries', 'responsive_id', 'BIGINT NULL AFTER style_classes');

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;

-- Add FK + index (guarded)
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

CALL AddIndexIfNotExists(DATABASE(), 'component_entries', 'idx_entry_responsive', 'responsive_id');

DROP PROCEDURE IF EXISTS AddIndexIfNotExists;

-- FK (guarded by existence check on INFORMATION_SCHEMA)
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
