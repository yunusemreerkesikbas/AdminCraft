-- #ADMINCRAFT_IMPEX
-- Service Page Media UID Alignment — run after media uploads.
-- Purpose: assign stable semantic media UIDs used by seed_service_content_page.sql.
-- Safety: uses MAX(id) per original_name and no-ops when the target UID is owned by a different record.

-- ============================================================
-- 1. HERO
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'hero-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-hero-bg' AND existing.id <> m.id
SET m.uid = 'service-hero-bg'
WHERE m.uid != 'service-hero-bg' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'hero-shape-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-hero-shape' AND existing.id <> m.id
SET m.uid = 'service-hero-shape'
WHERE m.uid != 'service-hero-shape' AND existing.id IS NULL;

-- ============================================================
-- 2. SERVICE ICONS
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'service-icon-1.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-icon-1' AND existing.id <> m.id
SET m.uid = 'service-icon-1'
WHERE m.uid != 'service-icon-1' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'service-icon-2.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-icon-2' AND existing.id <> m.id
SET m.uid = 'service-icon-2'
WHERE m.uid != 'service-icon-2' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'service-icon-3.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-icon-3' AND existing.id <> m.id
SET m.uid = 'service-icon-3'
WHERE m.uid != 'service-icon-3' AND existing.id IS NULL;

-- ============================================================
-- 3. SERVICE PANELS
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'service-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-panel-img-1' AND existing.id <> m.id
SET m.uid = 'service-panel-img-1'
WHERE m.uid != 'service-panel-img-1' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'service-2.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-panel-img-2' AND existing.id <> m.id
SET m.uid = 'service-panel-img-2'
WHERE m.uid != 'service-panel-img-2' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'service-3.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-panel-img-3' AND existing.id <> m.id
SET m.uid = 'service-panel-img-3'
WHERE m.uid != 'service-panel-img-3' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'service-4.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-panel-img-4' AND existing.id <> m.id
SET m.uid = 'service-panel-img-4'
WHERE m.uid != 'service-panel-img-4' AND existing.id IS NULL;

-- ============================================================
-- 4. BRANDS
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-1.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-brand-1' AND existing.id <> m.id
SET m.uid = 'service-brand-1'
WHERE m.uid != 'service-brand-1' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-2.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-brand-2' AND existing.id <> m.id
SET m.uid = 'service-brand-2'
WHERE m.uid != 'service-brand-2' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-3.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-brand-3' AND existing.id <> m.id
SET m.uid = 'service-brand-3'
WHERE m.uid != 'service-brand-3' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-4.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-brand-4' AND existing.id <> m.id
SET m.uid = 'service-brand-4'
WHERE m.uid != 'service-brand-4' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-5.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-brand-5' AND existing.id <> m.id
SET m.uid = 'service-brand-5'
WHERE m.uid != 'service-brand-5' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-6.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-brand-6' AND existing.id <> m.id
SET m.uid = 'service-brand-6'
WHERE m.uid != 'service-brand-6' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-7.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-brand-7' AND existing.id <> m.id
SET m.uid = 'service-brand-7'
WHERE m.uid != 'service-brand-7' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-8.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-brand-8' AND existing.id <> m.id
SET m.uid = 'service-brand-8'
WHERE m.uid != 'service-brand-8' AND existing.id IS NULL;

-- ============================================================
-- 5. PORT IMAGES (line image slider)
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-port-1' AND existing.id <> m.id
SET m.uid = 'service-port-1'
WHERE m.uid != 'service-port-1' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-2.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-port-2' AND existing.id <> m.id
SET m.uid = 'service-port-2'
WHERE m.uid != 'service-port-2' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-3.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-port-3' AND existing.id <> m.id
SET m.uid = 'service-port-3'
WHERE m.uid != 'service-port-3' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-4.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'service-port-4' AND existing.id <> m.id
SET m.uid = 'service-port-4'
WHERE m.uid != 'service-port-4' AND existing.id IS NULL;

