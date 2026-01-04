-- =====================================================
-- V24: Fix link_type column type
-- Convert ENUM to VARCHAR for JPA compatibility
-- NOTE: This migration is now redundant as V23 was updated to use VARCHAR.
-- It is kept for backward compatibility with systems where V23 ran with ENUM.
-- =====================================================

ALTER TABLE component_media_links
    MODIFY COLUMN link_type VARCHAR(20) NOT NULL DEFAULT 'ENTRY_MEDIA';
