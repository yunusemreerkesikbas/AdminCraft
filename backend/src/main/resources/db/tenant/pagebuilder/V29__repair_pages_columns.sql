-- Repair legacy pages columns and indexes (idempotent)

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

CALL AddColumnIfNotExists(DATABASE(), 'pages', 'template_id', 'BIGINT NULL');
CALL AddColumnIfNotExists(DATABASE(), 'pages', 'robot_tag', "ENUM('INDEX_FOLLOW', 'NOINDEX_FOLLOW', 'INDEX_NOFOLLOW', 'NOINDEX_NOFOLLOW') DEFAULT 'INDEX_FOLLOW'");
CALL AddColumnIfNotExists(DATABASE(), 'pages', 'published_at', 'datetime(6)');
CALL AddColumnIfNotExists(DATABASE(), 'pages', 'scheduled_at', 'datetime(6)');
CALL AddColumnIfNotExists(DATABASE(), 'pages', 'page_type', "VARCHAR(20) DEFAULT 'CONTENT'");

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;

-- Some legacy tenants still have pages.created_by as NOT NULL,
-- while seeds insert pages without created_by.
SET @created_by_nullable = (
    SELECT IS_NULLABLE
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pages'
      AND COLUMN_NAME = 'created_by'
);
SET @ddl = IF(@created_by_nullable = 'NO',
    'ALTER TABLE pages MODIFY COLUMN created_by BIGINT NULL',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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

CALL AddIndexIfNotExists(DATABASE(), 'pages', 'idx_page_template', 'template_id');
CALL AddIndexIfNotExists(DATABASE(), 'pages', 'idx_page_type_status', 'page_type, status');

DROP PROCEDURE IF EXISTS AddIndexIfNotExists;
