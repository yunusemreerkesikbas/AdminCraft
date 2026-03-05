-- Forward-only repair for legacy tenants where V22 may fail due to FK/index order.
-- Safely drops FK, index, and column only if they still exist.

DROP PROCEDURE IF EXISTS DropForeignKeyIfExists;
DROP PROCEDURE IF EXISTS DropIndexIfExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;

DELIMITER //

CREATE PROCEDURE DropForeignKeyIfExists(
    IN tableName VARCHAR(100),
    IN constraintName VARCHAR(100)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = tableName
          AND CONSTRAINT_NAME = constraintName
    ) THEN
        SET @ddl = CONCAT(
            'ALTER TABLE `', REPLACE(tableName, '`', '``'),
            '` DROP FOREIGN KEY `', REPLACE(constraintName, '`', '``'), '`'
        );
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

CREATE PROCEDURE DropIndexIfExists(
    IN tableName VARCHAR(100),
    IN indexName VARCHAR(100)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = tableName
          AND INDEX_NAME = indexName
    ) THEN
        SET @ddl = CONCAT(
            'ALTER TABLE `', REPLACE(tableName, '`', '``'),
            '` DROP INDEX `', REPLACE(indexName, '`', '``'), '`'
        );
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

CREATE PROCEDURE DropColumnIfExists(
    IN tableName VARCHAR(100),
    IN columnName VARCHAR(100)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = tableName
          AND COLUMN_NAME = columnName
    ) THEN
        SET @ddl = CONCAT(
            'ALTER TABLE `', REPLACE(tableName, '`', '``'),
            '` DROP COLUMN `', REPLACE(columnName, '`', '``'), '`'
        );
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

CALL DropForeignKeyIfExists('components', 'fk_component_navigation_link_node');
CALL DropIndexIfExists('components', 'idx_component_navigation_link_node');
CALL DropColumnIfExists('components', 'navigation_link_node_id');

DROP PROCEDURE IF EXISTS DropForeignKeyIfExists;
DROP PROCEDURE IF EXISTS DropIndexIfExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;
