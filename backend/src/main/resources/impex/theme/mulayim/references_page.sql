-- #CRAFTIVE_IMPEX
-- Mulayim references page seed.
-- Run via Admin UI /{lang}/impex after theme/mulayim/mulayim_foundation.sql.
-- Seeds the /references landing page with a dedicated hero, references logo wall and shared statement CTA.
-- Shared header and footer chrome remain foundation-owned.
-- Idempotent: safe to run multiple times.

-- ============================================================
-- 1. COMPONENT TYPES
-- ============================================================

INSERT INTO component_types (uuid, uid, name, category, is_navigation_aware, created_at, updated_at)
VALUES
  (UUID(), 'SimpleBannerComponent', 'Banner', 'hero', FALSE, NOW(), NOW()),
  (UUID(), 'ReferencesGridComponent', 'References Grid', 'content', FALSE, NOW(), NOW())
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
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ReferencesGridComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'altText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ReferencesGridComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ReferencesGridComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ReferencesGridComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================================
-- 3. SHARED STATEMENT CTA
-- ============================================================

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

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'f6200005-0000-4000-8000-000000000005', 'StatementCtaBlockTr', c.id, 'TR',
  'Markani guclendiren tasarim',
  'Logo, kurumsal kimlik ve kampanya tasarimini birlikte planlayalim',
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
  'Design that strengthens your brand',
  'Let us plan your logo, brand identity and campaign design together',
  NULL,
  'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StatementCtaBlock'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'f6300009-0000-4000-8000-000000000009', 'StatementCtaAction', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c
WHERE c.uid = 'StatementCtaBlock'
ON DUPLICATE KEY UPDATE
  component_id = VALUES(component_id),
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible),
  style_classes = VALUES(style_classes),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'f6400017-0000-4000-8000-000000000017', 'StatementCtaActionTr', e.id, 'TR', NULL, NULL, 'PUBLISHED',
  JSON_OBJECT('buttonText', 'Iletisime Gec', 'buttonUrl', '/contact'),
  NOW(), NOW(), NOW()
FROM component_entries e
WHERE e.uid = 'StatementCtaAction'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  status = VALUES(status),
  custom_data = VALUES(custom_data),
  published_at = VALUES(published_at),
  updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'f6400018-0000-4000-8000-000000000018', 'StatementCtaActionEn', e.id, 'EN', NULL, NULL, 'PUBLISHED',
  JSON_OBJECT('buttonText', 'Get in Touch', 'buttonUrl', '/contact'),
  NOW(), NOW(), NOW()
FROM component_entries e
WHERE e.uid = 'StatementCtaAction'
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  status = VALUES(status),
  custom_data = VALUES(custom_data),
  published_at = VALUES(published_at),
  updated_at = NOW();

