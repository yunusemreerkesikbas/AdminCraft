-- V34: Forward-only repair for page_i18n name/canonical_url (aligns with V26 design).
-- Idempotent: safe for tenants that ran V26 no-op or had manual schema drift.
-- GOVERNANCE EXCEPTION: Dynamic DDL via stored procedures is intentional here.
-- Cross-tenant idempotency requires INFORMATION_SCHEMA checks; this is a repair migration
-- targeting schema drift across existing tenants. Standard straight DDL would fail on tenants
-- where columns already exist or have already been renamed.

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;

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

CREATE PROCEDURE DropColumnIfExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN colName VARCHAR(100)
)
BEGIN
    IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = dbName AND TABLE_NAME = tableName AND COLUMN_NAME = colName) > 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' DROP COLUMN ', colName);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL AddColumnIfNotExists(DATABASE(), 'page_i18n', 'name', 'VARCHAR(200) NULL AFTER title');

SET @has_url_path = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'page_i18n' AND COLUMN_NAME = 'url_path');
SET @ddl = IF(@has_url_path > 0,
    'ALTER TABLE page_i18n CHANGE COLUMN url_path canonical_url VARCHAR(255) NULL',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CALL DropColumnIfExists(DATABASE(), 'page_i18n', 'subtitle');
CALL DropColumnIfExists(DATABASE(), 'page_i18n', 'meta_title');
CALL DropColumnIfExists(DATABASE(), 'page_i18n', 'meta_description');
CALL DropColumnIfExists(DATABASE(), 'page_i18n', 'description');
CALL DropColumnIfExists(DATABASE(), 'page_i18n', 'description_html');

SET @has_idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pages' AND INDEX_NAME = 'idx_page_sort');
SET @ddl = IF(@has_idx > 0, 'ALTER TABLE pages DROP INDEX idx_page_sort', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CALL DropColumnIfExists(DATABASE(), 'pages', 'featured_image');
CALL DropColumnIfExists(DATABASE(), 'pages', 'sort_order');

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;
