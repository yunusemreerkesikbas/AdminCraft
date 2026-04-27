-- #CRAFTIVE_IMPEX
-- Mulayim portfolio page seed.
-- Run via Admin UI /{lang}/impex after theme/mulayim/mulayim_foundation.sql and after media uploads.
-- Seeds the /portfolio landing page with hero and grid content.
-- Shared header and footer chrome remain foundation-owned.
-- Idempotent: safe to run multiple times.

-- ============================================================
-- 1. COMPONENT TYPES
-- ============================================================

INSERT INTO component_types (uuid, uid, name, category, is_navigation_aware, created_at, updated_at)
VALUES
  (UUID(), 'SimpleBannerComponent', 'Banner', 'hero', FALSE, NOW(), NOW()),
  (UUID(), 'FeatureCardComponent', 'Card', 'feature', FALSE, NOW(), NOW())
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
SELECT ct.id, 'filterKeys', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================================
-- 3. COMPONENTS
-- ============================================================

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, ct.id, seed.name, seed.display_order, TRUE, seed.style_classes, seed.custom_data, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT
    'd7300001-0000-4000-8000-000000000001' AS uuid,
    'PortfolioPageHeroBlock' AS uid,
    'SimpleBannerComponent' AS component_type_uid,
    'Portfolio Page Hero Block' AS name,
    0 AS display_order,
    'portfolio-page-hero' AS style_classes,
    NULL AS custom_data
  UNION ALL
  SELECT
    'd7300002-0000-4000-8000-000000000002',
    'PortfolioPageGrid',
    'FeatureCardComponent',
    'Portfolio Page Grid',
    0,
    'portfolio-page-grid portfolio-grid-col-4',
    JSON_OBJECT(
      'layoutVariant', 'portfolio-grid-col-4',
      'filterStyle', 'tabs',
      'sourceComponent', 'portfolio-grid-col-2-area',
      'filterKeys', JSON_ARRAY('*', 'logo-kurumsal-kimlik', 'logo-tasarimi', 'kartvizit')
    )
) seed
JOIN component_types ct ON ct.uid = seed.component_type_uid
ON DUPLICATE KEY UPDATE
  component_type_id = VALUES(component_type_id),
  name = VALUES(name),
  display_order = VALUES(display_order),
  is_visible = VALUES(is_visible),
  style_classes = VALUES(style_classes),
  custom_data = VALUES(custom_data),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 4. COMPONENT_I18N
