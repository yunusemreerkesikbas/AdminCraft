-- ========================================================================
-- Migration: Remove legacy 'slug' column from pages table
-- Issue: CMS-41 - Page creation failing with "Field 'slug' doesn't have a default value"
-- Date: 2025-10-05
-- Description: This migration removes the 'slug' column from the 'pages' table.
--              The slug field was part of the old single-language architecture.
--              In the multi-language architecture, URL paths are stored as 'url_path'
--              in the 'page_i18n' table instead.
-- ========================================================================

USE `admincraft-db`;

-- Check if column exists and drop it
SET @col_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pages'
    AND COLUMN_NAME = 'slug'
);

SET @sql_drop_slug = IF(@col_exists > 0,
  'ALTER TABLE pages DROP COLUMN slug',
  'SELECT "Column slug does not exist in pages table - no action needed" AS message'
);

PREPARE stmt FROM @sql_drop_slug;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify the column has been removed
SELECT
  CASE
    WHEN COUNT(*) = 0 THEN 'SUCCESS: slug column removed from pages table'
    ELSE 'WARNING: slug column still exists in pages table'
  END AS migration_status
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'pages'
  AND COLUMN_NAME = 'slug';
