-- #CRAFTIVE_IMPEX
-- About Page Media UID Alignment — run after media uploads and after seed_liko_media_uids.sql.
-- Purpose: assign stable media UIDs used by seed_about_content_page.sql.
-- Note: about award #1 reuses homepage-award-1 from seed_liko_media_uids.sql.
-- Safety: original_name is not unique in media. Each statement targets only the latest uploaded
-- matching asset. If the target UID is already owned by a different record, the statement no-ops
-- instead of failing with a duplicate key error. Resolve such conflicts manually before re-running.

-- ============================================================
-- 1. HERO
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'hero-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-hero-bg' AND existing.id <> m.id
SET m.uid = 'about-hero-bg'
WHERE m.uid != 'about-hero-bg' AND existing.id IS NULL;

-- ============================================================
-- 2. STORY
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'about-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-story-1' AND existing.id <> m.id
SET m.uid = 'about-story-1'
WHERE m.uid != 'about-story-1' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'about-2.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-story-2' AND existing.id <> m.id
SET m.uid = 'about-story-2'
WHERE m.uid != 'about-story-2' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'about-3.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-story-3' AND existing.id <> m.id
SET m.uid = 'about-story-3'
WHERE m.uid != 'about-story-3' AND existing.id IS NULL;

-- ============================================================
-- 3. TEAM
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'team-1-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-team-1' AND existing.id <> m.id
SET m.uid = 'about-team-1'
WHERE m.uid != 'about-team-1' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'team-1-2.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-team-2' AND existing.id <> m.id
SET m.uid = 'about-team-2'
WHERE m.uid != 'about-team-2' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'team-1-3.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-team-3' AND existing.id <> m.id
SET m.uid = 'about-team-3'
WHERE m.uid != 'about-team-3' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'team-1-4.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-team-4' AND existing.id <> m.id
SET m.uid = 'about-team-4'
WHERE m.uid != 'about-team-4' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'team-1-9.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-team-5' AND existing.id <> m.id
SET m.uid = 'about-team-5'
WHERE m.uid != 'about-team-5' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'team-1-6.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-team-6' AND existing.id <> m.id
SET m.uid = 'about-team-6'
WHERE m.uid != 'about-team-6' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'team-1-7.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-team-7' AND existing.id <> m.id
SET m.uid = 'about-team-7'
WHERE m.uid != 'about-team-7' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'team-1-8.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-team-8' AND existing.id <> m.id
SET m.uid = 'about-team-8'
WHERE m.uid != 'about-team-8' AND existing.id IS NULL;

-- ============================================================
-- 4. BRANDS
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-1.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-brand-1' AND existing.id <> m.id
SET m.uid = 'about-brand-1'
WHERE m.uid != 'about-brand-1' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-2.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-brand-2' AND existing.id <> m.id
SET m.uid = 'about-brand-2'
WHERE m.uid != 'about-brand-2' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-3.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-brand-3' AND existing.id <> m.id
SET m.uid = 'about-brand-3'
WHERE m.uid != 'about-brand-3' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-4.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-brand-4' AND existing.id <> m.id
SET m.uid = 'about-brand-4'
WHERE m.uid != 'about-brand-4' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-5.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-brand-5' AND existing.id <> m.id
SET m.uid = 'about-brand-5'
WHERE m.uid != 'about-brand-5' AND existing.id IS NULL;

-- ============================================================
-- 5. AWARDS
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'award-2.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-award-2' AND existing.id <> m.id
SET m.uid = 'about-award-2'
WHERE m.uid != 'about-award-2' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'award-3.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-award-3' AND existing.id <> m.id
SET m.uid = 'about-award-3'
WHERE m.uid != 'about-award-3' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'award-4.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-award-4' AND existing.id <> m.id
SET m.uid = 'about-award-4'
WHERE m.uid != 'about-award-4' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'award-5.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-award-5' AND existing.id <> m.id
SET m.uid = 'about-award-5'
WHERE m.uid != 'about-award-5' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'award-6.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'about-award-6' AND existing.id <> m.id
SET m.uid = 'about-award-6'
WHERE m.uid != 'about-award-6' AND existing.id IS NULL;
