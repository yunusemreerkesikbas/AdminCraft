-- =====================================================
-- V24: Fix link_type column type
-- Convert ENUM to VARCHAR for JPA compatibility
-- =====================================================

ALTER TABLE component_media_links
    MODIFY COLUMN link_type VARCHAR(20) NOT NULL DEFAULT 'ENTRY_MEDIA';
