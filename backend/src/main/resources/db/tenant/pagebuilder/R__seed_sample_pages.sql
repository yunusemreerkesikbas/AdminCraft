-- System pages seed — runs on every checksum change (repeatable migration)
-- These pages are created in every tenant DB at provisioning time.
-- Storefront requires these pages to be present for CMS delivery to work.
-- Uses ON DUPLICATE KEY UPDATE so checksum changes reconcile seed data in existing tenants.

-- ============================================
-- 1. HOMEPAGE (is_home=true, type=LANDING)
-- ============================================
INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT
    'f0000001-0000-0000-0000-000000000001',
    'homepage',
    (SELECT id FROM page_templates WHERE uid = 'LandingPageTemplate'),
    'PUBLISHED',
    'LANDING',
    TRUE,
    'INDEX_FOLLOW',
    NULL
ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), status = VALUES(status),
    page_type = VALUES(page_type), is_home = VALUES(is_home), robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT
    'f0000001-0000-0000-0001-000000000001',
    'homepage-tr',
    (SELECT id FROM pages WHERE uid = 'homepage'),
    'TR',
    'Anasayfa',
    'Hoş Geldiniz',
    '/',
    'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT
    'f0000001-0000-0000-0001-000000000002',
    'homepage-en',
    (SELECT id FROM pages WHERE uid = 'homepage'),
    'EN',
    'Homepage',
    'Welcome',
    '/',
    'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

-- ============================================
-- 2. PRODUCT PAGE TEMPLATE (type=PRODUCT)
-- ============================================
INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT
    'f0000002-0000-0000-0000-000000000001',
    'productPage',
    (SELECT id FROM page_templates WHERE uid = 'ProductDetailsPageTemplate'),
    'PUBLISHED',
    'PRODUCT',
    FALSE,
    'INDEX_FOLLOW',
    NULL
ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), status = VALUES(status),
    page_type = VALUES(page_type), is_home = VALUES(is_home), robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT
    'f0000002-0000-0000-0001-000000000001',
    'productPage-tr',
    (SELECT id FROM pages WHERE uid = 'productPage'),
    'TR',
    'Ürün Detayı',
    'Ürün Detayı',
    '/products/{code}',
    'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT
    'f0000002-0000-0000-0001-000000000002',
    'productPage-en',
    (SELECT id FROM pages WHERE uid = 'productPage'),
    'EN',
    'Product Details',
    'Product Details',
    '/products/{code}',
    'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

-- ============================================
-- 3. CATEGORY PAGE TEMPLATE (type=CATEGORY)
-- ============================================
INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT
    'f0000003-0000-0000-0000-000000000001',
    'categoryPage',
    (SELECT id FROM page_templates WHERE uid = 'CategoryPageTemplate'),
    'PUBLISHED',
    'CATEGORY',
    FALSE,
    'INDEX_FOLLOW',
    NULL
ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), status = VALUES(status),
    page_type = VALUES(page_type), is_home = VALUES(is_home), robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT
    'f0000003-0000-0000-0001-000000000001',
    'categoryPage-tr',
    (SELECT id FROM pages WHERE uid = 'categoryPage'),
    'TR',
    'Kategori',
    'Ürünler',
    '/c/{code}',
    'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT
    'f0000003-0000-0000-0001-000000000002',
    'categoryPage-en',
    (SELECT id FROM pages WHERE uid = 'categoryPage'),
    'EN',
    'Category',
    'Products',
    '/c/{code}',
    'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

-- ============================================
-- 4. SEARCH PAGE TEMPLATE (type=SEARCH)
-- ============================================
INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT
    'f0000004-0000-0000-0000-000000000001',
    'searchResultsPage',
    (SELECT id FROM page_templates WHERE uid = 'SearchResultsPageTemplate'),
    'PUBLISHED',
    'SEARCH',
    FALSE,
    'NOINDEX_FOLLOW',
    NULL
ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), status = VALUES(status),
    page_type = VALUES(page_type), is_home = VALUES(is_home), robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT
    'f0000004-0000-0000-0001-000000000001',
    'searchResultsPage-tr',
    (SELECT id FROM pages WHERE uid = 'searchResultsPage'),
    'TR',
    'Arama Sonuçları',
    'Arama Sonuçları',
    '/search',
    'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT
    'f0000004-0000-0000-0001-000000000002',
    'searchResultsPage-en',
    (SELECT id FROM pages WHERE uid = 'searchResultsPage'),
    'EN',
    'Search Results',
    'Search Results',
    '/search',
    'PUBLISHED'
ON DUPLICATE KEY UPDATE page_id = VALUES(page_id), name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);
