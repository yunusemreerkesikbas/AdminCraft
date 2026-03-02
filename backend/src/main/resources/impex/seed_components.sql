-- #ADMINCRAFT_IMPEX
-- Version-controlled ImpEx reference script.
-- Run via Admin UI /{lang}/impex when needed to seed component library demo data.
-- Idempotent: safe to run multiple times.
-- Prerequisite: component_types and entry_field_definitions must exist (Flyway R__seed_component_types, R__seed_entry_field_definitions).

-- ============================================
-- 1. COMPONENTS
-- ============================================

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

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a1000008-0000-4000-8000-000000000008', 'SeedLandingPortfolioGrid', id, 'Landing Portfolio Grid', 0, TRUE, 'portfolio-grid', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), style_classes = VALUES(style_classes), status = VALUES(status), updated_at = NOW();

-- ============================================
-- 2. COMPONENT_I18N (TR + EN)
-- ============================================

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

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000015-0000-4000-8000-000000000015', 'SeedLandingPortfolioGridTr', c.id, 'TR', 'Portfolyo', 'Seçili çalışmalar', 'Grafik tasarım projeleri vitrini', 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedLandingPortfolioGrid'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'a2000016-0000-4000-8000-000000000016', 'SeedLandingPortfolioGridEn', c.id, 'EN', 'Portfolio', 'Selected works', 'Graphic design project showcase', 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedLandingPortfolioGrid'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

-- ============================================
-- 3. COMPONENT_ENTRIES (Portfolio Grid items)
-- ============================================

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a3000001-0000-4000-8000-000000000001', 'SeedLandingPortfolioGridEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedLandingPortfolioGrid'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a3000002-0000-4000-8000-000000000002', 'SeedLandingPortfolioGridEntry2', c.id, 1, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedLandingPortfolioGrid'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a3000003-0000-4000-8000-000000000003', 'SeedLandingPortfolioGridEntry3', c.id, 2, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedLandingPortfolioGrid'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'a3000004-0000-4000-8000-000000000004', 'SeedLandingPortfolioGridEntry4', c.id, 3, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'SeedLandingPortfolioGrid'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), status = VALUES(status), updated_at = NOW();

-- ============================================
-- 4. COMPONENT_ENTRY_I18N (Portfolio Grid items)
-- ============================================

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'a4000001-0000-4000-8000-000000000001', 'SeedLandingPortfolioGridEntry1Tr', e.id, 'TR', 'Modern Kimlik Tasarımı', 'Kurumsal kimlik, logo ve tipografi sistemi.', 'PUBLISHED',
       JSON_OBJECT('imageUrl', 'https://images.unsplash.com/photo-1626785774573-4b799315345d?auto=format&fit=crop&w=1200&q=80', 'linkUrl', '/portfolio', 'category', 'Brand Identity', 'altText', 'Kurumsal kimlik örneği'),
       NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'SeedLandingPortfolioGridEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'a4000002-0000-4000-8000-000000000002', 'SeedLandingPortfolioGridEntry1En', e.id, 'EN', 'Modern Identity Design', 'Corporate identity, logo, and typography system.', 'PUBLISHED',
       JSON_OBJECT('imageUrl', 'https://images.unsplash.com/photo-1626785774573-4b799315345d?auto=format&fit=crop&w=1200&q=80', 'linkUrl', '/portfolio', 'category', 'Brand Identity', 'altText', 'Corporate identity showcase'),
       NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'SeedLandingPortfolioGridEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'a4000003-0000-4000-8000-000000000003', 'SeedLandingPortfolioGridEntry2Tr', e.id, 'TR', 'Poster Serisi', 'Müzik festivali için yüksek kontrast afiş serisi.', 'PUBLISHED',
       JSON_OBJECT('imageUrl', 'https://images.unsplash.com/photo-1633419461186-7d40a38105ec?auto=format&fit=crop&w=1200&q=80', 'linkUrl', '/portfolio', 'category', 'Poster Design', 'altText', 'Festival afiş tasarımı'),
       NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'SeedLandingPortfolioGridEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'a4000004-0000-4000-8000-000000000004', 'SeedLandingPortfolioGridEntry2En', e.id, 'EN', 'Poster Series', 'High-contrast poster series for a music festival.', 'PUBLISHED',
       JSON_OBJECT('imageUrl', 'https://images.unsplash.com/photo-1633419461186-7d40a38105ec?auto=format&fit=crop&w=1200&q=80', 'linkUrl', '/portfolio', 'category', 'Poster Design', 'altText', 'Festival poster layout'),
       NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'SeedLandingPortfolioGridEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'a4000005-0000-4000-8000-000000000005', 'SeedLandingPortfolioGridEntry3Tr', e.id, 'TR', 'Ambalaj Konsepti', 'Doğal kozmetik markası için ambalaj sistemi.', 'PUBLISHED',
       JSON_OBJECT('imageUrl', 'https://images.unsplash.com/photo-1545239351-1141bd82e8a6?auto=format&fit=crop&w=1200&q=80', 'linkUrl', '/portfolio', 'category', 'Packaging', 'altText', 'Ambalaj tasarımı örneği'),
       NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'SeedLandingPortfolioGridEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'a4000006-0000-4000-8000-000000000006', 'SeedLandingPortfolioGridEntry3En', e.id, 'EN', 'Packaging Concept', 'Packaging system for a natural cosmetics brand.', 'PUBLISHED',
       JSON_OBJECT('imageUrl', 'https://images.unsplash.com/photo-1545239351-1141bd82e8a6?auto=format&fit=crop&w=1200&q=80', 'linkUrl', '/portfolio', 'category', 'Packaging', 'altText', 'Packaging design showcase'),
       NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'SeedLandingPortfolioGridEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'a4000007-0000-4000-8000-000000000007', 'SeedLandingPortfolioGridEntry4Tr', e.id, 'TR', 'Web UI Koleksiyonu', 'SaaS dashboard ve landing page arayüzleri.', 'PUBLISHED',
       JSON_OBJECT('imageUrl', 'https://images.unsplash.com/photo-1467232004584-a241de8bcf5d?auto=format&fit=crop&w=1200&q=80', 'linkUrl', '/portfolio', 'category', 'Web UI', 'altText', 'Web arayüz tasarımı'),
       NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'SeedLandingPortfolioGridEntry4'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'a4000008-0000-4000-8000-000000000008', 'SeedLandingPortfolioGridEntry4En', e.id, 'EN', 'Web UI Collection', 'SaaS dashboards and landing page interfaces.', 'PUBLISHED',
       JSON_OBJECT('imageUrl', 'https://images.unsplash.com/photo-1467232004584-a241de8bcf5d?auto=format&fit=crop&w=1200&q=80', 'linkUrl', '/portfolio', 'category', 'Web UI', 'altText', 'Web interface showcase'),
       NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'SeedLandingPortfolioGridEntry4'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();
