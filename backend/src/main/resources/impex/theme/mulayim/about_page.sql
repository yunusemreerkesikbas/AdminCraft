-- #CRAFTIVE_IMPEX
-- Mulayim about page seed.
-- Run via Admin UI /{lang}/impex after theme/mulayim/mulayim_foundation.sql and after media uploads.
-- Seeds the /about content page from the about-me source page first three sections.
-- Shared header and footer chrome remain foundation-owned.
-- Idempotent: safe to run multiple times.

-- ============================================================
-- 1. COMPONENT TYPES
-- ============================================================

INSERT INTO component_types (uuid, uid, name, category, is_navigation_aware, created_at, updated_at)
VALUES
  (UUID(), 'ContentHeroComponent', 'Content Hero', 'hero', FALSE, NOW(), NOW()),
  (UUID(), 'SplitMediaIntroComponent', 'Split Media Intro', 'content', FALSE, NOW(), NOW()),
  (UUID(), 'ImageMarqueeComponent', 'Image Marquee', 'gallery', FALSE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  is_navigation_aware = VALUES(is_navigation_aware),
  updated_at = NOW();

-- ============================================================
-- 2. ENTRY FIELD DEFINITIONS
-- ============================================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContentHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContentHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContentHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'supportingText', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'ContentHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'scrollTarget', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContentHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'SplitMediaIntroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'introLabel', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'SplitMediaIntroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'items', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'SplitMediaIntroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ImageMarqueeComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'altText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ImageMarqueeComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================================
-- 3. COMPONENTS
-- ============================================================

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, ct.id, seed.name, seed.display_order, TRUE, seed.style_classes, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT 'a6200001-0000-4000-8000-000000000001' AS uuid, 'MulayimAboutHeroComponent' AS uid, 'ContentHeroComponent' AS component_type_uid, 'Mulayim About Hero' AS name, 0 AS display_order, 'mulayim-about-hero' AS style_classes
  UNION ALL SELECT 'a6200002-0000-4000-8000-000000000002', 'MulayimAboutIntroComponent', 'SplitMediaIntroComponent', 'Mulayim About Intro', 0, 'mulayim-about-intro'
  UNION ALL SELECT 'a6200003-0000-4000-8000-000000000003', 'MulayimAboutPortfolioMarquee', 'ImageMarqueeComponent', 'Mulayim About Portfolio Images', 0, 'mulayim-about-portfolio'
) seed
JOIN component_types ct ON ct.uid = seed.component_type_uid
ON DUPLICATE KEY UPDATE
  component_type_id = VALUES(component_type_id),
  name = VALUES(name),
  display_order = VALUES(display_order),
  is_visible = VALUES(is_visible),
  style_classes = VALUES(style_classes),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 4. COMPONENT_I18N
-- ============================================================

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, c.id, seed.language, seed.title, seed.subtitle, seed.description, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT 'a6210001-0000-4000-8000-000000000001' AS uuid, 'MulayimAboutHeroComponentTr' AS uid, 'MulayimAboutHeroComponent' AS component_uid, 'TR' AS language,
    'Şekiller ile oluşturulan marka hikayeleri!' AS title,
    'Ahmet Mülayim',
    'Grafik tasarım atölyesi · Atakum, Konya'
  UNION ALL SELECT 'a6210002-0000-4000-8000-000000000002', 'MulayimAboutHeroComponentEn', 'MulayimAboutHeroComponent', 'EN',
    'Brand stories created with shapes!',
    'Ahmet Mülayim',
    'Graphic design studio for logo, identity, social channels and campaign visuals — collaborative process rooted in Konya, Turkey.'
  UNION ALL SELECT 'a6210003-0000-4000-8000-000000000003', 'MulayimAboutIntroComponentTr', 'MulayimAboutIntroComponent', 'TR',
    'Ne yapıyoruz',
    'Grafik tasarım hizmetleri',
    'Ahmet Mülayim, markaların görsel kimliğini daha net ve akılda kalıcı hale getirmek için çalışır.

