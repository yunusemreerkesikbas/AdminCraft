-- SAP Commerce Cloud Hybris Style Page Templates
-- Updated: Removed cart/checkout/order templates, PascalCase UIDs, random UUIDs
-- Default templates editable by tenant admins (is_system = FALSE)
-- i18n compatible (V19+)

-- ============================================
-- 1. SYSTEM PAGE TEMPLATES (CMS Core)
-- ============================================
INSERT INTO page_templates (uuid, uid, is_system, is_active)
VALUES
    -- Core Content Templates
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567001', 'LandingPageTemplate', FALSE, TRUE),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678002', 'ContentPageTemplate', FALSE, TRUE),
    ('c3d4e5f6-a7b8-9012-cdef-123456789003', 'CategoryPageTemplate', FALSE, TRUE),
    ('d4e5f6a7-b8c9-0123-def0-234567890004', 'ProductDetailsPageTemplate', FALSE, TRUE),
    ('e5f6a7b8-c9d0-1234-ef01-345678901005', 'SearchResultsPageTemplate', FALSE, TRUE),
    -- Account & Auth
    ('f6a7b8c9-d0e1-2345-f012-456789012006', 'AccountPageTemplate', FALSE, TRUE),
    ('a7b8c9d0-e1f2-3456-0123-567890123007', 'LoginPageTemplate', FALSE, TRUE),
    -- Error Pages
    ('b8c9d0e1-f2a3-4567-1234-678901234008', 'ErrorPageTemplate', FALSE, TRUE),
    ('c9d0e1f2-a3b4-5678-2345-789012345009', 'NotFoundPageTemplate', FALSE, TRUE)
ON DUPLICATE KEY UPDATE is_system = VALUES(is_system), is_active = VALUES(is_active);

-- ============================================
-- 2. TEMPLATE I18N - Turkish (TR)
-- ============================================
INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'd0e1f2a3-b4c5-6789-3456-890123456101', 'LandingPageTemplateTr', id, 'TR', 
    'Landing Sayfası Şablonu', 'Kampanya ve promosyon sayfaları için ana giriş şablonu'
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'e1f2a3b4-c5d6-7890-4567-901234567102', 'ContentPageTemplateTr', id, 'TR', 
    'İçerik Sayfası Şablonu', 'Hakkımızda, İletişim, Blog gibi statik içerik sayfaları'
FROM page_templates WHERE uid = 'ContentPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'f2a3b4c5-d6e7-8901-5678-012345678103', 'CategoryPageTemplateTr', id, 'TR', 
    'Kategori Sayfası Şablonu', 'Ürün kategorisi listeleme ve filtreleme'
FROM page_templates WHERE uid = 'CategoryPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'a3b4c5d6-e7f8-9012-6789-123456789104', 'ProductDetailsPageTemplateTr', id, 'TR', 
    'Ürün Detay Sayfası Şablonu', 'Ürün bilgileri, görselleri ve satın alma seçenekleri'
FROM page_templates WHERE uid = 'ProductDetailsPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'b4c5d6e7-f8a9-0123-7890-234567890105', 'SearchResultsPageTemplateTr', id, 'TR', 
    'Arama Sonuçları Şablonu', 'Arama sonuçları listeleme sayfası'
FROM page_templates WHERE uid = 'SearchResultsPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'c5d6e7f8-a9b0-1234-8901-345678901106', 'AccountPageTemplateTr', id, 'TR', 
    'Hesap Sayfası Şablonu', 'Kullanıcı hesap yönetimi sayfası'
FROM page_templates WHERE uid = 'AccountPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'd6e7f8a9-b0c1-2345-9012-456789012107', 'LoginPageTemplateTr', id, 'TR', 
    'Giriş Sayfası Şablonu', 'Üye girişi ve kayıt sayfası'
FROM page_templates WHERE uid = 'LoginPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'e7f8a9b0-c1d2-3456-0123-567890123108', 'ErrorPageTemplateTr', id, 'TR', 
    'Hata Sayfası Şablonu', 'Genel hata sayfası (500)'
FROM page_templates WHERE uid = 'ErrorPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'f8a9b0c1-d2e3-4567-1234-678901234109', 'NotFoundPageTemplateTr', id, 'TR', 
    'Sayfa Bulunamadı Şablonu', '404 - Sayfa bulunamadı hata sayfası'
