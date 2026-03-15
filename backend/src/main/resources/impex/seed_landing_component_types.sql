-- #ADMINCRAFT_IMPEX
-- Landing Page Component Types — ImpEx seed file.
-- Run via Admin UI /{lang}/impex after seed_liko_components.sql is applied.
-- Idempotent: safe to run multiple times.
-- Creates type-specific component types for the Liko landing page sections.
-- Each type maps 1:1 to a frontend renderer — no styleClass dispatch needed.

-- ============================================================
-- 1. NEW COMPONENT TYPES
-- ============================================================

INSERT INTO component_types (uuid, uid, name, category, is_navigation_aware, created_at, updated_at)
VALUES
  (UUID(), 'HeroBannerComponent',       'Hero Banner',       'layout',  false, NOW(), NOW()),
  (UUID(), 'AboutBannerComponent',      'About Section',     'layout',  false, NOW(), NOW()),
  (UUID(), 'VideoSectionComponent',     'Video Section',     'media',   false, NOW(), NOW()),
  (UUID(), 'AwardBannerComponent',      'Award Section',     'layout',  false, NOW(), NOW()),
  (UUID(), 'ServiceCardComponent',      'Service Cards',     'layout',  false, NOW(), NOW()),
  (UUID(), 'ProjectCardComponent',      'Project Cards',     'layout',  false, NOW(), NOW()),
  (UUID(), 'InstagramSectionComponent', 'Instagram Section', 'social',  false, NOW(), NOW()),
  (UUID(), 'MarqueeTextComponent',      'Marquee Text',      'content', false, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name                = VALUES(name),
  category            = VALUES(category),
  is_navigation_aware = VALUES(is_navigation_aware),
  updated_at          = NOW();

-- ============================================================
-- 2. MIGRATE EXISTING COMPONENTS TO NEW TYPES
-- ============================================================

UPDATE components SET component_type_id = (SELECT id FROM component_types WHERE uid = 'HeroBannerComponent')
WHERE uid = 'HomepageHeroBanner';

UPDATE components SET component_type_id = (SELECT id FROM component_types WHERE uid = 'AboutBannerComponent')
WHERE uid = 'HomepageAboutSection';

UPDATE components SET component_type_id = (SELECT id FROM component_types WHERE uid = 'VideoSectionComponent')
WHERE uid = 'HomepageVideoSection';

UPDATE components SET component_type_id = (SELECT id FROM component_types WHERE uid = 'AwardBannerComponent')
WHERE uid = 'HomepageAwardSection';

UPDATE components SET component_type_id = (SELECT id FROM component_types WHERE uid = 'ServiceCardComponent')
WHERE uid = 'HomepageServiceSection';

UPDATE components SET component_type_id = (SELECT id FROM component_types WHERE uid = 'ProjectCardComponent')
WHERE uid = 'HomepageProjectSection';

UPDATE components SET component_type_id = (SELECT id FROM component_types WHERE uid = 'InstagramSectionComponent')
WHERE uid = 'HomepageInstagramSection';

UPDATE components SET component_type_id = (SELECT id FROM component_types WHERE uid = 'MarqueeTextComponent')
WHERE uid = 'HomepageMarqueeText';
