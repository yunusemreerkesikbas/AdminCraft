-- ============================================
-- Media Module Seed Data
-- Description: System format presets and root folder
-- ============================================

-- System format presets (SAP Hybris style)
INSERT INTO media_formats (uuid, uid, code, name, width, height, quality, crop_mode, is_system)
VALUES
    (UUID(), 'mediaformat_thumb', 'THUMBNAIL', 'Thumbnail', 100, 100, 80, 'COVER', TRUE),
    (UUID(), 'mediaformat_small', 'SMALL', 'Small', 300, 300, 85, 'FIT', TRUE),
    (UUID(), 'mediaformat_medium', 'MEDIUM', 'Medium', 600, 600, 85, 'FIT', TRUE),
    (UUID(), 'mediaformat_large', 'LARGE', 'Large', 1200, 1200, 90, 'FIT', TRUE),
    (UUID(), 'mediaformat_zoom', 'ZOOM', 'Zoom', 2000, 2000, 95, 'FIT', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), width = VALUES(width), height = VALUES(height);

-- Root folder

