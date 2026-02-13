-- Seed data for CMS components (component instances + i18n)
-- Repeatable migration: runs after R__seed_component_types and R__seed_entry_field_definitions
-- Used by CMS delivery API; component_types must exist.

-- ============================================
-- 1. COMPONENTS (fixed UIDs for slot_components reference)
-- ============================================

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a1000001-0000-4000-8000-000000000001', 'SeedHeaderComponent', id, 'Site Header', 0, TRUE, 'site-header', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'HeaderComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a1000002-0000-4000-8000-000000000002', 'SeedHeroBanner', id, 'Hero Banner', 0, TRUE, 'hero-banner', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a1000003-0000-4000-8000-000000000003', 'SeedWelcomeParagraph', id, 'Welcome Text', 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'CMSParagraphComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a1000004-0000-4000-8000-000000000004', 'SeedCtaShopNow', id, 'Shop Now CTA', 0, TRUE, 'btn-primary', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a1000005-0000-4000-8000-000000000005', 'SeedSection2Banner', id, 'Section 2 Banner', 1, TRUE, 'section-banner', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a1000006-0000-4000-8000-000000000006', 'SeedProductSummaryCta', id, 'View Details', 0, TRUE, 'btn-secondary', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

-- ============================================
-- 2. COMPONENT_I18N (TR + EN, PUBLISHED for delivery)
-- ============================================

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000001-0000-4000-8000-000000000001', 'SeedHeaderComponentTr', c.id, 'TR', 'Site Header', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedHeaderComponent'
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000002-0000-4000-8000-000000000002', 'SeedHeaderComponentEn', c.id, 'EN', 'Site Header', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedHeaderComponent'
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000003-0000-4000-8000-000000000003', 'SeedHeroBannerTr', c.id, 'TR', 'Hoş Geldiniz', 'Kampanyaları keşfedin', NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedHeroBanner'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000004-0000-4000-8000-000000000004', 'SeedHeroBannerEn', c.id, 'EN', 'Welcome', 'Discover our campaigns', NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedHeroBanner'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000005-0000-4000-8000-000000000005', 'SeedWelcomeParagraphTr', c.id, 'TR', 'Hoş geldiniz metni', NULL, 'Anasayfa için karşılama paragrafı.', 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedWelcomeParagraph'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000006-0000-4000-8000-000000000006', 'SeedWelcomeParagraphEn', c.id, 'EN', 'Welcome text', NULL, 'Homepage welcome paragraph.', 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedWelcomeParagraph'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000007-0000-4000-8000-000000000007', 'SeedCtaShopNowTr', c.id, 'TR', 'Alışverişe Başla', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedCtaShopNow'
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000008-0000-4000-8000-000000000008', 'SeedCtaShopNowEn', c.id, 'EN', 'Shop Now', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedCtaShopNow'
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000009-0000-4000-8000-000000000009', 'SeedSection2BannerTr', c.id, 'TR', 'İkinci Bölüm', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedSection2Banner'
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000010-0000-4000-8000-000000000010', 'SeedSection2BannerEn', c.id, 'EN', 'Section Two', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedSection2Banner'
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000011-0000-4000-8000-000000000011', 'SeedProductSummaryCtaTr', c.id, 'TR', 'Detayları Gör', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedProductSummaryCta'
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000012-0000-4000-8000-000000000012', 'SeedProductSummaryCtaEn', c.id, 'EN', 'View Details', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedProductSummaryCta'
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status), updated_at = NOW();
