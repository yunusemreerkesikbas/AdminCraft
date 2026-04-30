-- #CRAFTIVE_IMPEX
SET NAMES 'utf8mb4';
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;
-- Mulayim portfolio homepage seed.
-- Run via Admin UI /{lang}/impex after theme/mulayim/mulayim_foundation.sql and after media uploads.
-- Seeds the homepage body using the portfolio-grid-col-4 source layout.
-- Shared header and footer chrome remain foundation-owned.
-- Foundation owns globally reusable component types and field definitions, including BigTextCtaComponent.
-- Idempotent: safe to run multiple times.

-- ============================================================
-- 1. HOMEPAGE COMPONENT TYPES
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
SELECT ct.id, 'altText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'ariaLabel', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
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
  'Markanızı öne çıkaran güçlü tasarım',
  'Ahmet Mülayim',
  'Logo, kurumsal kimlik ve kampanya görselleriyle markanızın akılda kalmasını sağlayan net ve uygulanabilir tasarımlar üretiyoruz.',
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
  'Clear visual language for your brand',
  'Ahmet Mülayim',
  'We design logo, identity and campaign visuals that make your brand memorable and consistent.',
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
  'Seçili Çalışmalar',
  'Logo, kurumsal kimlik ve kampanya tasarımlarından öne çıkan işler',
  'Sektör bağımsız markalar için ürettiğimiz seçili logo ve kimlik projeleri.',
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
  'Selected Work',
  'Highlights from logo, identity and campaign projects',
  'A curated set of logo and brand identity projects delivered across different industries.',
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
  'Markanı güçlendiren tasarım',
  'Logo, kurumsal kimlik ve kampanya tasarımını birlikte planlayalım',
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
  SELECT 'f6400001-0000-4000-8000-000000000001' AS uuid, 'PortfolioCard01Tr' AS uid, 'PortfolioCard01' AS entry_uid, 'TR' AS language, 'Babil Sanat Logo & Kurumsal Kimlik' AS title, 'Logo Tasarım ve Kurumsal Kimlik' AS description,
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'linkUrl', '/portfolio/babil-sanat-logo-and-kurumsal-kimlik', 'category', 'Logo & Kurumsal Kimlik', 'altText', 'Babil Sanat için hazırlanan logo ve kurumsal kimlik tasarimi', 'ariaLabel', '') AS custom_data
  UNION ALL SELECT 'f6400002-0000-4000-8000-000000000002', 'PortfolioCard01En', 'PortfolioCard01', 'EN', 'Babil Sanat Logo & Corporate Identity', 'Logo and Corporate Identity',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'linkUrl', '/portfolio/babil-sanat-logo-and-kurumsal-kimlik', 'category', 'Logo & Identity', 'altText', 'Logo and brand identity design created for Babil Sanat', 'ariaLabel', '')

  UNION ALL SELECT 'f6400003-0000-4000-8000-000000000003', 'PortfolioCard02Tr', 'PortfolioCard02', 'TR', 'Tufanlar Tohumculuk Logo', 'Logo Tasarımı',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'linkUrl', '/portfolio/tufanlar-tohumculuk-logo', 'category', 'Logo Tasarımı', 'altText', 'Tufanlar Tohumculuk marka kimligini guclendiren logo tasarimi', 'ariaLabel', '')
  UNION ALL SELECT 'f6400004-0000-4000-8000-000000000004', 'PortfolioCard02En', 'PortfolioCard02', 'EN', 'Tufanlar Tohumculuk Logo', 'Logo Design',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'linkUrl', '/portfolio/tufanlar-tohumculuk-logo', 'category', 'Logo Design', 'altText', 'Logo design for Tufanlar Tohumculuk brand', 'ariaLabel', '')

  UNION ALL SELECT 'f6400005-0000-4000-8000-000000000005', 'PortfolioCard03Tr', 'PortfolioCard03', 'TR', 'Kns Dış Ticaret Danışmanlık Logo', 'Logo Tasarımı',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'linkUrl', '/portfolio/kns-dis-ticaret-danismanlik-logo', 'category', 'Logo Tasarımı', 'altText', 'Kns Dis Ticaret Danismanlik icin tasarlanan logo calismasi', 'ariaLabel', '')
  UNION ALL SELECT 'f6400006-0000-4000-8000-000000000006', 'PortfolioCard03En', 'PortfolioCard03', 'EN', 'Kns Foreign Trade Consultancy Logo', 'Logo Design',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'linkUrl', '/portfolio/kns-dis-ticaret-danismanlik-logo', 'category', 'Logo Design', 'altText', 'Logo design created for Kns Foreign Trade Consultancy', 'ariaLabel', '')

  UNION ALL SELECT 'f6400007-0000-4000-8000-000000000007', 'PortfolioCard04Tr', 'PortfolioCard04', 'TR', 'Armin Besi Logo', 'Yalin ve ayirt edici logo tasarimi',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'linkUrl', '/portfolio/armin-besi-logo', 'category', 'Logo Tasarımı', 'altText', 'Armin Besi markasi icin gelistirilen logo tasarimi', 'ariaLabel', '')
  UNION ALL SELECT 'f6400008-0000-4000-8000-000000000008', 'PortfolioCard04En', 'PortfolioCard04', 'EN', 'Armin Besi Logo', 'Logo Design',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'linkUrl', '/portfolio/armin-besi-logo', 'category', 'Logo Design', 'altText', 'Logo design developed for Armin Besi', 'ariaLabel', '')

  UNION ALL SELECT 'f6400009-0000-4000-8000-000000000009', 'PortfolioCard05Tr', 'PortfolioCard05', 'TR', 'Işık Ticaret Askeri Malzeme Logo', 'Mevcut markayi guclendiren logo revizyonu',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'linkUrl', '/portfolio/isik-ticaret-askeri-malzeme-logo', 'category', 'Logo Tasarımı', 'altText', 'Isik Ticaret markasi icin uygulanan logo revizyonu', 'ariaLabel', '')
  UNION ALL SELECT 'f6400010-0000-4000-8000-000000000010', 'PortfolioCard05En', 'PortfolioCard05', 'EN', 'Işık Ticaret Military Equipment Logo', 'Logo Refresh',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'linkUrl', '/portfolio/isik-ticaret-askeri-malzeme-logo', 'category', 'Logo Design', 'altText', 'Logo refresh project for Isik Ticaret', 'ariaLabel', '')

  UNION ALL SELECT 'f6400011-0000-4000-8000-000000000011', 'PortfolioCard06Tr', 'PortfolioCard06', 'TR', 'Köktaş İnşaat Logo ve Kartvizit', 'Sahada guven veren logo ve kartvizit tasarimi',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'linkUrl', '/portfolio/koktas-i-nsaat-logo-ve-kartvizit', 'category', 'Kurumsal Kimlik', 'altText', 'Koktas Insaat icin hazirlanan logo ve kartvizit tasarimi', 'ariaLabel', '')
  UNION ALL SELECT 'f6400012-0000-4000-8000-000000000012', 'PortfolioCard06En', 'PortfolioCard06', 'EN', 'Köktaş İnşaat Logo and Business Card', 'Logo and Business Card',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'linkUrl', '/portfolio/koktas-i-nsaat-logo-ve-kartvizit', 'category', 'Corporate Identity', 'altText', 'Logo and business card design for Koktas Insaat', 'ariaLabel', '')

  UNION ALL SELECT 'f6400013-0000-4000-8000-000000000013', 'PortfolioCard07Tr', 'PortfolioCard07', 'TR', 'Hasça Kuruyemiş - Kahve Logo Tasarımı', 'Raflarda ayirt edilen logo tasarimi',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'linkUrl', '/portfolio/hasca-kuruyemis-kahve-logo-tasarimi', 'category', 'Logo Tasarımı', 'altText', 'Hasca Kuruyemis Kahve markasi icin logo tasarimi', 'ariaLabel', '')
  UNION ALL SELECT 'f6400014-0000-4000-8000-000000000014', 'PortfolioCard07En', 'PortfolioCard07', 'EN', 'Hasça Nuts and Coffee Logo Design', 'Logo Design',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'linkUrl', '/portfolio/hasca-kuruyemis-kahve-logo-tasarimi', 'category', 'Logo Design', 'altText', 'Logo design project for Hasca Nuts and Coffee', 'ariaLabel', '')

  UNION ALL SELECT 'f6400015-0000-4000-8000-000000000015', 'PortfolioCard08Tr', 'PortfolioCard08', 'TR', 'Şen Turistik Logo Tasarımı', 'Turizm markasi icin guven veren logo kurgusu',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'linkUrl', '/portfolio/sen-turistik-logo-tasarimi', 'category', 'Logo Tasarımı', 'altText', 'Sen Turistik markasi icin hazirlanan logo tasarimi', 'ariaLabel', '')
  UNION ALL SELECT 'f6400016-0000-4000-8000-000000000016', 'PortfolioCard08En', 'PortfolioCard08', 'EN', 'Şen Turistik Logo Design', 'Logo Design',
    JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'linkUrl', '/portfolio/sen-turistik-logo-tasarimi', 'category', 'Logo Design', 'altText', 'Logo design for Sen Turistik', 'ariaLabel', '')

  UNION ALL SELECT 'f6400017-0000-4000-8000-000000000017', 'StatementCtaActionTr', 'StatementCtaAction', 'TR', NULL, NULL,
    JSON_OBJECT('buttonText', 'İletişime Geç', 'buttonUrl', '/contact')
  UNION ALL SELECT 'f6400018-0000-4000-8000-000000000018', 'StatementCtaActionEn', 'StatementCtaAction', 'EN', NULL, NULL,
    JSON_OBJECT('buttonText', 'Contact', 'buttonUrl', '/contact')
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
SELECT UUID(), 'homepage-tr', p.id, 'TR', 'Anasayfa', 'Ahmet Mülayim | Grafik Tasarım', '/', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'homepage'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT UUID(), 'homepage-en', p.id, 'EN', 'Homepage', 'Ahmet Mülayim | Graphic Design', '/', 'PUBLISHED'
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
SELECT ps.id, c.id, 1, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section2Slot' AND c.uid = 'PortfolioPageBrandStrip'
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