-- ============================================================

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, c.id, seed.language, seed.title, seed.subtitle, seed.description, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT
    'd7310001-0000-4000-8000-000000000001' AS uuid,
    'PortfolioPageHeroBlockTr' AS uid,
    'PortfolioPageHeroBlock' AS component_uid,
    'TR' AS language,
    'Portfolyo' AS title,
    'Logo tasarım ve kurumsal kimlik işleri' AS subtitle,
    'Kültür sanattan tarıma, inşaattan perakendeye uzanan markalar için hazırlanan seçili logo ve kurumsal kimlik çalışmalarını inceleyin.' AS description
  UNION ALL
  SELECT
    'd7310002-0000-4000-8000-000000000002',
    'PortfolioPageHeroBlockEn',
    'PortfolioPageHeroBlock',
    'EN',
    'Portfolio',
    'Logo design and corporate identity work',
    'Explore selected logo and identity projects created for brands across culture, agriculture, construction, retail and consultancy.'
  UNION ALL
  SELECT
    'd7310003-0000-4000-8000-000000000003',
    'PortfolioPageGridTr',
    'PortfolioPageGrid',
    'TR',
    'Seçili Projeler',
    'Gerçek marka işleri',
    'Ahmet Mülayim tarafından hazırlanan logo, kurumsal kimlik ve kartvizit çalışmalarından seçili örnekler.'
  UNION ALL
  SELECT
    'd7310004-0000-4000-8000-000000000004',
    'PortfolioPageGridEn',
    'PortfolioPageGrid',
    'EN',
    'Selected Projects',
    'Real brand work',
    'Selected logo design, identity and business card projects by Ahmet Mülayim.'
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
  SELECT 'd7320001-0000-4000-8000-000000000001' AS uuid, 'PortfolioPageCard01' AS uid, 'PortfolioPageGrid' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'd7320002-0000-4000-8000-000000000002', 'PortfolioPageCard02', 'PortfolioPageGrid', 1
  UNION ALL SELECT 'd7320003-0000-4000-8000-000000000003', 'PortfolioPageCard03', 'PortfolioPageGrid', 2
  UNION ALL SELECT 'd7320004-0000-4000-8000-000000000004', 'PortfolioPageCard04', 'PortfolioPageGrid', 3
  UNION ALL SELECT 'd7320005-0000-4000-8000-000000000005', 'PortfolioPageCard05', 'PortfolioPageGrid', 4
  UNION ALL SELECT 'd7320006-0000-4000-8000-000000000006', 'PortfolioPageCard06', 'PortfolioPageGrid', 5
  UNION ALL SELECT 'd7320007-0000-4000-8000-000000000007', 'PortfolioPageCard07', 'PortfolioPageGrid', 6
  UNION ALL SELECT 'd7320008-0000-4000-8000-000000000008', 'PortfolioPageCard08', 'PortfolioPageGrid', 7
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
  SELECT 'd7330001-0000-4000-8000-000000000001' AS uuid, 'PortfolioPageCard01Tr' AS uid, 'PortfolioPageCard01' AS entry_uid, 'TR' AS language, 'Babil Sanat Logo & Kurumsal Kimlik' AS title, 'Logo Tasarım ve Kurumsal Kimlik' AS description,
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'linkUrl', '/portfolio/babil-sanat-logo-and-kurumsal-kimlik', 'category', 'Logo & Kurumsal Kimlik', 'filterKeys', JSON_ARRAY('logo-kurumsal-kimlik')) AS custom_data
  UNION ALL SELECT 'd7330002-0000-4000-8000-000000000002', 'PortfolioPageCard01En', 'PortfolioPageCard01', 'EN', 'Babil Sanat Logo & Corporate Identity', 'Logo and Corporate Identity',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'linkUrl', '/portfolio/babil-sanat-logo-and-kurumsal-kimlik', 'category', 'Logo & Identity', 'filterKeys', JSON_ARRAY('logo-kurumsal-kimlik'))

  UNION ALL SELECT 'd7330003-0000-4000-8000-000000000003', 'PortfolioPageCard02Tr', 'PortfolioPageCard02', 'TR', 'Tufanlar Tohumculuk Logo', 'Logo Tasarımı',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'linkUrl', '/portfolio/tufanlar-tohumculuk-logo', 'category', 'Logo Tasarımı', 'filterKeys', JSON_ARRAY('logo-tasarimi'))
  UNION ALL SELECT 'd7330004-0000-4000-8000-000000000004', 'PortfolioPageCard02En', 'PortfolioPageCard02', 'EN', 'Tufanlar Tohumculuk Logo', 'Logo Design',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'linkUrl', '/portfolio/tufanlar-tohumculuk-logo', 'category', 'Logo Design', 'filterKeys', JSON_ARRAY('logo-tasarimi'))

  UNION ALL SELECT 'd7330005-0000-4000-8000-000000000005', 'PortfolioPageCard03Tr', 'PortfolioPageCard03', 'TR', 'Kns Dış Ticaret Danışmanlık Logo', 'Logo Tasarımı',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'linkUrl', '/portfolio/kns-dis-ticaret-danismanlik-logo', 'category', 'Logo Tasarımı', 'filterKeys', JSON_ARRAY('logo-tasarimi'))
  UNION ALL SELECT 'd7330006-0000-4000-8000-000000000006', 'PortfolioPageCard03En', 'PortfolioPageCard03', 'EN', 'Kns Foreign Trade Consultancy Logo', 'Logo Design',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'linkUrl', '/portfolio/kns-dis-ticaret-danismanlik-logo', 'category', 'Logo Design', 'filterKeys', JSON_ARRAY('logo-tasarimi'))

  UNION ALL SELECT 'd7330007-0000-4000-8000-000000000007', 'PortfolioPageCard04Tr', 'PortfolioPageCard04', 'TR', 'Armin Besi Logo', 'Logo Tasarımı',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'linkUrl', '/portfolio/armin-besi-logo', 'category', 'Logo Tasarımı', 'filterKeys', JSON_ARRAY('logo-tasarimi'))
  UNION ALL SELECT 'd7330008-0000-4000-8000-000000000008', 'PortfolioPageCard04En', 'PortfolioPageCard04', 'EN', 'Armin Besi Logo', 'Logo Design',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'linkUrl', '/portfolio/armin-besi-logo', 'category', 'Logo Design', 'filterKeys', JSON_ARRAY('logo-tasarimi'))

  UNION ALL SELECT 'd7330009-0000-4000-8000-000000000009', 'PortfolioPageCard05Tr', 'PortfolioPageCard05', 'TR', 'Işık Ticaret Askeri Malzeme Logo', 'Logo Revizyonu',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'linkUrl', '/portfolio/isik-ticaret-askeri-malzeme-logo', 'category', 'Logo Tasarımı', 'filterKeys', JSON_ARRAY('logo-tasarimi'))
  UNION ALL SELECT 'd7330010-0000-4000-8000-000000000010', 'PortfolioPageCard05En', 'PortfolioPageCard05', 'EN', 'Işık Ticaret Military Equipment Logo', 'Logo Refresh',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'linkUrl', '/portfolio/isik-ticaret-askeri-malzeme-logo', 'category', 'Logo Design', 'filterKeys', JSON_ARRAY('logo-tasarimi'))

  UNION ALL SELECT 'd7330011-0000-4000-8000-000000000011', 'PortfolioPageCard06Tr', 'PortfolioPageCard06', 'TR', 'Köktaş İnşaat Logo ve Kartvizit', 'Logo ve Kartvizit',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'linkUrl', '/portfolio/koktas-i-nsaat-logo-ve-kartvizit', 'category', 'Kurumsal Kimlik', 'filterKeys', JSON_ARRAY('logo-kurumsal-kimlik', 'kartvizit'))
  UNION ALL SELECT 'd7330012-0000-4000-8000-000000000012', 'PortfolioPageCard06En', 'PortfolioPageCard06', 'EN', 'Köktaş İnşaat Logo and Business Card', 'Logo and Business Card',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'linkUrl', '/portfolio/koktas-i-nsaat-logo-ve-kartvizit', 'category', 'Corporate Identity', 'filterKeys', JSON_ARRAY('logo-kurumsal-kimlik', 'kartvizit'))

  UNION ALL SELECT 'd7330013-0000-4000-8000-000000000013', 'PortfolioPageCard07Tr', 'PortfolioPageCard07', 'TR', 'Hasça Kuruyemiş - Kahve Logo Tasarımı', 'Logo Tasarımı',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'linkUrl', '/portfolio/hasca-kuruyemis-kahve-logo-tasarimi', 'category', 'Logo Tasarımı', 'filterKeys', JSON_ARRAY('logo-tasarimi'))
  UNION ALL SELECT 'd7330014-0000-4000-8000-000000000014', 'PortfolioPageCard07En', 'PortfolioPageCard07', 'EN', 'Hasça Nuts and Coffee Logo Design', 'Logo Design',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'linkUrl', '/portfolio/hasca-kuruyemis-kahve-logo-tasarimi', 'category', 'Logo Design', 'filterKeys', JSON_ARRAY('logo-tasarimi'))

  UNION ALL SELECT 'd7330015-0000-4000-8000-000000000015', 'PortfolioPageCard08Tr', 'PortfolioPageCard08', 'TR', 'Şen Turistik Logo Tasarımı', 'Logo Tasarımı',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'linkUrl', '/portfolio/sen-turistik-logo-tasarimi', 'category', 'Logo Tasarımı', 'filterKeys', JSON_ARRAY('logo-tasarimi'))
  UNION ALL SELECT 'd7330016-0000-4000-8000-000000000016', 'PortfolioPageCard08En', 'PortfolioPageCard08', 'EN', 'Şen Turistik Logo Design', 'Logo Design',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'linkUrl', '/portfolio/sen-turistik-logo-tasarimi', 'category', 'Logo Design', 'filterKeys', JSON_ARRAY('logo-tasarimi'))
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
-- 7. PORTFOLIO PAGE
-- ============================================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'd7340001-0000-4000-8000-000000000001', 'portfolio',
  (SELECT id FROM page_templates WHERE uid = 'LandingPageTemplate'),
  'PUBLISHED', 'LANDING', FALSE, 'INDEX_FOLLOW', NULL
