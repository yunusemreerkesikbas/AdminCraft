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
INSERT INTO media_folders (uuid, uid, code, name, path, depth)
VALUES (UUID(), 'mediafolder_root', 'root', 'Root', '/', 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Default subfolders
INSERT INTO media_folders (uuid, uid, code, name, parent_id, path, depth)
SELECT UUID(), 'mediafolder_images', 'images', 'Images', id, '/images/', 1
FROM media_folders WHERE code = 'root'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO media_folders (uuid, uid, code, name, parent_id, path, depth)
SELECT UUID(), 'mediafolder_documents', 'documents', 'Documents', id, '/documents/', 1
FROM media_folders WHERE code = 'root'
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO media_folders (uuid, uid, code, name, parent_id, path, depth)
SELECT UUID(), 'mediafolder_videos', 'videos', 'Videos', id, '/videos/', 1
FROM media_folders WHERE code = 'root'
ON DUPLICATE KEY UPDATE name = VALUES(name);