FROM page_templates WHERE uid = 'NotFoundPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- ============================================
-- 3. TEMPLATE I18N - English (EN)
-- ============================================
INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'a9b0c1d2-e3f4-5678-2345-789012345201', 'LandingPageTemplateEn', id, 'EN', 
    'Landing Page Template', 'Main entry template for campaigns and promotions'
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'b0c1d2e3-f4a5-6789-3456-890123456202', 'ContentPageTemplateEn', id, 'EN', 
    'Content Page Template', 'Static content pages like About, Contact, Blog'
FROM page_templates WHERE uid = 'ContentPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'c1d2e3f4-a5b6-7890-4567-901234567203', 'CategoryPageTemplateEn', id, 'EN', 
    'Category Page Template', 'Product category listing and filtering'
FROM page_templates WHERE uid = 'CategoryPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'd2e3f4a5-b6c7-8901-5678-012345678204', 'ProductDetailsPageTemplateEn', id, 'EN', 
    'Product Details Page Template', 'Product information, images and purchase options'
FROM page_templates WHERE uid = 'ProductDetailsPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'e3f4a5b6-c7d8-9012-6789-123456789205', 'SearchResultsPageTemplateEn', id, 'EN', 
    'Search Results Template', 'Search results listing page'
FROM page_templates WHERE uid = 'SearchResultsPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'f4a5b6c7-d8e9-0123-7890-234567890206', 'AccountPageTemplateEn', id, 'EN', 
    'Account Page Template', 'User account management page'
FROM page_templates WHERE uid = 'AccountPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'a5b6c7d8-e9f0-1234-8901-345678901207', 'LoginPageTemplateEn', id, 'EN', 
    'Login Page Template', 'Member login and registration page'
FROM page_templates WHERE uid = 'LoginPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'b6c7d8e9-f0a1-2345-9012-456789012208', 'ErrorPageTemplateEn', id, 'EN', 
    'Error Page Template', 'General error page (500)'
FROM page_templates WHERE uid = 'ErrorPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT 'c7d8e9f0-a1b2-3456-0123-567890123209', 'NotFoundPageTemplateEn', id, 'EN', 
    'Not Found Page Template', '404 - Page not found error page'
FROM page_templates WHERE uid = 'NotFoundPageTemplate'
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- ============================================
-- 4. TEMPLATE SLOTS (Hybris ContentSlot Pattern)
-- These define WHERE components can be placed in each template
-- ============================================

-- LandingPageTemplate Slots: multi-section (Section1 TOP, Section2 CENTER, Section3 BOTTOM)
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'd8e9f0a1-b2c3-4567-1234-678901234301', 'LandingPageSection1Slot', id, 'Section1', 'TOP', 1, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE uid = VALUES(uid), slot_name = VALUES(slot_name), position = VALUES(position), sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'dd000002-0000-4000-8000-000000000002', 'LandingPageSection2Slot', id, 'Section2', 'CENTER', 2, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'dd000003-0000-4000-8000-000000000003', 'LandingPageSection3Slot', id, 'Section3', 'BOTTOM', 3, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- ContentPageTemplate Slots (Main content with optional sidebar)
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'a1b2c3d4-e5f6-7890-4567-901234567304', 'ContentPageTopSlot', id, 'TopContent', 'TOP', 0, FALSE 
FROM page_templates WHERE uid = 'ContentPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'b2c3d4e5-f6a7-8901-5678-012345678305', 'ContentPageBodySlot', id, 'BodyContent', 'CENTER', 1, TRUE 
FROM page_templates WHERE uid = 'ContentPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'c3d4e5f6-a7b8-9012-6789-123456789306', 'ContentPageSideSlot', id, 'SideContent', 'RIGHT', 2, FALSE 
FROM page_templates WHERE uid = 'ContentPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- CategoryPageTemplate Slots (Filters + Grid)
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'd4e5f6a7-b8c9-0123-7890-234567890307', 'CategoryPageTopSlot', id, 'TopContent', 'TOP', 0, FALSE 
FROM page_templates WHERE uid = 'CategoryPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'e5f6a7b8-c9d0-1234-8901-345678901308', 'CategoryPageContentSlot', id, 'ProductGrid', 'CENTER', 1, TRUE 
FROM page_templates WHERE uid = 'CategoryPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- ProductDetailsPageTemplate Slots
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'f6a7b8c9-d0e1-2345-9012-456789012309', 'ProductDetailsSummarySlot', id, 'Summary', 'TOP', 0, TRUE 
FROM page_templates WHERE uid = 'ProductDetailsPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'a7b8c9d0-e1f2-3456-0123-567890123310', 'ProductDetailsTabsSlot', id, 'Tabs', 'CENTER', 1, FALSE 
FROM page_templates WHERE uid = 'ProductDetailsPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'b8c9d0e1-f2a3-4567-1234-678901234311', 'ProductDetailsCrossSellingSlot', id, 'CrossSelling', 'BOTTOM', 2, FALSE 
FROM page_templates WHERE uid = 'ProductDetailsPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- SearchResultsPageTemplate Slots
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'c9d0e1f2-a3b4-5678-2345-789012345312', 'SearchResultsTopSlot', id, 'TopContent', 'TOP', 0, FALSE 
FROM page_templates WHERE uid = 'SearchResultsPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'd0e1f2a3-b4c5-6789-3456-890123456313', 'SearchResultsContentSlot', id, 'Results', 'CENTER', 1, TRUE 
FROM page_templates WHERE uid = 'SearchResultsPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- AccountPageTemplate Slots
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'e1f2a3b4-c5d6-7890-4567-901234567314', 'AccountPageBodySlot', id, 'BodyContent', 'CENTER', 0, TRUE 
FROM page_templates WHERE uid = 'AccountPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- LoginPageTemplate Slots
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'f2a3b4c5-d6e7-8901-5678-012345678315', 'LoginPageLeftSlot', id, 'LeftContent', 'LEFT', 0, FALSE 
FROM page_templates WHERE uid = 'LoginPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'a3b4c5d6-e7f8-9012-6789-123456789316', 'LoginPageRightSlot', id, 'RightContent', 'RIGHT', 1, FALSE 
FROM page_templates WHERE uid = 'LoginPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- ErrorPageTemplate Slots
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'b4c5d6e7-f8a9-0123-7890-234567890317', 'ErrorPageMiddleSlot', id, 'MiddleContent', 'CENTER', 0, TRUE 
FROM page_templates WHERE uid = 'ErrorPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- NotFoundPageTemplate Slots
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'c5d6e7f8-a9b0-1234-8901-345678901318', 'NotFoundPageMiddleSlot', id, 'MiddleContent', 'CENTER', 0, TRUE
FROM page_templates WHERE uid = 'NotFoundPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- ============================================
-- 5. CHROME SLOTS: Header (TOP, sort_order=-1) + Footer (BOTTOM, sort_order=99)
--    Chrome templates: Landing, Content, Category, ProductDetails, SearchResults, Account
--    Excluded (no chrome): Login, Error, NotFound
-- ============================================

