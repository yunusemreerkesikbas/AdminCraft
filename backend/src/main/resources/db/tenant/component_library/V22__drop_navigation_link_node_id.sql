-- V22: Drop navigation_link_node_id column and its FK/index.
-- FK must be dropped before the index; MySQL refuses to drop an index
-- backing an active foreign key constraint.

DROP PROCEDURE IF EXISTS DropForeignKeyIfExists;
DROP PROCEDURE IF EXISTS DropIndexIfExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;

DELIMITER //

CREATE PROCEDURE DropForeignKeyIfExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN constraintName VARCHAR(100)
)
BEGIN
    IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
        WHERE TABLE_SCHEMA = dbName AND TABLE_NAME = tableName AND CONSTRAINT_NAME = constraintName) > 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' DROP FOREIGN KEY ', constraintName);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

CREATE PROCEDURE DropIndexIfExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN indexName VARCHAR(100)
)
BEGIN
    IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = dbName AND TABLE_NAME = tableName AND INDEX_NAME = indexName) > 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' DROP INDEX ', indexName);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

CREATE PROCEDURE DropColumnIfExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN columnName VARCHAR(100)
)
BEGIN
    IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = dbName AND TABLE_NAME = tableName AND COLUMN_NAME = columnName) > 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' DROP COLUMN ', columnName);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

CALL DropForeignKeyIfExists(DATABASE(), 'components', 'fk_component_navigation_link_node');
CALL DropIndexIfExists(DATABASE(), 'components', 'idx_component_navigation_link_node');
CALL DropColumnIfExists(DATABASE(), 'components', 'navigation_link_node_id');

DROP PROCEDURE IF EXISTS DropForeignKeyIfExists;
DROP PROCEDURE IF EXISTS DropIndexIfExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;
