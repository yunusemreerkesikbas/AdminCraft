-- Ensure page_type column exists on pages table
-- Sprint 37: Fix missing page_type column in some tenant DBs

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

        SET @ddl = CONCAT('ALTER TABLE `', REPLACE(tableName, '`', '``'), '` ADD COLUMN `', REPLACE(colName, '`', '``'), '` ', colDef);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL AddColumnIfNotExists(DATABASE(), 'pages', 'page_type', "VARCHAR(20) DEFAULT 'CONTENT' AFTER status");

-- Normalize page_type values based on template_id when available
UPDATE pages p
LEFT JOIN page_templates pt ON p.template_id = pt.id
SET p.page_type = CASE pt.uid
    WHEN 'ProductDetailsPageTemplate' THEN 'PRODUCT'
    WHEN 'CategoryPageTemplate' THEN 'CATEGORY'
    WHEN 'SearchResultsPageTemplate' THEN 'SEARCH'
    WHEN 'LandingPageTemplate' THEN 'LANDING'
    WHEN 'ErrorPageTemplate' THEN 'ERROR'
    WHEN 'NotFoundPageTemplate' THEN 'ERROR'
    ELSE COALESCE(p.page_type, 'CONTENT')
END;

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
