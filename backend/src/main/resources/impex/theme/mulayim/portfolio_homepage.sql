-- #CRAFTIVE_IMPEX
-- Mulayim portfolio homepage seed.
-- Run via Admin UI /{lang}/impex after theme/mulayim/mulayim_foundation.sql and after media uploads.
-- Seeds the homepage body using the portfolio-grid-col-4 source layout.
-- Shared header and footer chrome remain foundation-owned.
-- Idempotent: safe to run multiple times.

-- ============================================================
-- 1. COMPONENT TYPES
-- ============================================================

INSERT INTO component_types (uuid, uid, name, category, is_navigation_aware, created_at, updated_at)
VALUES
  (UUID(), 'SimpleBannerComponent', 'Banner', 'hero', FALSE, NOW(), NOW()),
  (UUID(), 'FeatureCardComponent', 'Card', 'feature', FALSE, NOW(), NOW()),
  (UUID(), 'BigTextCtaComponent', 'Big Text CTA', 'content', FALSE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  is_navigation_aware = VALUES(is_navigation_aware),
  updated_at = NOW();

-- ============================================================
-- 2. ENTRY FIELD DEFINITIONS
-- ============================================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'linkUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'linkUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'category', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'BigTextCtaComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'BigTextCtaComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================================
-- 3. COMPONENTS
-- ============================================================

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'f6100001-0000-4000-8000-000000000001', 'IntroBannerBlock', ct.id, 'Intro Banner Block', 0, TRUE, 'intro-banner', 'PUBLISHED', NOW(), NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE
  component_type_id = VALUES(component_type_id),
  name = VALUES(name),
  display_order = VALUES(display_order),
  is_visible = VALUES(is_visible),
  style_classes = VALUES(style_classes),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'f6100002-0000-4000-8000-000000000002', 'PortfolioCardGrid', ct.id, 'Portfolio Card Grid', 0, TRUE, 'portfolio-card-grid', 'PUBLISHED', NOW(), NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE
  component_type_id = VALUES(component_type_id),
  name = VALUES(name),
  display_order = VALUES(display_order),
  is_visible = VALUES(is_visible),
  style_classes = VALUES(style_classes),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'f6100003-0000-4000-8000-000000000003', 'StatementCtaBlock', ct.id, 'Statement CTA Block', 0, TRUE, 'statement-cta', 'PUBLISHED', NOW(), NOW()
FROM component_types ct WHERE ct.uid = 'BigTextCtaComponent'
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
SELECT 'f6200001-0000-4000-8000-000000000001', 'IntroBannerBlockTr', c.id, 'TR',
  'Klasik Grid',
  'Ahmet Mulayim',
  'Detaylara dikkat eden, cuma akşamı biralarını seven ve evrende iz bırakmayı hedefleyen farklı disiplinlerden bir ekibiz.',
  'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'IntroBannerBlock'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'f6200002-0000-4000-8000-000000000002', 'IntroBannerBlockEn', c.id, 'EN',
  'Classic Grid',
  'Ahmet Mulayim',
  'We are a diverse team that works with a deep attention to detail, enjoys beers on Friday nights and aspires to leave a dent in the universe.',
  'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'IntroBannerBlock'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'f6200003-0000-4000-8000-000000000003', 'PortfolioCardGridTr', c.id, 'TR',
  NULL, NULL, NULL,
  'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'PortfolioCardGrid'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'f6200004-0000-4000-8000-000000000004', 'PortfolioCardGridEn', c.id, 'EN',
  NULL, NULL, NULL,
  'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'PortfolioCardGrid'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'f6200005-0000-4000-8000-000000000005', 'StatementCtaBlockTr', c.id, 'TR',
  'DIJITAL TASARIM DENEYIMI',
  'YARATICI STUDIO',
  NULL,
  'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StatementCtaBlock'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'f6200006-0000-4000-8000-000000000006', 'StatementCtaBlockEn', c.id, 'EN',
  'DIGITAL DESIGN EXPERIENCE',
  'CREATIVE STUDIO',
  NULL,
  'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StatementCtaBlock'
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
  SELECT 'f6300001-0000-4000-8000-000000000001' AS uuid, 'PortfolioCard01' AS uid, 'PortfolioCardGrid' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'f6300002-0000-4000-8000-000000000002', 'PortfolioCard02', 'PortfolioCardGrid', 1
  UNION ALL SELECT 'f6300003-0000-4000-8000-000000000003', 'PortfolioCard03', 'PortfolioCardGrid', 2
  UNION ALL SELECT 'f6300004-0000-4000-8000-000000000004', 'PortfolioCard04', 'PortfolioCardGrid', 3
  UNION ALL SELECT 'f6300005-0000-4000-8000-000000000005', 'PortfolioCard05', 'PortfolioCardGrid', 4
  UNION ALL SELECT 'f6300006-0000-4000-8000-000000000006', 'PortfolioCard06', 'PortfolioCardGrid', 5
  UNION ALL SELECT 'f6300007-0000-4000-8000-000000000007', 'PortfolioCard07', 'PortfolioCardGrid', 6
  UNION ALL SELECT 'f6300008-0000-4000-8000-000000000008', 'PortfolioCard08', 'PortfolioCardGrid', 7
  UNION ALL SELECT 'f6300009-0000-4000-8000-000000000009', 'StatementCtaAction', 'StatementCtaBlock', 0
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
  SELECT 'f6400001-0000-4000-8000-000000000001' AS uuid, 'PortfolioCard01Tr' AS uid, 'PortfolioCard01' AS entry_uid, 'TR' AS language, 'Lectus' AS title, '2024' AS description,
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'linkUrl', '/portfolio/lectus', 'category', 'Concept') AS custom_data
  UNION ALL SELECT 'f6400002-0000-4000-8000-000000000002', 'PortfolioCard01En', 'PortfolioCard01', 'EN', 'Lectus', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'linkUrl', '/portfolio/lectus', 'category', 'Concept')

  UNION ALL SELECT 'f6400003-0000-4000-8000-000000000003', 'PortfolioCard02Tr', 'PortfolioCard02', 'TR', 'The Stage', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'linkUrl', '/portfolio/the-stage', 'category', 'Branding')
  UNION ALL SELECT 'f6400004-0000-4000-8000-000000000004', 'PortfolioCard02En', 'PortfolioCard02', 'EN', 'The Stage', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'linkUrl', '/portfolio/the-stage', 'category', 'Branding')

  UNION ALL SELECT 'f6400005-0000-4000-8000-000000000005', 'PortfolioCard03Tr', 'PortfolioCard03', 'TR', 'Art Direction', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'linkUrl', '/portfolio/art-direction', 'category', 'Branding')
  UNION ALL SELECT 'f6400006-0000-4000-8000-000000000006', 'PortfolioCard03En', 'PortfolioCard03', 'EN', 'Art Direction', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'linkUrl', '/portfolio/art-direction', 'category', 'Branding')

  UNION ALL SELECT 'f6400007-0000-4000-8000-000000000007', 'PortfolioCard04Tr', 'PortfolioCard04', 'TR', 'Petit Navire', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'linkUrl', '/portfolio/petit-navire', 'category', 'Branding')
  UNION ALL SELECT 'f6400008-0000-4000-8000-000000000008', 'PortfolioCard04En', 'PortfolioCard04', 'EN', 'Petit Navire', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'linkUrl', '/portfolio/petit-navire', 'category', 'Branding')

  UNION ALL SELECT 'f6400009-0000-4000-8000-000000000009', 'PortfolioCard05Tr', 'PortfolioCard05', 'TR', 'Big dream', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'linkUrl', '/portfolio/big-dream', 'category', 'Branding')
  UNION ALL SELECT 'f6400010-0000-4000-8000-000000000010', 'PortfolioCard05En', 'PortfolioCard05', 'EN', 'Big dream', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'linkUrl', '/portfolio/big-dream', 'category', 'Branding')

  UNION ALL SELECT 'f6400011-0000-4000-8000-000000000011', 'PortfolioCard06Tr', 'PortfolioCard06', 'TR', 'The Stage', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'linkUrl', '/portfolio/the-stage-2', 'category', 'Branding')
  UNION ALL SELECT 'f6400012-0000-4000-8000-000000000012', 'PortfolioCard06En', 'PortfolioCard06', 'EN', 'The Stage', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'linkUrl', '/portfolio/the-stage-2', 'category', 'Branding')

  UNION ALL SELECT 'f6400013-0000-4000-8000-000000000013', 'PortfolioCard07Tr', 'PortfolioCard07', 'TR', 'Big dream', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'linkUrl', '/portfolio/big-dream-2', 'category', 'Creative')
  UNION ALL SELECT 'f6400014-0000-4000-8000-000000000014', 'PortfolioCard07En', 'PortfolioCard07', 'EN', 'Big dream', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'linkUrl', '/portfolio/big-dream-2', 'category', 'Creative')

  UNION ALL SELECT 'f6400015-0000-4000-8000-000000000015', 'PortfolioCard08Tr', 'PortfolioCard08', 'TR', 'Big dream', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'linkUrl', '/portfolio/big-dream-3', 'category', 'Creative')
  UNION ALL SELECT 'f6400016-0000-4000-8000-000000000016', 'PortfolioCard08En', 'PortfolioCard08', 'EN', 'Big dream', '2024',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'linkUrl', '/portfolio/big-dream-3', 'category', 'Creative')

  UNION ALL SELECT 'f6400017-0000-4000-8000-000000000017', 'StatementCtaActionTr', 'StatementCtaAction', 'TR', NULL, NULL,
    JSON_OBJECT('buttonText', 'Iletisime Gec', 'buttonUrl', '/contact')
  UNION ALL SELECT 'f6400018-0000-4000-8000-000000000018', 'StatementCtaActionEn', 'StatementCtaAction', 'EN', NULL, NULL,
    JSON_OBJECT('buttonText', 'Get in Touch', 'buttonUrl', '/contact')
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
-- 7. HOMEPAGE PAGE
-- ============================================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT UUID(), 'homepage',
  (SELECT id FROM page_templates WHERE uid = 'LandingPageTemplate'),
  'PUBLISHED', 'LANDING', TRUE, 'INDEX_FOLLOW', NULL
