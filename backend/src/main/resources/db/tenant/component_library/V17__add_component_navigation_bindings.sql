-- V17: Add navigation binding fields for CategoryNavigationComponent
-- Stores component-level links to navigation root nodes used in CMS delivery.
-- Idempotent: uses procedure-based existence checks (MySQL lacks ADD COLUMN IF NOT EXISTS).

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS AddIndexIfNotExists;
DROP PROCEDURE IF EXISTS AddForeignKeyIfNotExists;

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

CREATE PROCEDURE AddIndexIfNotExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN indexName VARCHAR(100),
    IN colName VARCHAR(100)
)
BEGIN
    IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = dbName AND TABLE_NAME = tableName AND INDEX_NAME = indexName) = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' ADD INDEX ', indexName, ' (', colName, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

CREATE PROCEDURE AddForeignKeyIfNotExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN constraintName VARCHAR(100),
    IN ddlStatement TEXT
)
BEGIN
    IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
        WHERE TABLE_SCHEMA = dbName AND TABLE_NAME = tableName AND CONSTRAINT_NAME = constraintName) = 0 THEN
        SET @ddl = ddlStatement;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

CALL AddColumnIfNotExists(DATABASE(), 'components', 'navigation_node_id', 'BIGINT NULL AFTER responsive_id');
CALL AddColumnIfNotExists(DATABASE(), 'components', 'navigation_link_node_id', 'BIGINT NULL AFTER navigation_node_id');
CALL AddColumnIfNotExists(DATABASE(), 'components', 'navigation_type', 'VARCHAR(50) NULL AFTER navigation_link_node_id');
CALL AddColumnIfNotExists(DATABASE(), 'components', 'search_box', 'BOOLEAN NULL AFTER navigation_type');
CALL AddColumnIfNotExists(DATABASE(), 'components', 'wrap_after', 'INT NULL AFTER search_box');

CALL AddIndexIfNotExists(DATABASE(), 'components', 'idx_component_navigation_node', 'navigation_node_id');
CALL AddIndexIfNotExists(DATABASE(), 'components', 'idx_component_navigation_link_node', 'navigation_link_node_id');

CALL AddForeignKeyIfNotExists(DATABASE(), 'components', 'fk_component_navigation_node',
    'ALTER TABLE components ADD CONSTRAINT fk_component_navigation_node FOREIGN KEY (navigation_node_id) REFERENCES navigation_nodes(id) ON DELETE SET NULL');
CALL AddForeignKeyIfNotExists(DATABASE(), 'components', 'fk_component_navigation_link_node',
    'ALTER TABLE components ADD CONSTRAINT fk_component_navigation_link_node FOREIGN KEY (navigation_link_node_id) REFERENCES navigation_nodes(id) ON DELETE SET NULL');

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS AddIndexIfNotExists;
DROP PROCEDURE IF EXISTS AddForeignKeyIfNotExists;