-- ============================================================
-- 4. COMPONENTS
-- ============================================================

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, ct.id, seed.name, seed.display_order, TRUE, seed.style_classes, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT 'r7100001-0000-4000-8000-000000000001' AS uuid, 'ReferencesPageHeroBlock' AS uid, 'SimpleBannerComponent' AS component_type_uid, 'Mulayim References Hero' AS name, 0 AS display_order, NULL AS style_classes
  UNION ALL
  SELECT 'r7100002-0000-4000-8000-000000000002', 'ReferencesPageGrid', 'ReferencesGridComponent', 'Mulayim References Grid', 0, 'references-grid'
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
-- 5. COMPONENT_I18N
-- ============================================================

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, c.id, seed.language, seed.title, seed.subtitle, seed.description, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT 'r7110001-0000-4000-8000-000000000001' AS uuid, 'ReferencesPageHeroBlockTr' AS uid, 'ReferencesPageHeroBlock' AS component_uid, 'TR' AS language,
    'Referanslar' AS title,
    'Ahmet Mulayim' AS subtitle,
    'Logo, kurumsal kimlik, sosyal medya ve kampanya tasarimi alaninda birlikte calistigimiz markalardan secili bir duvar.' AS description
  UNION ALL
  SELECT 'r7110002-0000-4000-8000-000000000002', 'ReferencesPageHeroBlockEn', 'ReferencesPageHeroBlock', 'EN',
    'References',
    'Ahmet Mulayim',
    'A selected wall of brands we have supported across logo, identity, social media and campaign design work.'
  UNION ALL
  SELECT 'r7110003-0000-4000-8000-000000000003', 'ReferencesPageGridTr', 'ReferencesPageGrid', 'TR',
    'Calisilan markalar',
    'Secili logo duvari',
    'Referans logosu duvari ve iletisime gec daveti.'
  UNION ALL
  SELECT 'r7110004-0000-4000-8000-000000000004', 'ReferencesPageGridEn', 'ReferencesPageGrid', 'EN',
    'Brands worked with',
    'Selected logo wall',
    'Reference logo wall with a contact action below it.'
) seed
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 6. COMPONENT_ENTRIES
-- ============================================================

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, c.id, seed.sort_order, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT 'r7120001-0000-4000-8000-000000000001' AS uuid, 'ReferencesHeroPrimary' AS uid, 'ReferencesPageHeroBlock' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'r7120002-0000-4000-8000-000000000002', 'ReferencesLogo01', 'ReferencesPageGrid', 0
  UNION ALL SELECT 'r7120003-0000-4000-8000-000000000003', 'ReferencesLogo02', 'ReferencesPageGrid', 1
  UNION ALL SELECT 'r7120004-0000-4000-8000-000000000004', 'ReferencesLogo03', 'ReferencesPageGrid', 2
  UNION ALL SELECT 'r7120005-0000-4000-8000-000000000005', 'ReferencesLogo04', 'ReferencesPageGrid', 3
  UNION ALL SELECT 'r7120006-0000-4000-8000-000000000006', 'ReferencesLogo05', 'ReferencesPageGrid', 4
  UNION ALL SELECT 'r7120007-0000-4000-8000-000000000007', 'ReferencesLogo06', 'ReferencesPageGrid', 5
  UNION ALL SELECT 'r7120008-0000-4000-8000-000000000008', 'ReferencesLogo07', 'ReferencesPageGrid', 6
  UNION ALL SELECT 'r7120009-0000-4000-8000-000000000009', 'ReferencesLogo08', 'ReferencesPageGrid', 7
  UNION ALL SELECT 'r7120010-0000-4000-8000-000000000010', 'ReferencesLogo09', 'ReferencesPageGrid', 8
  UNION ALL SELECT 'r7120011-0000-4000-8000-000000000011', 'ReferencesLogo10', 'ReferencesPageGrid', 9
  UNION ALL SELECT 'r7120012-0000-4000-8000-000000000012', 'ReferencesLogo11', 'ReferencesPageGrid', 10
  UNION ALL SELECT 'r7120013-0000-4000-8000-000000000013', 'ReferencesLogo12', 'ReferencesPageGrid', 11
  UNION ALL SELECT 'r7120014-0000-4000-8000-000000000014', 'ReferencesLogo13', 'ReferencesPageGrid', 12
  UNION ALL SELECT 'r7120015-0000-4000-8000-000000000015', 'ReferencesLogo14', 'ReferencesPageGrid', 13
  UNION ALL SELECT 'r7120016-0000-4000-8000-000000000016', 'ReferencesLogo15', 'ReferencesPageGrid', 14
  UNION ALL SELECT 'r7120017-0000-4000-8000-000000000017', 'ReferencesGridAction', 'ReferencesPageGrid', 15
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
-- 7. COMPONENT_ENTRY_I18N
-- ============================================================

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT seed.uuid, seed.uid, e.id, seed.language, seed.title, seed.description, 'PUBLISHED', seed.custom_data, NOW(), NOW(), NOW()
FROM (
  SELECT 'r7130001-0000-4000-8000-000000000001' AS uuid, 'ReferencesHeroPrimaryTr' AS uid, 'ReferencesHeroPrimary' AS entry_uid, 'TR' AS language,
    NULL AS title,
    NULL AS description,
    JSON_OBJECT() AS custom_data
  UNION ALL
  SELECT 'r7130002-0000-4000-8000-000000000002', 'ReferencesHeroPrimaryEn', 'ReferencesHeroPrimary', 'EN',
    NULL,
    NULL,
    JSON_OBJECT()

  UNION ALL SELECT 'r7130003-0000-4000-8000-000000000003', 'ReferencesLogo01Tr', 'ReferencesLogo01', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-01', 'altText', '')
  UNION ALL SELECT 'r7130004-0000-4000-8000-000000000004', 'ReferencesLogo01En', 'ReferencesLogo01', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-01', 'altText', '')
  UNION ALL SELECT 'r7130005-0000-4000-8000-000000000005', 'ReferencesLogo02Tr', 'ReferencesLogo02', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-02', 'altText', '')
  UNION ALL SELECT 'r7130006-0000-4000-8000-000000000006', 'ReferencesLogo02En', 'ReferencesLogo02', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-02', 'altText', '')
  UNION ALL SELECT 'r7130007-0000-4000-8000-000000000007', 'ReferencesLogo03Tr', 'ReferencesLogo03', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-03', 'altText', '')
  UNION ALL SELECT 'r7130008-0000-4000-8000-000000000008', 'ReferencesLogo03En', 'ReferencesLogo03', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-03', 'altText', '')
  UNION ALL SELECT 'r7130009-0000-4000-8000-000000000009', 'ReferencesLogo04Tr', 'ReferencesLogo04', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-04', 'altText', '')
  UNION ALL SELECT 'r7130010-0000-4000-8000-000000000010', 'ReferencesLogo04En', 'ReferencesLogo04', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-04', 'altText', '')
  UNION ALL SELECT 'r7130011-0000-4000-8000-000000000011', 'ReferencesLogo05Tr', 'ReferencesLogo05', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-05', 'altText', '')
  UNION ALL SELECT 'r7130012-0000-4000-8000-000000000012', 'ReferencesLogo05En', 'ReferencesLogo05', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-05', 'altText', '')
  UNION ALL SELECT 'r7130013-0000-4000-8000-000000000013', 'ReferencesLogo06Tr', 'ReferencesLogo06', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-06', 'altText', '')
  UNION ALL SELECT 'r7130014-0000-4000-8000-000000000014', 'ReferencesLogo06En', 'ReferencesLogo06', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-06', 'altText', '')
  UNION ALL SELECT 'r7130015-0000-4000-8000-000000000015', 'ReferencesLogo07Tr', 'ReferencesLogo07', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-07', 'altText', '')
  UNION ALL SELECT 'r7130016-0000-4000-8000-000000000016', 'ReferencesLogo07En', 'ReferencesLogo07', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-07', 'altText', '')
  UNION ALL SELECT 'r7130017-0000-4000-8000-000000000017', 'ReferencesLogo08Tr', 'ReferencesLogo08', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-08', 'altText', '')
  UNION ALL SELECT 'r7130018-0000-4000-8000-000000000018', 'ReferencesLogo08En', 'ReferencesLogo08', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-08', 'altText', '')
  UNION ALL SELECT 'r7130019-0000-4000-8000-000000000019', 'ReferencesLogo09Tr', 'ReferencesLogo09', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-09', 'altText', '')
  UNION ALL SELECT 'r7130020-0000-4000-8000-000000000020', 'ReferencesLogo09En', 'ReferencesLogo09', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-09', 'altText', '')
  UNION ALL SELECT 'r7130021-0000-4000-8000-000000000021', 'ReferencesLogo10Tr', 'ReferencesLogo10', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-10', 'altText', '')
  UNION ALL SELECT 'r7130022-0000-4000-8000-000000000022', 'ReferencesLogo10En', 'ReferencesLogo10', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-10', 'altText', '')
  UNION ALL SELECT 'r7130023-0000-4000-8000-000000000023', 'ReferencesLogo11Tr', 'ReferencesLogo11', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-11', 'altText', '')
  UNION ALL SELECT 'r7130024-0000-4000-8000-000000000024', 'ReferencesLogo11En', 'ReferencesLogo11', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-11', 'altText', '')
  UNION ALL SELECT 'r7130025-0000-4000-8000-000000000025', 'ReferencesLogo12Tr', 'ReferencesLogo12', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-12', 'altText', '')
  UNION ALL SELECT 'r7130026-0000-4000-8000-000000000026', 'ReferencesLogo12En', 'ReferencesLogo12', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-12', 'altText', '')
  UNION ALL SELECT 'r7130027-0000-4000-8000-000000000027', 'ReferencesLogo13Tr', 'ReferencesLogo13', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-13', 'altText', '')
  UNION ALL SELECT 'r7130028-0000-4000-8000-000000000028', 'ReferencesLogo13En', 'ReferencesLogo13', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-13', 'altText', '')
  UNION ALL SELECT 'r7130029-0000-4000-8000-000000000029', 'ReferencesLogo14Tr', 'ReferencesLogo14', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-14', 'altText', '')
  UNION ALL SELECT 'r7130030-0000-4000-8000-000000000030', 'ReferencesLogo14En', 'ReferencesLogo14', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-14', 'altText', '')
  UNION ALL SELECT 'r7130031-0000-4000-8000-000000000031', 'ReferencesLogo15Tr', 'ReferencesLogo15', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-15', 'altText', '')
  UNION ALL SELECT 'r7130032-0000-4000-8000-000000000032', 'ReferencesLogo15En', 'ReferencesLogo15', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'references-brand-15', 'altText', '')

  UNION ALL SELECT 'r7130033-0000-4000-8000-000000000033', 'ReferencesGridActionTr', 'ReferencesGridAction', 'TR', NULL, NULL, JSON_OBJECT('buttonText', 'Iletisime Gec', 'buttonUrl', '/contact')
  UNION ALL SELECT 'r7130034-0000-4000-8000-000000000034', 'ReferencesGridActionEn', 'ReferencesGridAction', 'EN', NULL, NULL, JSON_OBJECT('buttonText', 'Get in Touch', 'buttonUrl', '/contact')
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
-- 8. REFERENCES PAGE
-- ============================================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'r7140001-0000-4000-8000-000000000001', 'references',
  (SELECT id FROM page_templates WHERE uid = 'LandingPageTemplate'),
  'PUBLISHED', 'LANDING', FALSE, 'INDEX_FOLLOW', NULL