Logo tasarımından kurumsal kimliğe, sosyal medya içeriklerinden web ve kampanya görsellerine kadar her projede markanın sektörünü, hedef kitlesini ve kullanım alanlarını birlikte ele alır.'
  UNION ALL SELECT 'a6210004-0000-4000-8000-000000000004', 'MulayimAboutIntroComponentEn', 'MulayimAboutIntroComponent', 'EN',
    'What we do',
    'Graphic design services',
    'Ahmet Mülayim helps brands build clearer and more memorable visual identities.

From logo design and corporate identity to social media content, web design and campaign visuals, each project is shaped around the brand’s sector, audience and real usage needs.'
  UNION ALL SELECT 'a6210005-0000-4000-8000-000000000005', 'MulayimAboutPortfolioMarqueeTr', 'MulayimAboutPortfolioMarquee', 'TR',
    NULL, NULL, NULL
  UNION ALL SELECT 'a6210006-0000-4000-8000-000000000006', 'MulayimAboutPortfolioMarqueeEn', 'MulayimAboutPortfolioMarquee', 'EN',
    NULL, NULL, NULL
) seed
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 5. COMPONENT_ENTRIES
-- ============================================================

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, c.id, seed.sort_order, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT 'a6220001-0000-4000-8000-000000000001' AS uuid, 'MulayimAboutHeroPrimary' AS uid, 'MulayimAboutHeroComponent' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'a6220005-0000-4000-8000-000000000005', 'MulayimAboutIntroList1', 'MulayimAboutIntroComponent', 0
  UNION ALL SELECT 'a6220006-0000-4000-8000-000000000006', 'MulayimAboutIntroList2', 'MulayimAboutIntroComponent', 1
  UNION ALL SELECT 'a6220007-0000-4000-8000-000000000007', 'MulayimAboutPortfolioImage1', 'MulayimAboutPortfolioMarquee', 0
  UNION ALL SELECT 'a6220008-0000-4000-8000-000000000008', 'MulayimAboutPortfolioImage2', 'MulayimAboutPortfolioMarquee', 1
  UNION ALL SELECT 'a6220009-0000-4000-8000-000000000009', 'MulayimAboutPortfolioImage3', 'MulayimAboutPortfolioMarquee', 2
) seed
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  component_id = VALUES(component_id),
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible),
  style_classes = VALUES(style_classes),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 6. COMPONENT_ENTRY_I18N
