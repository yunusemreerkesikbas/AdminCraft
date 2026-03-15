-- #ADMINCRAFT_IMPEX
-- Liko Media UID Alignment — ImpEx seed file.
-- Run via Admin UI /{lang}/impex AFTER seed_liko_components.sql.
-- Purpose: Assigns semantic UIDs to uploaded media so component mediaUid
--          references (e.g. 'homepage-hero-bg') resolve to the correct files.
-- Idempotent: safe to run multiple times (uses ON DUPLICATE KEY / WHERE original_name).

-- ============================================================
-- 1. HERO BANNER
-- ============================================================
UPDATE media SET uid = 'homepage-hero-bg'
WHERE original_name = 'hero-bg-1.jpg' AND uid != 'homepage-hero-bg';

-- ============================================================
-- 2. ABOUT SECTION
--    Entry1 = main left image (ab-1)
--    Entry2 = inner/overlay image (ab-2)
--    Entry3 = right/side image   (ab-3)
-- ============================================================
UPDATE media SET uid = 'homepage-about-1'
WHERE original_name = 'ab-1.jpg' AND uid != 'homepage-about-1';

UPDATE media SET uid = 'homepage-about-2'
WHERE original_name = 'ab-2.jpg' AND uid != 'homepage-about-2';

UPDATE media SET uid = 'homepage-about-3'
WHERE original_name = 'ab-3.jpg' AND uid != 'homepage-about-3';

-- ============================================================
-- 3. PROJECTS (7 images)
-- ============================================================
UPDATE media SET uid = 'homepage-project-1'
WHERE original_name = 'project-1.jpg' AND uid != 'homepage-project-1';

UPDATE media SET uid = 'homepage-project-2'
WHERE original_name = 'project-2.jpg' AND uid != 'homepage-project-2';

UPDATE media SET uid = 'homepage-project-3'
WHERE original_name = 'project-3.jpg' AND uid != 'homepage-project-3';

UPDATE media SET uid = 'homepage-project-4'
WHERE original_name = 'project-4.jpg' AND uid != 'homepage-project-4';

UPDATE media SET uid = 'homepage-project-5'
WHERE original_name = 'project-5.jpg' AND uid != 'homepage-project-5';

UPDATE media SET uid = 'homepage-project-6'
WHERE original_name = 'project-6.jpg' AND uid != 'homepage-project-6';

UPDATE media SET uid = 'homepage-project-7'
WHERE original_name = 'project-7.jpg' AND uid != 'homepage-project-7';

-- ============================================================
-- 4. AWARD SECTION
-- ============================================================
UPDATE media SET uid = 'homepage-award-1'
WHERE original_name = 'award-1.png' AND uid != 'homepage-award-1';

-- ============================================================
-- 5. INSTAGRAM SECTION (6 uploaded - 7th slot will remain empty)
--    insta-1    = main large image
--    insta-2    = floating
--    insta-inner-3/5/6/7 = floating (insta-inner-4 not uploaded)
-- ============================================================
UPDATE media SET uid = 'homepage-instagram-1'
WHERE original_name = 'insta-1.jpg' AND uid != 'homepage-instagram-1';

UPDATE media SET uid = 'homepage-instagram-2'
WHERE original_name = 'insta-2.jpg' AND uid != 'homepage-instagram-2';

UPDATE media SET uid = 'homepage-instagram-3'
WHERE original_name = 'insta-inner-3.jpg' AND uid != 'homepage-instagram-3';

UPDATE media SET uid = 'homepage-instagram-4'
WHERE original_name = 'insta-inner-5.jpg' AND uid != 'homepage-instagram-4';

UPDATE media SET uid = 'homepage-instagram-5'
WHERE original_name = 'insta-inner-6.jpg' AND uid != 'homepage-instagram-5';

UPDATE media SET uid = 'homepage-instagram-6'
WHERE original_name = 'insta-inner-7.jpg' AND uid != 'homepage-instagram-6';

-- ============================================================
-- 6. SITE LOGOS
--    logo.png       = dark-colored logo (used on light backgrounds → logoDarkUrl)
--    logo-white.png = white/light logo  (used on dark backgrounds  → logoUrl)
-- ============================================================
UPDATE media SET uid = 'site-logo-dark'
WHERE original_name = 'logo.png' AND uid != 'site-logo-dark';

UPDATE media SET uid = 'site-logo-light'
WHERE original_name = 'logo-white.png' AND uid != 'site-logo-light';

UPDATE sites SET
    logo_media_uid      = (SELECT uid FROM media WHERE original_name = 'logo-white.png' LIMIT 1),
    logo_dark_media_uid = (SELECT uid FROM media WHERE original_name = 'logo.png' LIMIT 1)
WHERE logo_media_uid IS NULL OR logo_dark_media_uid IS NULL;

-- ============================================================
-- MISSING MEDIA (upload required separately):
--   homepage-video-poster  → video poster image (any format)
--   homepage-service-icon-1 through -4 → 4 service icon images (SVG/PNG)
--   homepage-instagram-7   → 7th instagram floating image
-- ============================================================
