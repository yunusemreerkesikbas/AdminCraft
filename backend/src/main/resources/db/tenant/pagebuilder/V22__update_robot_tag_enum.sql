-- Add robot_tag column to pages table with ENUM type
-- Note: This column was missing from V1 baseline but referenced in later migrations.
ALTER TABLE pages ADD COLUMN robot_tag ENUM('INDEX_FOLLOW', 'NOINDEX_FOLLOW', 'INDEX_NOFOLLOW', 'NOINDEX_NOFOLLOW') DEFAULT 'INDEX_FOLLOW';