-- ============================================================

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT seed.uuid, seed.uid, e.id, seed.language, seed.title, seed.description, 'PUBLISHED', seed.custom_data, NOW(), NOW(), NOW()
FROM (
  SELECT 'a6230001-0000-4000-8000-000000000001' AS uuid, 'MulayimAboutHeroPrimaryTr' AS uid, 'MulayimAboutHeroPrimary' AS entry_uid, 'TR' AS language,
    NULL AS title,
    NULL AS description,
    JSON_OBJECT('mediaUid', 'mulayim-about-hero', 'buttonText', 'Projeleri İncele', 'buttonUrl', '/portfolio', 'supportingText', NULL, 'scrollTarget', 'MulayimAboutIntroComponent') AS custom_data
  UNION ALL SELECT 'a6230002-0000-4000-8000-000000000002', 'MulayimAboutHeroPrimaryEn', 'MulayimAboutHeroPrimary', 'EN',
    'Studio image',
    'Explore the studio’s graphic design approach and selected brand work.',
    JSON_OBJECT('mediaUid', 'mulayim-about-hero', 'buttonText', 'View Projects', 'buttonUrl', '/portfolio', 'supportingText', 'Logo, identity and campaign work', 'scrollTarget', 'MulayimAboutIntroComponent')
  UNION ALL SELECT 'a6230009-0000-4000-8000-000000000009', 'MulayimAboutIntroList1Tr', 'MulayimAboutIntroList1', 'TR',
    'Hizmetler', NULL,
    JSON_OBJECT('items', JSON_ARRAY('Logo tasarım ve kurumsal kimlik', 'Sosyal medya yönetimi', 'Web tasarım', 'Katalog ve broşür tasarımı', 'Açık hava ve fuar tasarımı'))
  UNION ALL SELECT 'a6230010-0000-4000-8000-000000000010', 'MulayimAboutIntroList1En', 'MulayimAboutIntroList1', 'EN',
    'Services', NULL,
    JSON_OBJECT('items', JSON_ARRAY('Logo design and corporate identity', 'Social media management', 'Web design', 'Catalog and brochure design', 'Outdoor and exhibition design'))
  UNION ALL SELECT 'a6230011-0000-4000-8000-000000000011', 'MulayimAboutIntroList2Tr', 'MulayimAboutIntroList2', 'TR',
    'Odak alanları', NULL,
    JSON_OBJECT('items', JSON_ARRAY('Promosyon ve ambalaj tasarımı', 'Prodüksiyon hizmeti', 'Özel günler ve kampanya tasarımı', 'Video içerik tasarımı', 'Marka görsel sistemi'))
  UNION ALL SELECT 'a6230012-0000-4000-8000-000000000012', 'MulayimAboutIntroList2En', 'MulayimAboutIntroList2', 'EN',
    'Focus areas', NULL,
    JSON_OBJECT('items', JSON_ARRAY('Promotional and packaging design', 'Production services', 'Seasonal and campaign design', 'Video content design', 'Brand visual systems'))
  UNION ALL SELECT 'a6230013-0000-4000-8000-000000000013', 'MulayimAboutPortfolioImage1Tr', 'MulayimAboutPortfolioImage1', 'TR',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'mulayim-about-portfolio-1', 'altText', 'Mulayim portfolio görseli 1')
  UNION ALL SELECT 'a6230014-0000-4000-8000-000000000014', 'MulayimAboutPortfolioImage1En', 'MulayimAboutPortfolioImage1', 'EN',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'mulayim-about-portfolio-1', 'altText', 'Mulayim portfolio image 1')
  UNION ALL SELECT 'a6230015-0000-4000-8000-000000000015', 'MulayimAboutPortfolioImage2Tr', 'MulayimAboutPortfolioImage2', 'TR',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'mulayim-about-portfolio-2', 'altText', 'Mulayim portfolio görseli 2')
  UNION ALL SELECT 'a6230016-0000-4000-8000-000000000016', 'MulayimAboutPortfolioImage2En', 'MulayimAboutPortfolioImage2', 'EN',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'mulayim-about-portfolio-2', 'altText', 'Mulayim portfolio image 2')
  UNION ALL SELECT 'a6230017-0000-4000-8000-000000000017', 'MulayimAboutPortfolioImage3Tr', 'MulayimAboutPortfolioImage3', 'TR',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'mulayim-about-portfolio-3', 'altText', 'Mulayim portfolio görseli 3')
  UNION ALL SELECT 'a6230018-0000-4000-8000-000000000018', 'MulayimAboutPortfolioImage3En', 'MulayimAboutPortfolioImage3', 'EN',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'mulayim-about-portfolio-3', 'altText', 'Mulayim portfolio image 3')
) seed
JOIN component_entries e ON e.uid = seed.entry_uid
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  status = VALUES(status),
  custom_data = VALUES(custom_data),
  published_at = VALUES(published_at),
  updated_at = NOW();

-- ============================================================
-- 7. ABOUT PAGE
-- ============================================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'a6240001-0000-4000-8000-000000000001', 'about',
  (SELECT id FROM page_templates WHERE uid = 'ContentPageTemplate'),
  'PUBLISHED', 'CONTENT', FALSE, 'INDEX_FOLLOW', NULL
ON DUPLICATE KEY UPDATE
  template_id = VALUES(template_id),
  status = VALUES(status),
  page_type = VALUES(page_type),
  is_home = VALUES(is_home),
  robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, description, canonical_url, status)
