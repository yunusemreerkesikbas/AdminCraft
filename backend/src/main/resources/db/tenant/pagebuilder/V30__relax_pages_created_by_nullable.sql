-- Ensure pages.created_by is nullable for legacy tenants.
-- Some repeatable seeds insert pages without created_by.

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
