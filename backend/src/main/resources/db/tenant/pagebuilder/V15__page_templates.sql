-- Sprint 27: Page Template System
-- Creates page_templates and template_slots tables for reusable page layouts

CREATE TABLE IF NOT EXISTS page_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    is_system BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_page_template_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS template_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    template_id BIGINT NOT NULL,
    slot_name VARCHAR(50) NOT NULL,
    position VARCHAR(20) NOT NULL,
    sort_order INT DEFAULT 0,
    is_required BOOLEAN DEFAULT FALSE,
    max_components INT NULL,
    allowed_types JSON NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_template_slot (template_id, slot_name),
    INDEX idx_template_slot_template (template_id),
    FOREIGN KEY (template_id) REFERENCES page_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

DROP PROCEDURE IF EXISTS DropColumnIfExists;
DELIMITER //
CREATE PROCEDURE DropColumnIfExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN colName VARCHAR(100)
)
BEGIN
    IF (SELECT count(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = dbName
        AND TABLE_NAME = tableName
        AND COLUMN_NAME = colName) = 1 THEN

        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' DROP COLUMN ', colName);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

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

DROP PROCEDURE IF EXISTS AddForeignKeyIfNotExists;
DELIMITER //
CREATE PROCEDURE AddForeignKeyIfNotExists(
    IN dbName VARCHAR(100),
    IN tableName VARCHAR(100),
    IN constraintName VARCHAR(100),
    IN fkDef TEXT
)
BEGIN
    IF (SELECT count(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = dbName
        AND TABLE_NAME = tableName
        AND CONSTRAINT_NAME = constraintName
        AND CONSTRAINT_TYPE = 'FOREIGN KEY') = 0 THEN

        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' ADD CONSTRAINT ', constraintName, ' ', fkDef);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL AddColumnIfNotExists(DATABASE(), 'page_templates', 'created_by', 'BIGINT NULL');
CALL AddColumnIfNotExists(DATABASE(), 'page_templates', 'updated_by', 'BIGINT NULL');
CALL DropColumnIfExists(DATABASE(), 'page_templates', 'preview_image');

CALL AddColumnIfNotExists(DATABASE(), 'template_slots', 'uid', 'VARCHAR(50) NOT NULL UNIQUE AFTER uuid');
CALL AddColumnIfNotExists(DATABASE(), 'template_slots', 'updated_at', 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL AddColumnIfNotExists(DATABASE(), 'template_slots', 'created_by', 'BIGINT NULL');
CALL AddColumnIfNotExists(DATABASE(), 'template_slots', 'updated_by', 'BIGINT NULL');

CALL AddColumnIfNotExists(DATABASE(), 'pages', 'template_id', 'BIGINT NULL AFTER category_id');
CALL AddIndexIfNotExists(DATABASE(), 'pages', 'idx_page_template', 'template_id');
CALL AddForeignKeyIfNotExists(DATABASE(), 'pages', 'fk_page_template',
    'FOREIGN KEY (template_id) REFERENCES page_templates(id) ON DELETE SET NULL');

CALL DropColumnIfExists(DATABASE(), 'pages', 'template_uid');

DROP PROCEDURE IF EXISTS AddForeignKeyIfNotExists;
DROP PROCEDURE IF EXISTS AddIndexIfNotExists;
DROP PROCEDURE IF EXISTS DropColumnIfExists;
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