ON DUPLICATE KEY UPDATE
  template_id = VALUES(template_id),
  status = VALUES(status),
  page_type = VALUES(page_type),
  is_home = VALUES(is_home),
  robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'r7150001-0000-4000-8000-000000000001', 'references-tr', p.id, 'TR', 'Referanslar', 'Referanslar | Ahmet Mulayim', '/references', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'references'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'r7150002-0000-4000-8000-000000000002', 'references-en', p.id, 'EN', 'References', 'References | Ahmet Mulayim', '/references', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'references'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

-- ============================================================
-- 9. PAGE_SLOTS
-- ============================================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT
  CASE ts.slot_name
    WHEN 'Section1' THEN 'refs0001-0000-4000-8000-000000000001'
    WHEN 'Section2' THEN 'refs0002-0000-4000-8000-000000000002'
    WHEN 'Section3' THEN 'refs0003-0000-4000-8000-000000000003'
  END,
  CONCAT(p.uid, '-', ts.slot_name, 'Slot'), p.id, ts.slot_name, ts.position, ts.sort_order, TRUE, FALSE, NOW(), NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid = 'references'
  AND ts.slot_name IN ('Section1', 'Section2', 'Section3')
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  is_shared = VALUES(is_shared),
  updated_at = NOW();

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT
  CASE ts.slot_name
    WHEN 'Section4' THEN 'refs0004-0000-4000-8000-000000000004'
    WHEN 'Section5' THEN 'refs0005-0000-4000-8000-000000000005'
    WHEN 'Section6' THEN 'refs0006-0000-4000-8000-000000000006'
    WHEN 'Section7' THEN 'refs0007-0000-4000-8000-000000000007'
    WHEN 'Section8' THEN 'refs0008-0000-4000-8000-000000000008'
  END,
  CONCAT(p.uid, '-', ts.slot_name, 'Slot'), p.id, ts.slot_name, ts.position, ts.sort_order, FALSE, FALSE, NOW(), NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid = 'references'
  AND ts.slot_name IN ('Section4', 'Section5', 'Section6', 'Section7', 'Section8')
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_active = FALSE,
  is_shared = FALSE,
  updated_at = NOW();

