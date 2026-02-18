-- V20: Simplify component type navigation capabilities into a single profile enum-like column.
-- Idempotent and drift-safe via INFORMATION_SCHEMA guards.

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS ExecuteIfColumnExists;
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

CREATE PROCEDURE ExecuteIfColumnExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN colName VARCHAR(100),
    IN sqlText TEXT
)
BEGIN
    IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = dbName AND TABLE_NAME = tableName AND COLUMN_NAME = colName) > 0 THEN
        SET @ddl = sqlText;
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

CALL AddColumnIfNotExists(
    DATABASE(),
    'component_types',
    'navigation_profile',
    'VARCHAR(40) NOT NULL DEFAULT ''NONE'' AFTER category'
);

-- Precedence (low -> high): NODE -> NODE_REQUIRED -> NODE_WITH_TYPE -> NODE_WITH_LINK -> CATEGORY
-- This mirrors previous boolean-combination behavior.
CALL ExecuteIfColumnExists(
    DATABASE(),
    'component_types',
    'supports_navigation_node',
    'UPDATE component_types
       SET navigation_profile = ''NODE''
     WHERE supports_navigation_node = TRUE
       AND (navigation_profile IS NULL OR navigation_profile = ''NONE'')'
);

CALL ExecuteIfColumnExists(
    DATABASE(),
    'component_types',
    'requires_navigation_node',
    'UPDATE component_types
       SET navigation_profile = ''NODE_REQUIRED''
     WHERE requires_navigation_node = TRUE'
);

CALL ExecuteIfColumnExists(
    DATABASE(),
    'component_types',
    'supports_navigation_type',
    'UPDATE component_types
       SET navigation_profile = ''NODE_WITH_TYPE''
     WHERE supports_navigation_type = TRUE'
);

CALL ExecuteIfColumnExists(
    DATABASE(),
    'component_types',
    'supports_navigation_link_node',
    'UPDATE component_types
       SET navigation_profile = ''NODE_WITH_LINK''
     WHERE supports_navigation_link_node = TRUE'
);

CALL ExecuteIfColumnExists(
    DATABASE(),
    'component_types',
    'supports_search_box',
    'UPDATE component_types
       SET navigation_profile = ''CATEGORY''
     WHERE supports_search_box = TRUE'
);

CALL DropColumnIfExists(DATABASE(), 'component_types', 'supports_navigation_node');
CALL DropColumnIfExists(DATABASE(), 'component_types', 'requires_navigation_node');
CALL DropColumnIfExists(DATABASE(), 'component_types', 'supports_navigation_link_node');
CALL DropColumnIfExists(DATABASE(), 'component_types', 'supports_navigation_type');
CALL DropColumnIfExists(DATABASE(), 'component_types', 'supports_search_box');

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS ExecuteIfColumnExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;
