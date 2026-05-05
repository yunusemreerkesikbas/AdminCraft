-- #CRAFTIVE_IMPEX
-- Mulayim contact page seed.
-- Run via Admin UI /{lang}/impex after theme/mulayim/mulayim_foundation.sql.
-- Seeds the /contact content page with a dedicated hero and a UI-first contact form section.
-- Shared header and footer chrome remain foundation-owned.
-- Idempotent: safe to run multiple times.

-- ============================================================
-- 1. COMPONENT TYPES
-- ============================================================

INSERT INTO component_types (uuid, uid, name, category, is_navigation_aware, created_at, updated_at)
VALUES
  (UUID(), 'SimpleBannerComponent', 'Banner', 'hero', FALSE, NOW(), NOW()),
  (UUID(), 'ContactFormSectionComponent', 'Contact Form Section', 'form', FALSE, NOW(), NOW())
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
SELECT ct.id, 'mapEmbedUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContactFormSectionComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'formTitle', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContactFormSectionComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'nameLabel', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContactFormSectionComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'namePlaceholder', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContactFormSectionComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'subjectLabel', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContactFormSectionComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'subjectPlaceholder', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContactFormSectionComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'messageLabel', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContactFormSectionComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'messagePlaceholder', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'ContactFormSectionComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'submitLabel', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContactFormSectionComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================================
-- 3. COMPONENTS
-- ============================================================

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, ct.id, seed.name, seed.display_order, TRUE, seed.style_classes, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT 'c7100001-0000-4000-8000-000000000001' AS uuid, 'MulayimContactHeroComponent' AS uid, 'SimpleBannerComponent' AS component_type_uid, 'Mulayim Contact Hero' AS name, 0 AS display_order, NULL AS style_classes
  UNION ALL
  SELECT 'c7100002-0000-4000-8000-000000000002', 'MulayimContactFormComponent', 'ContactFormSectionComponent', 'Mulayim Contact Form', 0, 'mulayim-contact-form'
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
  SELECT 'c7110001-0000-4000-8000-000000000001' AS uuid, 'MulayimContactHeroComponentTr' AS uid, 'MulayimContactHeroComponent' AS component_uid, 'TR' AS language,
    'Birlikte yeni bir marka hikayesi kuralım' AS title,
    'Mulayim Studio' AS subtitle,
    'Logo, kurumsal kimlik, sosyal medya ve kampanya tasarımları için proje kapsamınızı paylaşın. İlk çerçeveyi birlikte netleştirelim.' AS description
  UNION ALL
  SELECT 'c7110002-0000-4000-8000-000000000002', 'MulayimContactHeroComponentEn', 'MulayimContactHeroComponent', 'EN',
    'Let''s build the next chapter of your brand' ,
    'Mulayim Studio',
    'Share the scope of your logo, identity, social media or campaign design project and we can frame the brief together.'
  UNION ALL
  SELECT 'c7110003-0000-4000-8000-000000000003', 'MulayimContactFormComponentTr', 'MulayimContactFormComponent', 'TR',
    'İletişim',
    'Mesajınızı bırakın',
    'Yeni bir proje, teklif talebi ya da düzenli tasarım desteği için kısa bir özet yeterli. Bu ilk sürüm yalnızca arayüz yapısını hazırlar.'
  UNION ALL
  SELECT 'c7110004-0000-4000-8000-000000000004', 'MulayimContactFormComponentEn', 'MulayimContactFormComponent', 'EN',
    'Contact',
    'Leave a message',
    'A short outline is enough for a new project, a quotation request or ongoing design support. This first version prepares the UI structure only.'
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
  SELECT 'c7120002-0000-4000-8000-000000000002' AS uuid, 'MulayimContactFormPrimary' AS uid, 'MulayimContactFormComponent' AS component_uid, 0 AS sort_order
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
  SELECT 'c7130003-0000-4000-8000-000000000003' AS uuid, 'MulayimContactFormPrimaryTr' AS uid, 'MulayimContactFormPrimary' AS entry_uid, 'TR' AS language,
    NULL AS title,
    NULL AS description,
    JSON_OBJECT(
      'mapEmbedUrl', 'https://www.google.com/maps?q=Turkey&z=5&output=embed',
      'formTitle', 'Hemen Teklif Al',
      'nameLabel', 'İsim',
      'namePlaceholder', 'Adınız Soyadınız',
      'subjectLabel', 'Konu',
      'subjectPlaceholder', 'Markanız veya proje başlığınız',
      'messageLabel', 'Mesaj',
      'messagePlaceholder', 'İhtiyacınızı, teslim zamanını ve varsa kapsamı birkaç cümleyle paylaşın',
      'submitLabel', 'Teklif Al'
    ) AS custom_data
  UNION ALL
  SELECT 'c7130004-0000-4000-8000-000000000004', 'MulayimContactFormPrimaryEn', 'MulayimContactFormPrimary', 'EN',
    NULL,
    NULL,
    JSON_OBJECT(
      'mapEmbedUrl', 'https://www.google.com/maps?q=Turkey&z=5&output=embed',
      'formTitle', 'Send a Message',
      'nameLabel', 'Name',
      'namePlaceholder', 'Your full name',
      'subjectLabel', 'Subject',
      'subjectPlaceholder', 'Your brand or project title',
      'messageLabel', 'Message',
      'messagePlaceholder', 'Share the scope, timing and goals in a few sentences',
      'submitLabel', 'Prepare Message'
    )
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
-- 7. CONTACT PAGE
-- ============================================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'c7140001-0000-4000-8000-000000000001', 'contact',
  (SELECT id FROM page_templates WHERE uid = 'ContentPageTemplate'),
  'PUBLISHED', 'CONTENT', FALSE, 'INDEX_FOLLOW', NULL
ON DUPLICATE KEY UPDATE
  template_id = VALUES(template_id),
  status = VALUES(status),
  page_type = VALUES(page_type),
  is_home = VALUES(is_home),
  robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'c7150001-0000-4000-8000-000000000001', 'contact-tr', p.id, 'TR', 'İletişim', 'İletişim | Ahmet Mülayim', '/contact', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'contact'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'c7150002-0000-4000-8000-000000000002', 'contact-en', p.id, 'EN', 'Contact', 'Contact | Ahmet Mülayim', '/contact', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'contact'
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
WHERE p.uid = 'contact'
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
  SELECT 'contact-TopContentSlot' AS slot_uid, 'MulayimContactHeroComponent' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'contact-BodyContentSlot', 'MulayimContactFormComponent', 0
) seed
JOIN page_slots ps ON ps.uid = seed.slot_uid
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible);
