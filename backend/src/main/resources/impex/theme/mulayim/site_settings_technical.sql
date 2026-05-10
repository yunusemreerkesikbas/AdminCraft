-- #CRAFTIVE_IMPEX
-- Mulayim site settings + technical settings seed.
-- Run via Admin UI /{lang}/impex after theme/mulayim/mulayim_foundation.sql.
-- Idempotent: safe to run multiple times.

-- ============================================================
-- 1. GLOBAL SITE SETTINGS (language = NULL)
-- ============================================================
-- MySQL UNIQUE index treats each NULL as distinct, so ON DUPLICATE KEY UPDATE
-- never fires for NULL-language rows. Use DELETE + INSERT instead.

INSERT INTO site_settings (
  setting_key, setting_value, language, setting_type, category, display_name, is_public, sort_order, updated_by
)
VALUES
  ('global.contactEmail', 'info@ahmetmulayim.com', NULL, 'TEXT', 'general', 'Contact Email', FALSE, 10, NULL),
  ('global.contactPhone', '05394204255', NULL, 'TEXT', 'general', 'Contact Phone', FALSE, 11, NULL),
  ('global.whatsappPhone', '05394204255', NULL, 'TEXT', 'general', 'WhatsApp Phone', FALSE, 12, NULL),
  ('global.canonicalBaseUrl', 'https://www.ahmetmulayim.com', NULL, 'URL', 'seo', 'Canonical Base URL', FALSE, 20, NULL),
  ('global.robots', 'index,follow', NULL, 'TEXT', 'seo', 'Default Robots', FALSE, 21, NULL),
  ('global.address.line1', 'Körfez mahallesi ıhlamur caddesi no:39 Kleopatra evleri kat:4 daire no:20', NULL, 'TEXT', 'general', 'Address Line 1', FALSE, 30, NULL),
  ('global.address.line2', '', NULL, 'TEXT', 'general', 'Address Line 2', FALSE, 31, NULL),
  ('global.address.city', 'Atakum', NULL, 'TEXT', 'general', 'City', FALSE, 32, NULL),
  ('global.address.state', 'Samsun', NULL, 'TEXT', 'general', 'State', FALSE, 33, NULL),
  ('global.address.postalCode', '55200', NULL, 'TEXT', 'general', 'Postal Code', FALSE, 34, NULL),
  ('global.address.country', 'Türkiye', NULL, 'TEXT', 'general', 'Country', FALSE, 35, NULL),
  ('global.address.mapEmbedUrl', '', NULL, 'TEXT', 'general', 'Map Embed URL', FALSE, 36, NULL),
  ('global.social.instagram', 'https://www.instagram.com', NULL, 'TEXT', 'social', 'Instagram URL', FALSE, 40, NULL),
  ('global.social.youtube', 'https://www.youtube.com', NULL, 'TEXT', 'social', 'YouTube URL', FALSE, 41, NULL),
  ('global.social.linkedin', '', NULL, 'TEXT', 'social', 'LinkedIn URL', FALSE, 42, NULL),
  ('global.social.facebook', '', NULL, 'TEXT', 'social', 'Facebook URL', FALSE, 43, NULL),
  ('global.social.x', '', NULL, 'TEXT', 'social', 'X URL', FALSE, 44, NULL),
  ('global.social.tiktok', '', NULL, 'TEXT', 'social', 'TikTok URL', FALSE, 45, NULL);

-- ============================================================
-- 2. I18N SITE SETTINGS (TR / EN)
-- ============================================================