-- Header Slots
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000001-0000-4000-8000-000000000001', 'LandingPageHeaderSlot', id, 'Header', 'TOP', -1, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000002-0000-4000-8000-000000000002', 'ContentPageHeaderSlot', id, 'Header', 'TOP', -1, FALSE
FROM page_templates WHERE uid = 'ContentPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000003-0000-4000-8000-000000000003', 'CategoryPageHeaderSlot', id, 'Header', 'TOP', -1, FALSE
FROM page_templates WHERE uid = 'CategoryPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000004-0000-4000-8000-000000000004', 'ProductDetailsPageHeaderSlot', id, 'Header', 'TOP', -1, FALSE
FROM page_templates WHERE uid = 'ProductDetailsPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000005-0000-4000-8000-000000000005', 'SearchResultsPageHeaderSlot', id, 'Header', 'TOP', -1, FALSE
FROM page_templates WHERE uid = 'SearchResultsPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000006-0000-4000-8000-000000000006', 'AccountPageHeaderSlot', id, 'Header', 'TOP', -1, FALSE
FROM page_templates WHERE uid = 'AccountPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Footer Slots
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000007-0000-4000-8000-000000000007', 'LandingPageFooterSlot', id, 'Footer', 'BOTTOM', 99, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000008-0000-4000-8000-000000000008', 'ContentPageFooterSlot', id, 'Footer', 'BOTTOM', 99, FALSE
FROM page_templates WHERE uid = 'ContentPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000009-0000-4000-8000-000000000009', 'CategoryPageFooterSlot', id, 'Footer', 'BOTTOM', 99, FALSE
FROM page_templates WHERE uid = 'CategoryPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000010-0000-4000-8000-000000000010', 'ProductDetailsPageFooterSlot', id, 'Footer', 'BOTTOM', 99, FALSE
FROM page_templates WHERE uid = 'ProductDetailsPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000011-0000-4000-8000-000000000011', 'SearchResultsPageFooterSlot', id, 'Footer', 'BOTTOM', 99, FALSE
FROM page_templates WHERE uid = 'SearchResultsPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000012-0000-4000-8000-000000000012', 'AccountPageFooterSlot', id, 'Footer', 'BOTTOM', 99, FALSE
FROM page_templates WHERE uid = 'AccountPageTemplate'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);
