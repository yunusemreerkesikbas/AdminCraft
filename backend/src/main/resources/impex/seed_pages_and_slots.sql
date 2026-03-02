-- #ADMINCRAFT_IMPEX
-- Version-controlled ImpEx reference script.
-- Run via Admin UI /{lang}/impex when needed to seed sample pages, slots and slot assignments.
-- Idempotent: safe to run multiple times.
-- Prerequisites (run in order):
--   1. seed_components.sql  — components must exist for slot_components assignments
--   2. This file            — pages, page_slots, slot_components, shared slots
-- Flyway prerequisites: page_templates and template_slots must exist (R__seed_page_templates).

-- ============================================
-- 1. PAGES
-- ============================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'f0000001-0000-0000-0000-000000000001', 'homepage',
    (SELECT id FROM page_templates WHERE uid = 'LandingPageTemplate'),
    'PUBLISHED', 'LANDING', TRUE, 'INDEX_FOLLOW',
    (SELECT id FROM users WHERE is_system = TRUE ORDER BY id ASC LIMIT 1)
ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), status = VALUES(status),
    page_type = VALUES(page_type), is_home = VALUES(is_home), robot_tag = VALUES(robot_tag);

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'f0000002-0000-0000-0000-000000000001', 'productPage',
    (SELECT id FROM page_templates WHERE uid = 'ProductDetailsPageTemplate'),
    'PUBLISHED', 'PRODUCT', FALSE, 'INDEX_FOLLOW',
    (SELECT id FROM users WHERE is_system = TRUE ORDER BY id ASC LIMIT 1)
ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), status = VALUES(status),
    page_type = VALUES(page_type), is_home = VALUES(is_home), robot_tag = VALUES(robot_tag);

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'f0000003-0000-0000-0000-000000000001', 'categoryPage',
    (SELECT id FROM page_templates WHERE uid = 'CategoryPageTemplate'),
    'PUBLISHED', 'CATEGORY', FALSE, 'INDEX_FOLLOW',
    (SELECT id FROM users WHERE is_system = TRUE ORDER BY id ASC LIMIT 1)
ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), status = VALUES(status),
    page_type = VALUES(page_type), is_home = VALUES(is_home), robot_tag = VALUES(robot_tag);

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'f0000004-0000-0000-0000-000000000001', 'searchResultsPage',
    (SELECT id FROM page_templates WHERE uid = 'SearchResultsPageTemplate'),
    'PUBLISHED', 'SEARCH', FALSE, 'NOINDEX_FOLLOW',
    (SELECT id FROM users WHERE is_system = TRUE ORDER BY id ASC LIMIT 1)
ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), status = VALUES(status),
    page_type = VALUES(page_type), is_home = VALUES(is_home), robot_tag = VALUES(robot_tag);

-- ============================================
-- 2. PAGE_I18N (TR + EN per page)
-- ============================================

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000001-0000-0000-0001-000000000001', 'homepage-tr',
    (SELECT id FROM pages WHERE uid = 'homepage'),
    'TR', 'Anasayfa', 'Hoş Geldiniz', '/', 'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000001-0000-0000-0001-000000000002', 'homepage-en',
    (SELECT id FROM pages WHERE uid = 'homepage'),
    'EN', 'Homepage', 'Welcome', '/', 'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000002-0000-0000-0001-000000000001', 'productPage-tr',
    (SELECT id FROM pages WHERE uid = 'productPage'),
    'TR', 'Ürün Detayı', 'Ürün Detayı', '/products/{code}', 'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000002-0000-0000-0001-000000000002', 'productPage-en',
    (SELECT id FROM pages WHERE uid = 'productPage'),
    'EN', 'Product Details', 'Product Details', '/products/{code}', 'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000003-0000-0000-0001-000000000001', 'categoryPage-tr',
    (SELECT id FROM pages WHERE uid = 'categoryPage'),
    'TR', 'Kategori', 'Ürünler', '/c/{code}', 'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000003-0000-0000-0001-000000000002', 'categoryPage-en',
    (SELECT id FROM pages WHERE uid = 'categoryPage'),
    'EN', 'Category', 'Products', '/c/{code}', 'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000004-0000-0000-0001-000000000001', 'searchResultsPage-tr',
    (SELECT id FROM pages WHERE uid = 'searchResultsPage'),
    'TR', 'Arama Sonuçları', 'Arama Sonuçları', '/search', 'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000004-0000-0000-0001-000000000002', 'searchResultsPage-en',
    (SELECT id FROM pages WHERE uid = 'searchResultsPage'),
    'EN', 'Search Results', 'Search Results', '/search', 'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

-- ============================================
-- 3. PAGE_SLOTS (auto-generated from template_slots per page)
-- ============================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT UUID(), CONCAT(p.uid, '-', ts.slot_name, 'Slot'), p.id, ts.slot_name, ts.position, ts.sort_order, TRUE, FALSE, NOW(), NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid IN ('homepage', 'productPage', 'categoryPage', 'searchResultsPage')
ON DUPLICATE KEY UPDATE position = VALUES(position), sort_order = VALUES(sort_order), updated_at = NOW();

-- ============================================
-- 4. SHARED SLOTS (is_shared=TRUE, page_id=NULL — chrome Header/Footer)
-- ============================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
VALUES ('bb000001-0000-4000-8000-000000000001', 'SharedHeaderSlot', NULL, 'Header', 'TOP', -1, TRUE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
VALUES ('bb000002-0000-4000-8000-000000000002', 'SharedFooterSlot', NULL, 'Footer', 'BOTTOM', 99, TRUE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- ============================================
-- 5. SLOT_COMPONENTS (wire seed components into page slots)
-- ============================================

-- Homepage Section1 → Portfolio Grid
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section1Slot' AND c.uid = 'SeedLandingPortfolioGrid'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Product Details Summary → CTA
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'productPage-SummarySlot' AND c.uid = 'SeedProductSummaryCta'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Category Top → Hero Banner
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'categoryPage-TopContentSlot' AND c.uid = 'SeedHeroBanner'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Search Results Top → Welcome Paragraph
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'searchResultsPage-TopContentSlot' AND c.uid = 'SeedWelcomeParagraph'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);