INSERT INTO site_settings (
  setting_key, setting_value, language, setting_type, category, display_name, is_public, sort_order, updated_by
)
VALUES
  ('i18n.siteName', 'Ahmet Mülayim', 'TR', 'I18N_TEXT', 'general', 'Site Name (TR)', FALSE, 100, NULL),
  ('i18n.tagline', 'Logo, kurumsal kimlik ve kampanya görselleriyle markalar için net işler tasarlıyoruz.', 'TR', 'I18N_TEXT', 'general', 'Tagline (TR)', FALSE, 101, NULL),
  ('i18n.seo.title', 'Ahmet Mülayim | Grafik Tasarım ve Kurumsal Kimlik', 'TR', 'I18N_TEXT', 'seo', 'SEO Title (TR)', FALSE, 110, NULL),
  ('i18n.seo.description', 'Logo tasarımı, kurumsal kimlik ve kampanya görselleri ile markalar için yaratıcı grafik tasarım hizmetleri.', 'TR', 'I18N_TEXT', 'seo', 'SEO Description (TR)', FALSE, 111, NULL),
  ('i18n.seo.keywords', 'grafik tasarım,logo tasarımı,kurumsal kimlik,ahmet mülayim', 'TR', 'I18N_TEXT', 'seo', 'SEO Keywords (TR)', FALSE, 112, NULL),
  ('i18n.seo.ogTitle', 'Ahmet Mülayim | Grafik Tasarım', 'TR', 'I18N_TEXT', 'seo', 'OG Title (TR)', FALSE, 113, NULL),
  ('i18n.seo.ogDescription', 'Logo, kurumsal kimlik ve kampanya tasarımı çalışmaları.', 'TR', 'I18N_TEXT', 'seo', 'OG Description (TR)', FALSE, 114, NULL),
  ('i18n.seo.twitterCard', 'summary_large_image', 'TR', 'I18N_TEXT', 'seo', 'Twitter Card (TR)', FALSE, 115, NULL),
  ('i18n.seo.titleSeparator', ' | ', 'TR', 'I18N_TEXT', 'seo', 'SEO Title Separator (TR)', FALSE, 116, NULL),
  ('i18n.cookie.consent.text', 'Bu web sitesi deneyiminizi iyileştirmek için çerezleri kullanır.', 'TR', 'I18N_TEXT', 'general', 'Cookie Consent Text (TR)', FALSE, 117, NULL),

  ('i18n.siteName', 'Ahmet Mülayim', 'EN', 'I18N_TEXT', 'general', 'Site Name (EN)', FALSE, 200, NULL),
  ('i18n.tagline', 'Graphic design studio for logo, identity and campaign work.', 'EN', 'I18N_TEXT', 'general', 'Tagline (EN)', FALSE, 201, NULL),
  ('i18n.seo.title', 'Ahmet Mülayim | Graphic Design and Brand Identity', 'EN', 'I18N_TEXT', 'seo', 'SEO Title (EN)', FALSE, 210, NULL),
  ('i18n.seo.description', 'Graphic design studio focused on logo design, brand identity and campaign visuals.', 'EN', 'I18N_TEXT', 'seo', 'SEO Description (EN)', FALSE, 211, NULL),
  ('i18n.seo.keywords', 'graphic design,logo design,brand identity,ahmet mulayim', 'EN', 'I18N_TEXT', 'seo', 'SEO Keywords (EN)', FALSE, 212, NULL),
  ('i18n.seo.ogTitle', 'Ahmet Mülayim | Graphic Design', 'EN', 'I18N_TEXT', 'seo', 'OG Title (EN)', FALSE, 213, NULL),
  ('i18n.seo.ogDescription', 'Logo, brand identity and campaign design works.', 'EN', 'I18N_TEXT', 'seo', 'OG Description (EN)', FALSE, 214, NULL),
  ('i18n.seo.twitterCard', 'summary_large_image', 'EN', 'I18N_TEXT', 'seo', 'Twitter Card (EN)', FALSE, 215, NULL),
  ('i18n.seo.titleSeparator', ' | ', 'EN', 'I18N_TEXT', 'seo', 'SEO Title Separator (EN)', FALSE, 216, NULL),
  ('i18n.cookie.consent.text', 'This website uses cookies to improve your experience.', 'EN', 'I18N_TEXT', 'general', 'Cookie Consent Text (EN)', FALSE, 217, NULL)
ON DUPLICATE KEY UPDATE
  setting_value = VALUES(setting_value),
  setting_type = VALUES(setting_type),
  category = VALUES(category),
  display_name = VALUES(display_name),
  is_public = VALUES(is_public),
  sort_order = VALUES(sort_order),
  updated_by = VALUES(updated_by),
  updated_at = NOW();

-- ============================================================
-- 3. SITE TECHNICAL SETTINGS (first site in tenant DB)
-- ============================================================

INSERT INTO site_technical_settings (
  site_id, robots_txt, sitemap_enabled, indexing_enabled, cookie_consent_enabled, created_by, updated_by
)
SELECT
  first_site.id,
  'User-agent: *\nAllow: /\n\nSitemap: https://www.ahmetmulayim.com/sitemap.xml\n',
  TRUE,
  TRUE,
  FALSE,
  NULL,
  NULL
FROM (
  SELECT id FROM sites ORDER BY id ASC LIMIT 1
) first_site
ON DUPLICATE KEY UPDATE
  robots_txt = VALUES(robots_txt),
  sitemap_enabled = VALUES(sitemap_enabled),
  indexing_enabled = VALUES(indexing_enabled),
  cookie_consent_enabled = VALUES(cookie_consent_enabled),
  updated_by = VALUES(updated_by),
  updated_at = NOW();