SELECT 'a6250001-0000-4000-8000-000000000001', 'about-tr', p.id, 'TR', 'Hakkında', 'Hakkımda | Ahmet Mülayim — Grafik tasarım atölyesi, Konya', 'Ahmet Mülayim ile tanışın: logo, kurumsal kimlik ve kampanya görsellerinde net, akılda kalıcı ve mecralarda tutarlı çalışan görsel sistemler. Atakum, Samsun.', '/about', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'about'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  description = VALUES(description),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, description, canonical_url, status)
SELECT 'a6250002-0000-4000-8000-000000000002', 'about-en', p.id, 'EN', 'About', 'About | Ahmet Mülayim — Graphic design studio, Konya', 'Meet Ahmet Mülayim: logo design, brand identity and campaign visuals shaped into distinctive, practical systems for brands. Based in Atakum, Samsun, Turkey.', '/about', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'about'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  description = VALUES(description),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

-- ============================================================
-- 8. PAGE_SLOTS
-- ============================================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT UUID(), CONCAT(p.uid, '-', ts.slot_name, 'Slot'), p.id, ts.slot_name, ts.position, ts.sort_order, TRUE, FALSE, NOW(), NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid = 'about'
  AND ts.slot_name IN ('TopContent', 'BodyContent', 'SideContent')
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  is_shared = VALUES(is_shared),
  updated_at = NOW();

-- ============================================================
-- 9. SLOT_COMPONENTS
-- ============================================================

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, seed.sort_order, TRUE, NOW()
FROM (
  SELECT 'about-TopContentSlot' AS slot_uid, 'MulayimAboutHeroComponent' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'about-BodyContentSlot', 'MulayimAboutIntroComponent', 0
  UNION ALL SELECT 'about-BodyContentSlot', 'PortfolioPageBrandStrip', 1
  UNION ALL SELECT 'about-BodyContentSlot', 'MulayimAboutPortfolioMarquee', 2
) seed
JOIN page_slots ps ON ps.uid = seed.slot_uid
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible);

-- ============================================================
-- 10. MEDIA UID ALIGNMENT
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'hero-2-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'mulayim-about-hero' AND existing.id <> m.id
SET m.uid = 'mulayim-about-hero'
WHERE m.uid != 'mulayim-about-hero' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'about-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'mulayim-about-intro-1' AND existing.id <> m.id
SET m.uid = 'mulayim-about-intro-1'
WHERE m.uid != 'mulayim-about-intro-1' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'about-2.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'mulayim-about-intro-2' AND existing.id <> m.id
SET m.uid = 'mulayim-about-intro-2'
WHERE m.uid != 'mulayim-about-intro-2' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'about-3.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'mulayim-about-intro-3' AND existing.id <> m.id
SET m.uid = 'mulayim-about-intro-3'
WHERE m.uid != 'mulayim-about-intro-3' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'portfolio-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'mulayim-about-portfolio-1' AND existing.id <> m.id
SET m.uid = 'mulayim-about-portfolio-1'
WHERE m.uid != 'mulayim-about-portfolio-1' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'portfolio-2.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'mulayim-about-portfolio-2' AND existing.id <> m.id
SET m.uid = 'mulayim-about-portfolio-2'
WHERE m.uid != 'mulayim-about-portfolio-2' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'portfolio-3.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'mulayim-about-portfolio-3' AND existing.id <> m.id
SET m.uid = 'mulayim-about-portfolio-3'
WHERE m.uid != 'mulayim-about-portfolio-3' AND existing.id IS NULL;

-- ============================================================
-- REQUIRED MEDIA UPLOADS
--   hero-2-1.jpg
--   about-1.jpg
--   about-2.jpg
--   about-3.jpg
--   portfolio-1.jpg
--   portfolio-2.jpg
--   portfolio-3.jpg
-- ============================================================