-- ============================================================
-- 10. SLOT_COMPONENTS
-- ============================================================

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, seed.sort_order, TRUE, NOW()
FROM (
  SELECT 'references-Section1Slot' AS slot_uid, 'ReferencesPageHeroBlock' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'references-Section2Slot', 'ReferencesPageGrid', 0
  UNION ALL SELECT 'references-Section3Slot', 'StatementCtaBlock', 0
) seed
JOIN page_slots ps ON ps.uid = seed.slot_uid
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible);

-- ============================================================
-- 11. MEDIA UID ALIGNMENT
-- ============================================================

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-1.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-01' AND existing.id <> m.id
SET m.uid = 'references-brand-01'
WHERE m.uid != 'references-brand-01' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-2.png') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-02' AND existing.id <> m.id
SET m.uid = 'references-brand-02'
WHERE m.uid != 'references-brand-02' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-3.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-03' AND existing.id <> m.id
SET m.uid = 'references-brand-03'
WHERE m.uid != 'references-brand-03' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-4.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-04' AND existing.id <> m.id
SET m.uid = 'references-brand-04'
WHERE m.uid != 'references-brand-04' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-5.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-05' AND existing.id <> m.id
SET m.uid = 'references-brand-05'
WHERE m.uid != 'references-brand-05' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-6.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-06' AND existing.id <> m.id
SET m.uid = 'references-brand-06'
WHERE m.uid != 'references-brand-06' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-7.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-07' AND existing.id <> m.id
SET m.uid = 'references-brand-07'
WHERE m.uid != 'references-brand-07' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-8.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-08' AND existing.id <> m.id
SET m.uid = 'references-brand-08'
WHERE m.uid != 'references-brand-08' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-9.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-09' AND existing.id <> m.id
SET m.uid = 'references-brand-09'
WHERE m.uid != 'references-brand-09' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-10.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-10' AND existing.id <> m.id
SET m.uid = 'references-brand-10'
WHERE m.uid != 'references-brand-10' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-11.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-11' AND existing.id <> m.id
SET m.uid = 'references-brand-11'
WHERE m.uid != 'references-brand-11' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-12.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-12' AND existing.id <> m.id
SET m.uid = 'references-brand-12'
WHERE m.uid != 'references-brand-12' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-13.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-13' AND existing.id <> m.id
SET m.uid = 'references-brand-13'
WHERE m.uid != 'references-brand-13' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-14.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-14' AND existing.id <> m.id
SET m.uid = 'references-brand-14'
WHERE m.uid != 'references-brand-14' AND existing.id IS NULL;

UPDATE media m
JOIN (SELECT MAX(id) AS id FROM media WHERE original_name = 'brand-15.jpg') picked ON picked.id = m.id
LEFT JOIN media existing ON existing.uid = 'references-brand-15' AND existing.id <> m.id
SET m.uid = 'references-brand-15'
WHERE m.uid != 'references-brand-15' AND existing.id IS NULL;