ON DUPLICATE KEY UPDATE
  template_id = VALUES(template_id),
  status = VALUES(status),
  page_type = VALUES(page_type),
  is_home = VALUES(is_home),
  robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT UUID(), 'homepage-tr', p.id, 'TR', 'Anasayfa', 'Klasik Grid', '/', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'homepage'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT UUID(), 'homepage-en', p.id, 'EN', 'Homepage', 'Classic Grid', '/', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'homepage'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

-- ============================================================
-- 8. PAGE_SLOTS
-- ============================================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT UUID(), CONCAT(p.uid, '-', ts.slot_name, 'Slot'), p.id, ts.slot_name, ts.position, ts.sort_order, TRUE, FALSE, NOW(), NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid = 'homepage'
  AND ts.slot_name IN ('Section1', 'Section2', 'Section3')
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  is_shared = VALUES(is_shared),
  updated_at = NOW();

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT UUID(), CONCAT(p.uid, '-', ts.slot_name, 'Slot'), p.id, ts.slot_name, ts.position, ts.sort_order, FALSE, FALSE, NOW(), NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid = 'homepage'
  AND ts.slot_name IN ('Section4', 'Section5', 'Section6', 'Section7', 'Section8')
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_active = FALSE,
  is_shared = FALSE,
  updated_at = NOW();

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
VALUES (UUID(), 'SharedHeaderSlot', NULL, 'Header', 'TOP', -1, TRUE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  is_shared = VALUES(is_shared),
  updated_at = NOW();

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
VALUES (UUID(), 'SharedFooterSlot', NULL, 'Footer', 'BOTTOM', 99, TRUE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  is_shared = VALUES(is_shared),
  updated_at = NOW();

-- ============================================================
-- 9. SLOT_COMPONENTS
-- ============================================================

UPDATE slot_components sc
JOIN page_slots ps ON ps.id = sc.slot_id
JOIN components c ON c.id = sc.component_id
SET sc.is_visible = FALSE
WHERE ps.uid IN (
  'homepage-Section1Slot',
  'homepage-Section2Slot',
  'homepage-Section3Slot',
  'homepage-Section4Slot',
  'homepage-Section5Slot',
  'homepage-Section6Slot',
  'homepage-Section7Slot',
  'homepage-Section8Slot'
)
  AND c.uid NOT IN ('IntroBannerBlock', 'PortfolioCardGrid', 'StatementCtaBlock');

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedHeaderSlot' AND c.uid = 'StorefrontHeaderMainNavigation'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 1, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedHeaderSlot' AND c.uid = 'StorefrontHeaderIntroBlock'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 2, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedHeaderSlot' AND c.uid = 'StorefrontHeaderSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 3, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedHeaderSlot' AND c.uid = 'StorefrontHeaderContactInfo'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'StorefrontFooterBrandBlock'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 1, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'StorefrontFooterSitemapNavigation'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 2, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'StorefrontFooterOfficeLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 3, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'StorefrontFooterNewsletter'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 4, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'StorefrontFooterSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section1Slot' AND c.uid = 'IntroBannerBlock'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section2Slot' AND c.uid = 'PortfolioCardGrid'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section3Slot' AND c.uid = 'StatementCtaBlock'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

-- ============================================================
-- 10. MEDIA UID ALIGNMENT
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-4.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'portfolio-grid-image-01' AND existing.id <> m.id
SET m.uid = 'portfolio-grid-image-01'
WHERE m.uid != 'portfolio-grid-image-01' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-3.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'portfolio-grid-image-02' AND existing.id <> m.id
SET m.uid = 'portfolio-grid-image-02'
WHERE m.uid != 'portfolio-grid-image-02' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-2.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'portfolio-grid-image-03' AND existing.id <> m.id
SET m.uid = 'portfolio-grid-image-03'
WHERE m.uid != 'portfolio-grid-image-03' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'portfolio-grid-image-04' AND existing.id <> m.id
SET m.uid = 'portfolio-grid-image-04'
WHERE m.uid != 'portfolio-grid-image-04' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-5.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'portfolio-grid-image-05' AND existing.id <> m.id
SET m.uid = 'portfolio-grid-image-05'
WHERE m.uid != 'portfolio-grid-image-05' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-6.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'portfolio-grid-image-06' AND existing.id <> m.id
SET m.uid = 'portfolio-grid-image-06'
WHERE m.uid != 'portfolio-grid-image-06' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-7.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'portfolio-grid-image-07' AND existing.id <> m.id
SET m.uid = 'portfolio-grid-image-07'
WHERE m.uid != 'portfolio-grid-image-07' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'port-8.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'portfolio-grid-image-08' AND existing.id <> m.id
SET m.uid = 'portfolio-grid-image-08'
WHERE m.uid != 'portfolio-grid-image-08' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'logo.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'site-logo-dark' AND existing.id <> m.id
SET m.uid = 'site-logo-dark'
WHERE m.uid != 'site-logo-dark' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'logo-white.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'site-logo-light' AND existing.id <> m.id
SET m.uid = 'site-logo-light'
WHERE m.uid != 'site-logo-light' AND existing.id IS NULL;

UPDATE sites
SET
  logo_media_uid = COALESCE((SELECT uid FROM media WHERE uid = 'site-logo-light' LIMIT 1), logo_media_uid),
  logo_dark_media_uid = COALESCE((SELECT uid FROM media WHERE uid = 'site-logo-dark' LIMIT 1), logo_dark_media_uid);

-- ============================================================
-- REQUIRED MEDIA UPLOADS
--   port-1.jpg through port-8.jpg
--   logo.png
--   logo-white.png
-- ============================================================