ON DUPLICATE KEY UPDATE
  template_id = VALUES(template_id),
  status = VALUES(status),
  page_type = VALUES(page_type),
  is_home = VALUES(is_home),
  robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'd7350001-0000-4000-8000-000000000001', 'portfolio-tr', p.id, 'TR', 'Portfolyo', 'Portfolyo | Ahmet Mülayim', '/portfolio', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'portfolio'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'd7350002-0000-4000-8000-000000000002', 'portfolio-en', p.id, 'EN', 'Portfolio', 'Portfolio | Ahmet Mülayim', '/portfolio', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'portfolio'
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
WHERE p.uid = 'portfolio'
  AND ts.slot_name IN ('Section1', 'Section2')
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
WHERE p.uid = 'portfolio'
  AND ts.slot_name IN ('Section3', 'Section4', 'Section5', 'Section6', 'Section7', 'Section8')
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_active = FALSE,
  is_shared = FALSE,
  updated_at = NOW();

-- ============================================================
-- 9. SLOT_COMPONENTS
-- ============================================================

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, seed.sort_order, TRUE, NOW()
FROM (
  SELECT 'portfolio-Section1Slot' AS slot_uid, 'PortfolioPageHeroBlock' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'portfolio-Section2Slot', 'PortfolioPageGrid', 0
  UNION ALL SELECT 'portfolio-Section2Slot', 'PortfolioPageBrandStrip', 1
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

-- ============================================================
-- REQUIRED MEDIA UPLOADS
--   port-1.jpg through port-8.jpg
-- ============================================================
