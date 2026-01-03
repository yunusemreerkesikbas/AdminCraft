-- ============================================
-- Remove Media Folder Feature
-- Version: V21
-- Description: Removes folder organization feature from media module
-- ============================================

-- 1. Drop foreign key constraint from media table
ALTER TABLE media DROP FOREIGN KEY fk_media_folder;

-- 2. Drop the folder_id index
ALTER TABLE media DROP INDEX idx_media_folder;

-- 3. Drop the folder_id column from media table
ALTER TABLE media DROP COLUMN folder_id;

-- 4. Drop the media_folders table
DROP TABLE IF EXISTS media_folders;
