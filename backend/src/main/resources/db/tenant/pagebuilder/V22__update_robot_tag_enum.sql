-- Update robot_tag column to use ENUM type if supported, or verify VARCHAR compatibility
-- Since we just switched Java side to Enum, we should ensure DB column is consistent.
-- Assuming MySQL/MariaDB dialect based on previous files.

ALTER TABLE pages MODIFY robot_tag ENUM('INDEX_FOLLOW', 'NOINDEX_FOLLOW', 'INDEX_NOFOLLOW', 'NOINDEX_NOFOLLOW') DEFAULT 'INDEX_FOLLOW';
