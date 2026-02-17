-- V35: Restore description column to page_i18n
-- V34 dropped description alongside legacy columns (subtitle, meta_title, meta_description, description_html)
-- but the PageI18n entity and application layer still reference this column.
-- Uses inline SET/PREPARE/EXECUTE guard (no DELIMITER, Flyway-safe) per migration governance rules.

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'page_i18n' AND COLUMN_NAME = 'description');
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE page_i18n ADD COLUMN description LONGTEXT NULL AFTER title',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
