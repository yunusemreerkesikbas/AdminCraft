-- #CRAFTIVE_IMPEX
-- Liko theme foundation.
-- Run via Admin UI /{lang}/impex before any page-specific Liko imports.
-- Seeds theme-owned page templates, template slots, shared chrome components and navigation data.
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
ON DUPLICATE KEY UPDATE uid = VALUES(uid), slot_name = VALUES(slot_name), position = VALUES(position), sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'dd000003-0000-4000-8000-000000000003', 'LandingPageSection3Slot', id, 'Section3', 'BOTTOM', 3, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE uid = VALUES(uid), slot_name = VALUES(slot_name), position = VALUES(position), sort_order = VALUES(sort_order);

-- Section4 (CENTER, 4) — Services
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'dd000004-0000-4000-8000-000000000004', 'LandingPageSection4Slot', id, 'Section4', 'CENTER', 4, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE uid = VALUES(uid), slot_name = VALUES(slot_name), position = VALUES(position), sort_order = VALUES(sort_order);

-- Section5 (CENTER, 5) — Projects
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'dd000005-0000-4000-8000-000000000005', 'LandingPageSection5Slot', id, 'Section5', 'CENTER', 5, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE uid = VALUES(uid), slot_name = VALUES(slot_name), position = VALUES(position), sort_order = VALUES(sort_order);

-- Section6 (BOTTOM, 6) — Awards
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'dd000006-0000-4000-8000-000000000006', 'LandingPageSection6Slot', id, 'Section6', 'BOTTOM', 6, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE uid = VALUES(uid), slot_name = VALUES(slot_name), position = VALUES(position), sort_order = VALUES(sort_order);

-- Section7 (BOTTOM, 7) — Marquee
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'dd000007-0000-4000-8000-000000000007', 'LandingPageSection7Slot', id, 'Section7', 'BOTTOM', 7, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE uid = VALUES(uid), slot_name = VALUES(slot_name), position = VALUES(position), sort_order = VALUES(sort_order);

-- Section8 (BOTTOM, 8) — Instagram
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'dd000008-0000-4000-8000-000000000008', 'LandingPageSection8Slot', id, 'Section8', 'BOTTOM', 8, FALSE
FROM page_templates WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE uid = VALUES(uid), slot_name = VALUES(slot_name), position = VALUES(position), sort_order = VALUES(sort_order);

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

-- Shared storefront chrome components for the Liko theme.
-- Run via Admin UI /{lang}/impex as part of `theme/liko/liko_foundation.sql`.
-- Idempotent: safe to run multiple times.
-- Prerequisites:
--   1. component_types seeded by Flyway
-- Note: page-specific Liko scripts depend on these chrome components.

-- UUID ranges:
--   d1 = components
--   d2 = component_i18n
--   d3 = entries
--   d4 = entry_i18n

-- ============================================================
-- 1. COMPONENTS
-- ============================================================

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT 'd1000001-0000-4000-8000-000000000001', 'StorefrontHeaderMainNavigation', id, 'Storefront Header Main Navigation', 0, TRUE, 'header-main-navigation', JSON_OBJECT('layoutRole', 'header.mainNavigation'), 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'NavigationComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), style_classes = VALUES(style_classes), custom_data = VALUES(custom_data), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT 'd1000002-0000-4000-8000-000000000002', 'StorefrontHeaderSocialLinks', id, 'Storefront Header Social Links', 1, TRUE, 'header-social-links', JSON_OBJECT('layoutRole', 'header.socialLinks'), 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), style_classes = VALUES(style_classes), custom_data = VALUES(custom_data), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT 'd1000003-0000-4000-8000-000000000003', 'StorefrontHeaderContactInfo', id, 'Storefront Header Contact Info', 2, TRUE, 'header-contact-info', JSON_OBJECT('layoutRole', 'header.contactInfo'), 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), style_classes = VALUES(style_classes), custom_data = VALUES(custom_data), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT 'd1000004-0000-4000-8000-000000000004', 'StorefrontFooterBrandBlock', id, 'Storefront Footer Brand Block', 0, TRUE, 'footer-brand-block', JSON_OBJECT('layoutRole', 'footer.brandBlock'), 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'CMSParagraphComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), style_classes = VALUES(style_classes), custom_data = VALUES(custom_data), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT 'd1000005-0000-4000-8000-000000000005', 'StorefrontFooterSitemapNavigation', id, 'Storefront Footer Sitemap Navigation', 1, TRUE, 'footer-sitemap-navigation', JSON_OBJECT('layoutRole', 'footer.sitemapNavigation'), 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'NavigationComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), style_classes = VALUES(style_classes), custom_data = VALUES(custom_data), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT 'd1000006-0000-4000-8000-000000000006', 'StorefrontFooterOfficeLinks', id, 'Storefront Footer Office Links', 2, TRUE, 'footer-office-links', JSON_OBJECT('layoutRole', 'footer.officeLinks'), 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), style_classes = VALUES(style_classes), custom_data = VALUES(custom_data), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT 'd1000007-0000-4000-8000-000000000007', 'StorefrontFooterNewsletter', id, 'Storefront Footer Newsletter', 3, TRUE, 'footer-newsletter', JSON_OBJECT('layoutRole', 'footer.newsletter'), 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), style_classes = VALUES(style_classes), custom_data = VALUES(custom_data), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT 'd1000008-0000-4000-8000-000000000008', 'StorefrontFooterSocialLinks', id, 'Storefront Footer Social Links', 4, TRUE, 'footer-social-links', JSON_OBJECT('layoutRole', 'footer.socialLinks'), 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), style_classes = VALUES(style_classes), custom_data = VALUES(custom_data), updated_at = NOW();

-- ============================================================
-- 2. COMPONENT_I18N (localized TR / EN content)
-- ============================================================

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000001-0000-4000-8000-000000000001', 'StorefrontHeaderMainNavigationTr', c.id, 'TR',
    'Ana Menü', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderMainNavigation'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000002-0000-4000-8000-000000000002', 'StorefrontHeaderMainNavigationEn', c.id, 'EN',
    'Main Menu', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderMainNavigation'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000003-0000-4000-8000-000000000003', 'StorefrontHeaderSocialLinksTr', c.id, 'TR',
    'Sosyal', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderSocialLinks'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000004-0000-4000-8000-000000000004', 'StorefrontHeaderSocialLinksEn', c.id, 'EN',
    'Social', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderSocialLinks'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000005-0000-4000-8000-000000000005', 'StorefrontHeaderContactInfoTr', c.id, 'TR',
    'İletişim', NULL, 'Sorularınız için bize ulaşın.', 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderContactInfo'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000006-0000-4000-8000-000000000006', 'StorefrontHeaderContactInfoEn', c.id, 'EN',
    'Contact', NULL, 'Get in touch.', 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderContactInfo'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000007-0000-4000-8000-000000000007', 'StorefrontFooterBrandBlockTr', c.id, 'TR',
    NULL, NULL, 'Bize yazın, sed id semper risus in hend rerit.', 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterBrandBlock'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000008-0000-4000-8000-000000000008', 'StorefrontFooterBrandBlockEn', c.id, 'EN',
    NULL, NULL, 'Drop us a line sed id semper risus in hend rerit.', 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterBrandBlock'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000009-0000-4000-8000-000000000009', 'StorefrontFooterSitemapNavigationTr', c.id, 'TR',
    'Site Haritası', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterSitemapNavigation'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000010-0000-4000-8000-000000000010', 'StorefrontFooterSitemapNavigationEn', c.id, 'EN',
    'Sitemap', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterSitemapNavigation'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000011-0000-4000-8000-000000000011', 'StorefrontFooterOfficeLinksTr', c.id, 'TR',
    'Ofis', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterOfficeLinks'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000012-0000-4000-8000-000000000012', 'StorefrontFooterOfficeLinksEn', c.id, 'EN',
    'Office', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterOfficeLinks'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000013-0000-4000-8000-000000000013', 'StorefrontFooterNewsletterTr', c.id, 'TR',
    'Bültenimize Abone Olun', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterNewsletter'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000014-0000-4000-8000-000000000014', 'StorefrontFooterNewsletterEn', c.id, 'EN',
    'Subscribe to our newsletter', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterNewsletter'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000015-0000-4000-8000-000000000015', 'StorefrontFooterSocialLinksTr', c.id, 'TR',
    'Sosyal', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterSocialLinks'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'd2000016-0000-4000-8000-000000000016', 'StorefrontFooterSocialLinksEn', c.id, 'EN',
    'Social', NULL, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterSocialLinks'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), updated_at = NOW();

-- ============================================================
-- 3. COMPONENT_ENTRIES
-- ============================================================

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000001-0000-4000-8000-000000000001', 'StorefrontHeaderSocialLinksEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000002-0000-4000-8000-000000000002', 'StorefrontHeaderSocialLinksEntry2', c.id, 1, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000003-0000-4000-8000-000000000003', 'StorefrontHeaderSocialLinksEntry3', c.id, 2, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000004-0000-4000-8000-000000000004', 'StorefrontHeaderSocialLinksEntry4', c.id, 3, FALSE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000005-0000-4000-8000-000000000005', 'StorefrontHeaderContactInfoEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderContactInfo'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000006-0000-4000-8000-000000000006', 'StorefrontHeaderContactInfoEntry2', c.id, 1, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontHeaderContactInfo'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000007-0000-4000-8000-000000000007', 'StorefrontFooterOfficeLinksEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterOfficeLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000008-0000-4000-8000-000000000008', 'StorefrontFooterOfficeLinksEntry2', c.id, 1, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterOfficeLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000009-0000-4000-8000-000000000009', 'StorefrontFooterOfficeLinksEntry3', c.id, 2, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterOfficeLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000010-0000-4000-8000-000000000010', 'StorefrontFooterNewsletterEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterNewsletter'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000011-0000-4000-8000-000000000011', 'StorefrontFooterSocialLinksEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000012-0000-4000-8000-000000000012', 'StorefrontFooterSocialLinksEntry2', c.id, 1, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'd3000013-0000-4000-8000-000000000013', 'StorefrontFooterSocialLinksEntry3', c.id, 2, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'StorefrontFooterSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

-- ============================================================
-- 4. ENTRY_I18N (localized TR / EN content)
-- ============================================================

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000001-0000-4000-8000-000000000001', 'StorefrontHeaderSocialLinksEntry1Tr', e.id, 'TR',
    'LinkedIn', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'https://www.linkedin.com', 'target', '_blank'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderSocialLinksEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000002-0000-4000-8000-000000000002', 'StorefrontHeaderSocialLinksEntry1En', e.id, 'EN',
    'LinkedIn', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'https://www.linkedin.com', 'target', '_blank'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderSocialLinksEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000003-0000-4000-8000-000000000003', 'StorefrontHeaderSocialLinksEntry2Tr', e.id, 'TR',
    'Instagram', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'https://www.instagram.com', 'target', '_blank'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderSocialLinksEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000004-0000-4000-8000-000000000004', 'StorefrontHeaderSocialLinksEntry2En', e.id, 'EN',
    'Instagram', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'https://www.instagram.com', 'target', '_blank'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderSocialLinksEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000005-0000-4000-8000-000000000005', 'StorefrontHeaderSocialLinksEntry3Tr', e.id, 'TR',
    'Behance', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'https://www.behance.net', 'target', '_blank'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderSocialLinksEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000006-0000-4000-8000-000000000006', 'StorefrontHeaderSocialLinksEntry3En', e.id, 'EN',
    'Behance', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'https://www.behance.net', 'target', '_blank'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderSocialLinksEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000007-0000-4000-8000-000000000007', 'StorefrontHeaderSocialLinksEntry4Tr', e.id, 'TR',
    'Dribbble', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'https://dribbble.com', 'target', '_blank'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderSocialLinksEntry4'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000008-0000-4000-8000-000000000008', 'StorefrontHeaderSocialLinksEntry4En', e.id, 'EN',
    'Dribbble', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'https://dribbble.com', 'target', '_blank'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderSocialLinksEntry4'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000009-0000-4000-8000-000000000009', 'StorefrontHeaderContactInfoEntry1Tr', e.id, 'TR',
    '+ 725 214 456', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'tel:+725214456'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderContactInfoEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000010-0000-4000-8000-000000000010', 'StorefrontHeaderContactInfoEntry1En', e.id, 'EN',
    '+ 725 214 456', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'tel:+725214456'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderContactInfoEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000011-0000-4000-8000-000000000011', 'StorefrontHeaderContactInfoEntry2Tr', e.id, 'TR',
    'contact@liko.com', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'mailto:contact@liko.com'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderContactInfoEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000012-0000-4000-8000-000000000012', 'StorefrontHeaderContactInfoEntry2En', e.id, 'EN',
    'contact@liko.com', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'mailto:contact@liko.com'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontHeaderContactInfoEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000013-0000-4000-8000-000000000013', 'StorefrontFooterOfficeLinksEntry1Tr', e.id, 'TR',
    '740 NEW SOUTH HEAD RD, TRIPLE BAY SWFW 3108, NEW YORK', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'https://www.google.com/maps/@23.8223596,90.3656686,15z?entry=ttu', 'target', '_blank'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterOfficeLinksEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000014-0000-4000-8000-000000000014', 'StorefrontFooterOfficeLinksEntry1En', e.id, 'EN',
    '740 NEW SOUTH HEAD RD, TRIPLE BAY SWFW 3108, NEW YORK', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'https://www.google.com/maps/@23.8223596,90.3656686,15z?entry=ttu', 'target', '_blank'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterOfficeLinksEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000015-0000-4000-8000-000000000015', 'StorefrontFooterOfficeLinksEntry2Tr', e.id, 'TR',
    'Tel: + 725 214 456', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'tel:+725214456'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterOfficeLinksEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000016-0000-4000-8000-000000000016', 'StorefrontFooterOfficeLinksEntry2En', e.id, 'EN',
    'P: + 725 214 456', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'tel:+725214456'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterOfficeLinksEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000017-0000-4000-8000-000000000017', 'StorefrontFooterOfficeLinksEntry3Tr', e.id, 'TR',
    'E-posta: contact@liko.com', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'mailto:contact@liko.com'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterOfficeLinksEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000018-0000-4000-8000-000000000018', 'StorefrontFooterOfficeLinksEntry3En', e.id, 'EN',
    'E: contact@liko.com', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', 'mailto:contact@liko.com'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterOfficeLinksEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000019-0000-4000-8000-000000000019', 'StorefrontFooterNewsletterEntry1Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('inputPlaceholder', 'E-posta adresiniz...', 'buttonLabel', 'Abone Ol'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterNewsletterEntry1'
ON DUPLICATE KEY UPDATE custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000020-0000-4000-8000-000000000020', 'StorefrontFooterNewsletterEntry1En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('inputPlaceholder', 'Enter your email...', 'buttonLabel', 'Subscribe'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterNewsletterEntry1'
ON DUPLICATE KEY UPDATE custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000021-0000-4000-8000-000000000021', 'StorefrontFooterSocialLinksEntry1Tr', e.id, 'TR',
    'Linkedin', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', '#'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterSocialLinksEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000022-0000-4000-8000-000000000022', 'StorefrontFooterSocialLinksEntry1En', e.id, 'EN',
    'Linkedin', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', '#'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterSocialLinksEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000023-0000-4000-8000-000000000023', 'StorefrontFooterSocialLinksEntry2Tr', e.id, 'TR',
    'Twitter', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', '#'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterSocialLinksEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000024-0000-4000-8000-000000000024', 'StorefrontFooterSocialLinksEntry2En', e.id, 'EN',
    'Twitter', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', '#'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterSocialLinksEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000025-0000-4000-8000-000000000025', 'StorefrontFooterSocialLinksEntry3Tr', e.id, 'TR',
    'Instagram', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', '#'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterSocialLinksEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'd4000026-0000-4000-8000-000000000026', 'StorefrontFooterSocialLinksEntry3En', e.id, 'EN',
    'Instagram', NULL, 'PUBLISHED', JSON_OBJECT('linkUrl', '#'), NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'StorefrontFooterSocialLinksEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), custom_data = VALUES(custom_data), status = VALUES(status), published_at = VALUES(published_at), updated_at = NOW();

-- Version-controlled ImpEx reference script.
-- Run via Admin UI /{lang}/impex as part of `theme/liko/liko_foundation.sql`.
-- Idempotent: safe to run multiple times.
-- Prerequisites: core navigation tables must exist (Flyway).

-- ============================================
-- 1. ROOT NODES
-- ============================================

INSERT INTO navigation_nodes (uuid, uid, parent_id, position, sort_order, is_visible, is_tab, created_at, updated_at, created_by, updated_by)
VALUES ('c1000001-0000-4000-8000-000000000001', 'LandingMainNavNode', NULL, 'TOP', 0, TRUE, FALSE, NOW(), NOW(), NULL, NULL)
ON DUPLICATE KEY UPDATE position = VALUES(position), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_nodes (uuid, uid, parent_id, position, sort_order, is_visible, is_tab, created_at, updated_at, created_by, updated_by)
VALUES ('c1000003-0000-4000-8000-000000000003', 'LandingFooterNavNode', NULL, 'BOTTOM', 1, TRUE, FALSE, NOW(), NOW(), NULL, NULL)
ON DUPLICATE KEY UPDATE position = VALUES(position), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_nodes (uuid, uid, parent_id, position, sort_order, is_visible, is_tab, created_at, updated_at, created_by, updated_by)
SELECT 'c1000011-0000-4000-8000-000000000011', 'LandingMainNavHomeNode', n.id, 'TOP', 0, TRUE, FALSE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingMainNavNode'
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_nodes (uuid, uid, parent_id, position, sort_order, is_visible, is_tab, created_at, updated_at, created_by, updated_by)
SELECT 'c1000012-0000-4000-8000-000000000012', 'LandingMainNavPagesNode', n.id, 'TOP', 1, TRUE, FALSE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingMainNavNode'
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_nodes (uuid, uid, parent_id, position, sort_order, is_visible, is_tab, created_at, updated_at, created_by, updated_by)
SELECT 'c1000013-0000-4000-8000-000000000013', 'LandingMainNavPortfolioNode', n.id, 'TOP', 2, TRUE, FALSE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingMainNavNode'
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_nodes (uuid, uid, parent_id, position, sort_order, is_visible, is_tab, created_at, updated_at, created_by, updated_by)
SELECT 'c1000014-0000-4000-8000-000000000014', 'LandingMainNavBlogNode', n.id, 'TOP', 3, TRUE, FALSE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingMainNavNode'
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_nodes (uuid, uid, parent_id, position, sort_order, is_visible, is_tab, created_at, updated_at, created_by, updated_by)
SELECT 'c1000015-0000-4000-8000-000000000015', 'LandingMainNavContactNode', n.id, 'TOP', 4, TRUE, FALSE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingMainNavNode'
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

-- ============================================
-- 2. NODE I18N (TR mirrors EN)
-- ============================================

INSERT INTO navigation_node_i18n (uid, uuid, node_id, language, title, created_at, updated_at, created_by, updated_by)
SELECT data.uid, data.uuid, n.id, data.language, data.title, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n
JOIN (
  SELECT 'c2000001-0000-4000-8000-000000000001' AS uid, 'c2000001-0000-4000-8000-000000000101' AS uuid, 'LandingMainNavNode' AS node_uid, 'TR' AS language, 'Main Menu' AS title
  UNION ALL SELECT 'c2000002-0000-4000-8000-000000000002', 'c2000002-0000-4000-8000-000000000102', 'LandingMainNavNode', 'EN', 'Main Menu'
  UNION ALL SELECT 'c2000005-0000-4000-8000-000000000005', 'c2000005-0000-4000-8000-000000000105', 'LandingFooterNavNode', 'TR', 'Footer Sitemap'
  UNION ALL SELECT 'c2000006-0000-4000-8000-000000000006', 'c2000006-0000-4000-8000-000000000106', 'LandingFooterNavNode', 'EN', 'Footer Sitemap'
  UNION ALL SELECT 'c2000007-0000-4000-8000-000000000007', 'c2000007-0000-4000-8000-000000000107', 'LandingMainNavHomeNode', 'TR', 'Home'
  UNION ALL SELECT 'c2000008-0000-4000-8000-000000000008', 'c2000008-0000-4000-8000-000000000108', 'LandingMainNavHomeNode', 'EN', 'Home'
  UNION ALL SELECT 'c2000009-0000-4000-8000-000000000009', 'c2000009-0000-4000-8000-000000000109', 'LandingMainNavPagesNode', 'TR', 'Pages'
  UNION ALL SELECT 'c2000010-0000-4000-8000-000000000010', 'c2000010-0000-4000-8000-000000000110', 'LandingMainNavPagesNode', 'EN', 'Pages'
  UNION ALL SELECT 'c2000011-0000-4000-8000-000000000011', 'c2000011-0000-4000-8000-000000000111', 'LandingMainNavPortfolioNode', 'TR', 'Portfolio'
  UNION ALL SELECT 'c2000012-0000-4000-8000-000000000012', 'c2000012-0000-4000-8000-000000000112', 'LandingMainNavPortfolioNode', 'EN', 'Portfolio'
  UNION ALL SELECT 'c2000013-0000-4000-8000-000000000013', 'c2000013-0000-4000-8000-000000000113', 'LandingMainNavBlogNode', 'TR', 'Blog'
  UNION ALL SELECT 'c2000014-0000-4000-8000-000000000014', 'c2000014-0000-4000-8000-000000000114', 'LandingMainNavBlogNode', 'EN', 'Blog'
  UNION ALL SELECT 'c2000015-0000-4000-8000-000000000015', 'c2000015-0000-4000-8000-000000000115', 'LandingMainNavContactNode', 'TR', 'Contact'
  UNION ALL SELECT 'c2000016-0000-4000-8000-000000000016', 'c2000016-0000-4000-8000-000000000116', 'LandingMainNavContactNode', 'EN', 'Contact'
) data ON n.uid = data.node_uid
ON DUPLICATE KEY UPDATE title = VALUES(title), updated_at = NOW();

-- ============================================
-- 3. MAIN NAV ENTRIES
-- ============================================

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT data.uuid, data.uid, n.id, 'URL', NULL, data.url, NULL, '_self', FALSE, data.sort_order, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n
JOIN (
  SELECT 'c3000001-0000-4000-8000-000000000001' AS uuid, 'LandingMainNavHome01' AS uid, '/' AS url, 0 AS sort_order
  UNION ALL SELECT 'c3000002-0000-4000-8000-000000000002', 'LandingMainNavHome02', '/home-2', 1
  UNION ALL SELECT 'c3000003-0000-4000-8000-000000000003', 'LandingMainNavHome03', '/home-3', 2
  UNION ALL SELECT 'c3000004-0000-4000-8000-000000000004', 'LandingMainNavHome04', '/home-4', 3
  UNION ALL SELECT 'c3000005-0000-4000-8000-000000000005', 'LandingMainNavHome05', '/home-5', 4
  UNION ALL SELECT 'c3000006-0000-4000-8000-000000000006', 'LandingMainNavHome06', '/home-6', 5
  UNION ALL SELECT 'c3000007-0000-4000-8000-000000000007', 'LandingMainNavHome07', '/home-7', 6
  UNION ALL SELECT 'c3000008-0000-4000-8000-000000000008', 'LandingMainNavHome08', '/home-8', 7
  UNION ALL SELECT 'c3000009-0000-4000-8000-000000000009', 'LandingMainNavHome09', '/home-9', 8
  UNION ALL SELECT 'c3000010-0000-4000-8000-000000000010', 'LandingMainNavHome10', '/home-10', 9
  UNION ALL SELECT 'c3000011-0000-4000-8000-000000000011', 'LandingMainNavHome11', '/home-11', 10
  UNION ALL SELECT 'c3000012-0000-4000-8000-000000000012', 'LandingMainNavHome12', '/home-12', 11
) data
WHERE n.uid = 'LandingMainNavHomeNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), url = VALUES(url), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT data.uuid, data.uid, n.id, 'URL', NULL, data.url, NULL, '_self', FALSE, data.sort_order, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n
JOIN (
  SELECT 'c3000101-0000-4000-8000-000000000101' AS uuid, 'LandingMainNavPages01' AS uid, '/about-us' AS url, 0 AS sort_order
  UNION ALL SELECT 'c3000102-0000-4000-8000-000000000102', 'LandingMainNavPages02', '/faq', 1
  UNION ALL SELECT 'c3000103-0000-4000-8000-000000000103', 'LandingMainNavPages03', '/about-me', 2
  UNION ALL SELECT 'c3000104-0000-4000-8000-000000000104', 'LandingMainNavPages04', '/pricing', 3
  UNION ALL SELECT 'c3000105-0000-4000-8000-000000000105', 'LandingMainNavPages05', '/team', 4
  UNION ALL SELECT 'c3000106-0000-4000-8000-000000000106', 'LandingMainNavPages06', '/brand', 5
  UNION ALL SELECT 'c3000107-0000-4000-8000-000000000107', 'LandingMainNavPages07', '/team-details', 6
  UNION ALL SELECT 'c3000108-0000-4000-8000-000000000108', 'LandingMainNavPages08', '/register', 7
  UNION ALL SELECT 'c3000109-0000-4000-8000-000000000109', 'LandingMainNavPages09', '/service', 8
  UNION ALL SELECT 'c3000110-0000-4000-8000-000000000110', 'LandingMainNavPages10', '/login', 9
  UNION ALL SELECT 'c3000111-0000-4000-8000-000000000111', 'LandingMainNavPages11', '/service-details', 10
  UNION ALL SELECT 'c3000112-0000-4000-8000-000000000112', 'LandingMainNavPages12', '/error', 11
  UNION ALL SELECT 'c3000113-0000-4000-8000-000000000113', 'LandingMainNavPages13', '/shop', 12
  UNION ALL SELECT 'c3000114-0000-4000-8000-000000000114', 'LandingMainNavPages14', '/shop-details/1', 13
  UNION ALL SELECT 'c3000115-0000-4000-8000-000000000115', 'LandingMainNavPages15', '/shop-details-2', 14
  UNION ALL SELECT 'c3000116-0000-4000-8000-000000000116', 'LandingMainNavPages16', '/account', 15
  UNION ALL SELECT 'c3000117-0000-4000-8000-000000000117', 'LandingMainNavPages17', '/cart', 16
  UNION ALL SELECT 'c3000118-0000-4000-8000-000000000118', 'LandingMainNavPages18', '/checkout', 17
  UNION ALL SELECT 'c3000119-0000-4000-8000-000000000119', 'LandingMainNavPages19', '/wishlist', 18
) data
WHERE n.uid = 'LandingMainNavPagesNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), url = VALUES(url), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT data.uuid, data.uid, n.id, 'URL', NULL, data.url, NULL, '_self', FALSE, data.sort_order, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n
JOIN (
  SELECT 'c3000201-0000-4000-8000-000000000201' AS uuid, 'LandingMainNavPortfolio01' AS uid, '/portfolio-standard' AS url, 0 AS sort_order
  UNION ALL SELECT 'c3000202-0000-4000-8000-000000000202', 'LandingMainNavPortfolio02', '/home-10', 1
  UNION ALL SELECT 'c3000203-0000-4000-8000-000000000203', 'LandingMainNavPortfolio03', '/portfolio-random', 2
  UNION ALL SELECT 'c3000204-0000-4000-8000-000000000204', 'LandingMainNavPortfolio04', '/home-11', 3
  UNION ALL SELECT 'c3000205-0000-4000-8000-000000000205', 'LandingMainNavPortfolio05', '/portfolio-masonry', 4
  UNION ALL SELECT 'c3000206-0000-4000-8000-000000000206', 'LandingMainNavPortfolio06', '/home-12', 5
  UNION ALL SELECT 'c3000207-0000-4000-8000-000000000207', 'LandingMainNavPortfolio07', '/portfolio-wrapper', 6
  UNION ALL SELECT 'c3000208-0000-4000-8000-000000000208', 'LandingMainNavPortfolio08', '/portfolio-showcase', 7
  UNION ALL SELECT 'c3000209-0000-4000-8000-000000000209', 'LandingMainNavPortfolio09', '/home-7', 8
  UNION ALL SELECT 'c3000210-0000-4000-8000-000000000210', 'LandingMainNavPortfolio10', '/home-11', 9
  UNION ALL SELECT 'c3000211-0000-4000-8000-000000000211', 'LandingMainNavPortfolio11', '/portfolio-showcase', 10
  UNION ALL SELECT 'c3000212-0000-4000-8000-000000000212', 'LandingMainNavPortfolio12', '/home-9', 11
  UNION ALL SELECT 'c3000213-0000-4000-8000-000000000213', 'LandingMainNavPortfolio13', '/portfolio-grid-col-2', 12
  UNION ALL SELECT 'c3000214-0000-4000-8000-000000000214', 'LandingMainNavPortfolio14', '/portfolio-grid-col-3', 13
  UNION ALL SELECT 'c3000215-0000-4000-8000-000000000215', 'LandingMainNavPortfolio15', '/portfolio-grid-col-3-fullwidth', 14
  UNION ALL SELECT 'c3000216-0000-4000-8000-000000000216', 'LandingMainNavPortfolio16', '/portfolio-grid-col-4', 15
  UNION ALL SELECT 'c3000217-0000-4000-8000-000000000217', 'LandingMainNavPortfolio17', '/portfolio-grid-col-4-fullwidth', 16
  UNION ALL SELECT 'c3000218-0000-4000-8000-000000000218', 'LandingMainNavPortfolio18', '/portfolio-showcase-details-2', 17
  UNION ALL SELECT 'c3000219-0000-4000-8000-000000000219', 'LandingMainNavPortfolio19', '/portfolio-details-1', 18
  UNION ALL SELECT 'c3000220-0000-4000-8000-000000000220', 'LandingMainNavPortfolio20', '/portfolio-details-2', 19
  UNION ALL SELECT 'c3000221-0000-4000-8000-000000000221', 'LandingMainNavPortfolio21', '/portfolio-details-comparison', 20
  UNION ALL SELECT 'c3000222-0000-4000-8000-000000000222', 'LandingMainNavPortfolio22', '/portfolio-details-video', 21
  UNION ALL SELECT 'c3000223-0000-4000-8000-000000000223', 'LandingMainNavPortfolio23', '/portfolio-custom-light', 22
  UNION ALL SELECT 'c3000224-0000-4000-8000-000000000224', 'LandingMainNavPortfolio24', '/portfolio-showcase-details', 23
  UNION ALL SELECT 'c3000225-0000-4000-8000-000000000225', 'LandingMainNavPortfolio25', '/portfolio-details-3', 24
) data
WHERE n.uid = 'LandingMainNavPortfolioNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), url = VALUES(url), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT data.uuid, data.uid, n.id, 'URL', NULL, data.url, NULL, '_self', FALSE, data.sort_order, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n
JOIN (
  SELECT 'c3000301-0000-4000-8000-000000000301' AS uuid, 'LandingMainNavBlog01' AS uid, '/blog-modern' AS url, 0 AS sort_order
  UNION ALL SELECT 'c3000302-0000-4000-8000-000000000302', 'LandingMainNavBlog02', '/blog-classic', 1
  UNION ALL SELECT 'c3000303-0000-4000-8000-000000000303', 'LandingMainNavBlog03', '/blog-list', 2
  UNION ALL SELECT 'c3000304-0000-4000-8000-000000000304', 'LandingMainNavBlog04', '/blog-details/1', 3
  UNION ALL SELECT 'c3000305-0000-4000-8000-000000000305', 'LandingMainNavBlog05', '/blog-details-2', 4
) data
WHERE n.uid = 'LandingMainNavBlogNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), url = VALUES(url), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT data.uuid, data.uid, n.id, 'URL', NULL, data.url, NULL, '_self', FALSE, data.sort_order, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n
JOIN (
  SELECT 'c3000401-0000-4000-8000-000000000401' AS uuid, 'LandingMainNavContact01' AS uid, '/contact' AS url, 0 AS sort_order
  UNION ALL SELECT 'c3000402-0000-4000-8000-000000000402', 'LandingMainNavContact02', '/contact-2', 1
) data
WHERE n.uid = 'LandingMainNavContactNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), url = VALUES(url), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

-- ============================================
-- 4. FOOTER NAV ENTRIES
-- ============================================

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT data.uuid, data.uid, n.id, 'URL', NULL, data.url, NULL, '_self', FALSE, data.sort_order, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n
JOIN (
  SELECT 'c3000501-0000-4000-8000-000000000501' AS uuid, 'LandingFooterNav01' AS uid, '/' AS url, 0 AS sort_order
  UNION ALL SELECT 'c3000502-0000-4000-8000-000000000502', 'LandingFooterNav02', '/about-us', 1
  UNION ALL SELECT 'c3000503-0000-4000-8000-000000000503', 'LandingFooterNav03', '/contact', 2
  UNION ALL SELECT 'c3000504-0000-4000-8000-000000000504', 'LandingFooterNav04', '/blog-modern', 3
  UNION ALL SELECT 'c3000505-0000-4000-8000-000000000505', 'LandingFooterNav05', '/home-2', 4
) data
WHERE n.uid = 'LandingFooterNavNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), url = VALUES(url), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

-- ============================================
-- 5. ENTRY I18N (TR mirrors EN)
-- ============================================

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT data.uid, data.uuid, e.id, data.language, data.link_name, NOW(), NOW(), NULL, NULL
FROM navigation_entries e
JOIN (
  SELECT 'c4000001-0000-4000-8000-000000000001' AS uid, 'c4000001-0000-4000-8000-000000000101' AS uuid, 'LandingMainNavHome01' AS entry_uid, 'TR' AS language, 'MAIN HOME' AS link_name
  UNION ALL SELECT 'c4000002-0000-4000-8000-000000000002', 'c4000002-0000-4000-8000-000000000102', 'LandingMainNavHome01', 'EN', 'MAIN HOME'
  UNION ALL SELECT 'c4000003-0000-4000-8000-000000000003', 'c4000003-0000-4000-8000-000000000103', 'LandingMainNavHome02', 'TR', 'Fashion STUDIO'
  UNION ALL SELECT 'c4000004-0000-4000-8000-000000000004', 'c4000004-0000-4000-8000-000000000104', 'LandingMainNavHome02', 'EN', 'Fashion STUDIO'
  UNION ALL SELECT 'c4000005-0000-4000-8000-000000000005', 'c4000005-0000-4000-8000-000000000105', 'LandingMainNavHome03', 'TR', 'CREATIVE AGENCY'
  UNION ALL SELECT 'c4000006-0000-4000-8000-000000000006', 'c4000006-0000-4000-8000-000000000106', 'LandingMainNavHome03', 'EN', 'CREATIVE AGENCY'
  UNION ALL SELECT 'c4000007-0000-4000-8000-000000000007', 'c4000007-0000-4000-8000-000000000107', 'LandingMainNavHome04', 'TR', 'Digital Agency'
  UNION ALL SELECT 'c4000008-0000-4000-8000-000000000008', 'c4000008-0000-4000-8000-000000000108', 'LandingMainNavHome04', 'EN', 'Digital Agency'
  UNION ALL SELECT 'c4000009-0000-4000-8000-000000000009', 'c4000009-0000-4000-8000-000000000109', 'LandingMainNavHome05', 'TR', 'DESIGN STUDIO'
  UNION ALL SELECT 'c4000010-0000-4000-8000-000000000010', 'c4000010-0000-4000-8000-000000000110', 'LandingMainNavHome05', 'EN', 'DESIGN STUDIO'
  UNION ALL SELECT 'c4000011-0000-4000-8000-000000000011', 'c4000011-0000-4000-8000-000000000111', 'LandingMainNavHome06', 'TR', 'Minimal Shop'
  UNION ALL SELECT 'c4000012-0000-4000-8000-000000000012', 'c4000012-0000-4000-8000-000000000112', 'LandingMainNavHome06', 'EN', 'Minimal Shop'
  UNION ALL SELECT 'c4000013-0000-4000-8000-000000000013', 'c4000013-0000-4000-8000-000000000113', 'LandingMainNavHome07', 'TR', 'DESIGN STUDIO'
  UNION ALL SELECT 'c4000014-0000-4000-8000-000000000014', 'c4000014-0000-4000-8000-000000000114', 'LandingMainNavHome07', 'EN', 'DESIGN STUDIO'
  UNION ALL SELECT 'c4000015-0000-4000-8000-000000000015', 'c4000015-0000-4000-8000-000000000115', 'LandingMainNavHome08', 'TR', 'showcase carousel'
  UNION ALL SELECT 'c4000016-0000-4000-8000-000000000016', 'c4000016-0000-4000-8000-000000000116', 'LandingMainNavHome08', 'EN', 'showcase carousel'
  UNION ALL SELECT 'c4000017-0000-4000-8000-000000000017', 'c4000017-0000-4000-8000-000000000117', 'LandingMainNavHome09', 'TR', 'INTERACTIVE LINKS'
  UNION ALL SELECT 'c4000018-0000-4000-8000-000000000018', 'c4000018-0000-4000-8000-000000000118', 'LandingMainNavHome09', 'EN', 'INTERACTIVE LINKS'
  UNION ALL SELECT 'c4000019-0000-4000-8000-000000000019', 'c4000019-0000-4000-8000-000000000119', 'LandingMainNavHome10', 'TR', 'wrapper slider'
  UNION ALL SELECT 'c4000020-0000-4000-8000-000000000020', 'c4000020-0000-4000-8000-000000000120', 'LandingMainNavHome10', 'EN', 'wrapper slider'
  UNION ALL SELECT 'c4000021-0000-4000-8000-000000000021', 'c4000021-0000-4000-8000-000000000121', 'LandingMainNavHome11', 'TR', 'showcase parallax'
  UNION ALL SELECT 'c4000022-0000-4000-8000-000000000022', 'c4000022-0000-4000-8000-000000000122', 'LandingMainNavHome11', 'EN', 'showcase parallax'
  UNION ALL SELECT 'c4000023-0000-4000-8000-000000000023', 'c4000023-0000-4000-8000-000000000123', 'LandingMainNavHome12', 'TR', 'horizontal'
  UNION ALL SELECT 'c4000024-0000-4000-8000-000000000024', 'c4000024-0000-4000-8000-000000000124', 'LandingMainNavHome12', 'EN', 'horizontal'
  UNION ALL SELECT 'c4000101-0000-4000-8000-000000000201', 'c4000101-0000-4000-8000-000000000301', 'LandingFooterNav01', 'TR', 'Home'
  UNION ALL SELECT 'c4000102-0000-4000-8000-000000000202', 'c4000102-0000-4000-8000-000000000302', 'LandingFooterNav01', 'EN', 'Home'
  UNION ALL SELECT 'c4000103-0000-4000-8000-000000000203', 'c4000103-0000-4000-8000-000000000303', 'LandingFooterNav02', 'TR', 'About'
  UNION ALL SELECT 'c4000104-0000-4000-8000-000000000204', 'c4000104-0000-4000-8000-000000000304', 'LandingFooterNav02', 'EN', 'About'
  UNION ALL SELECT 'c4000105-0000-4000-8000-000000000205', 'c4000105-0000-4000-8000-000000000305', 'LandingFooterNav03', 'TR', 'Contact'
  UNION ALL SELECT 'c4000106-0000-4000-8000-000000000206', 'c4000106-0000-4000-8000-000000000306', 'LandingFooterNav03', 'EN', 'Contact'
  UNION ALL SELECT 'c4000107-0000-4000-8000-000000000207', 'c4000107-0000-4000-8000-000000000307', 'LandingFooterNav04', 'TR', 'Blog'
  UNION ALL SELECT 'c4000108-0000-4000-8000-000000000208', 'c4000108-0000-4000-8000-000000000308', 'LandingFooterNav04', 'EN', 'Blog'
  UNION ALL SELECT 'c4000109-0000-4000-8000-000000000209', 'c4000109-0000-4000-8000-000000000309', 'LandingFooterNav05', 'TR', 'Landing'
  UNION ALL SELECT 'c4000110-0000-4000-8000-000000000210', 'c4000110-0000-4000-8000-000000000310', 'LandingFooterNav05', 'EN', 'Landing'
) data ON e.uid = data.entry_uid
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT data.uid, data.uuid, e.id, data.language, data.link_name, NOW(), NOW(), NULL, NULL
FROM navigation_entries e
JOIN (
  SELECT 'c4000201-0000-4000-8000-000000000401' AS uid, 'c4000201-0000-4000-8000-000000000501' AS uuid, 'LandingMainNavPages01' AS entry_uid, 'TR' AS language, 'ABOUT US' AS link_name
  UNION ALL SELECT 'c4000202-0000-4000-8000-000000000402', 'c4000202-0000-4000-8000-000000000502', 'LandingMainNavPages01', 'EN', 'ABOUT US'
  UNION ALL SELECT 'c4000203-0000-4000-8000-000000000403', 'c4000203-0000-4000-8000-000000000503', 'LandingMainNavPages02', 'TR', 'FAQ Page'
  UNION ALL SELECT 'c4000204-0000-4000-8000-000000000404', 'c4000204-0000-4000-8000-000000000504', 'LandingMainNavPages02', 'EN', 'FAQ Page'
  UNION ALL SELECT 'c4000205-0000-4000-8000-000000000405', 'c4000205-0000-4000-8000-000000000505', 'LandingMainNavPages03', 'TR', 'ABOUT ME'
  UNION ALL SELECT 'c4000206-0000-4000-8000-000000000406', 'c4000206-0000-4000-8000-000000000506', 'LandingMainNavPages03', 'EN', 'ABOUT ME'
  UNION ALL SELECT 'c4000207-0000-4000-8000-000000000407', 'c4000207-0000-4000-8000-000000000507', 'LandingMainNavPages04', 'TR', 'Pricing'
  UNION ALL SELECT 'c4000208-0000-4000-8000-000000000408', 'c4000208-0000-4000-8000-000000000508', 'LandingMainNavPages04', 'EN', 'Pricing'
  UNION ALL SELECT 'c4000209-0000-4000-8000-000000000409', 'c4000209-0000-4000-8000-000000000509', 'LandingMainNavPages05', 'TR', 'Team Page'
  UNION ALL SELECT 'c4000210-0000-4000-8000-000000000410', 'c4000210-0000-4000-8000-000000000510', 'LandingMainNavPages05', 'EN', 'Team Page'
  UNION ALL SELECT 'c4000211-0000-4000-8000-000000000411', 'c4000211-0000-4000-8000-000000000511', 'LandingMainNavPages06', 'TR', 'OUR CLIENTS'
  UNION ALL SELECT 'c4000212-0000-4000-8000-000000000412', 'c4000212-0000-4000-8000-000000000512', 'LandingMainNavPages06', 'EN', 'OUR CLIENTS'
  UNION ALL SELECT 'c4000213-0000-4000-8000-000000000413', 'c4000213-0000-4000-8000-000000000513', 'LandingMainNavPages07', 'TR', 'Team Details'
  UNION ALL SELECT 'c4000214-0000-4000-8000-000000000414', 'c4000214-0000-4000-8000-000000000514', 'LandingMainNavPages07', 'EN', 'Team Details'
  UNION ALL SELECT 'c4000215-0000-4000-8000-000000000415', 'c4000215-0000-4000-8000-000000000515', 'LandingMainNavPages08', 'TR', 'Register'
  UNION ALL SELECT 'c4000216-0000-4000-8000-000000000416', 'c4000216-0000-4000-8000-000000000516', 'LandingMainNavPages08', 'EN', 'Register'
  UNION ALL SELECT 'c4000217-0000-4000-8000-000000000417', 'c4000217-0000-4000-8000-000000000517', 'LandingMainNavPages09', 'TR', 'OUR SERVICES'
  UNION ALL SELECT 'c4000218-0000-4000-8000-000000000418', 'c4000218-0000-4000-8000-000000000518', 'LandingMainNavPages09', 'EN', 'OUR SERVICES'
  UNION ALL SELECT 'c4000219-0000-4000-8000-000000000419', 'c4000219-0000-4000-8000-000000000519', 'LandingMainNavPages10', 'TR', 'LogIn'
  UNION ALL SELECT 'c4000220-0000-4000-8000-000000000420', 'c4000220-0000-4000-8000-000000000520', 'LandingMainNavPages10', 'EN', 'LogIn'
  UNION ALL SELECT 'c4000221-0000-4000-8000-000000000421', 'c4000221-0000-4000-8000-000000000521', 'LandingMainNavPages11', 'TR', 'SERVICES DETAILS'
  UNION ALL SELECT 'c4000222-0000-4000-8000-000000000422', 'c4000222-0000-4000-8000-000000000522', 'LandingMainNavPages11', 'EN', 'SERVICES DETAILS'
  UNION ALL SELECT 'c4000223-0000-4000-8000-000000000423', 'c4000223-0000-4000-8000-000000000523', 'LandingMainNavPages12', 'TR', 'ERROR PAGE'
  UNION ALL SELECT 'c4000224-0000-4000-8000-000000000424', 'c4000224-0000-4000-8000-000000000524', 'LandingMainNavPages12', 'EN', 'ERROR PAGE'
  UNION ALL SELECT 'c4000225-0000-4000-8000-000000000425', 'c4000225-0000-4000-8000-000000000525', 'LandingMainNavPages13', 'TR', 'Shop Page'
  UNION ALL SELECT 'c4000226-0000-4000-8000-000000000426', 'c4000226-0000-4000-8000-000000000526', 'LandingMainNavPages13', 'EN', 'Shop Page'
  UNION ALL SELECT 'c4000227-0000-4000-8000-000000000427', 'c4000227-0000-4000-8000-000000000527', 'LandingMainNavPages14', 'TR', 'Shop Details One'
  UNION ALL SELECT 'c4000228-0000-4000-8000-000000000428', 'c4000228-0000-4000-8000-000000000528', 'LandingMainNavPages14', 'EN', 'Shop Details One'
  UNION ALL SELECT 'c4000229-0000-4000-8000-000000000429', 'c4000229-0000-4000-8000-000000000529', 'LandingMainNavPages15', 'TR', 'Shop Details Two'
  UNION ALL SELECT 'c4000230-0000-4000-8000-000000000430', 'c4000230-0000-4000-8000-000000000530', 'LandingMainNavPages15', 'EN', 'Shop Details Two'
  UNION ALL SELECT 'c4000231-0000-4000-8000-000000000431', 'c4000231-0000-4000-8000-000000000531', 'LandingMainNavPages16', 'TR', 'my account'
  UNION ALL SELECT 'c4000232-0000-4000-8000-000000000432', 'c4000232-0000-4000-8000-000000000532', 'LandingMainNavPages16', 'EN', 'my account'
  UNION ALL SELECT 'c4000233-0000-4000-8000-000000000433', 'c4000233-0000-4000-8000-000000000533', 'LandingMainNavPages17', 'TR', 'Cart'
  UNION ALL SELECT 'c4000234-0000-4000-8000-000000000434', 'c4000234-0000-4000-8000-000000000534', 'LandingMainNavPages17', 'EN', 'Cart'
  UNION ALL SELECT 'c4000235-0000-4000-8000-000000000435', 'c4000235-0000-4000-8000-000000000535', 'LandingMainNavPages18', 'TR', 'Checkout'
  UNION ALL SELECT 'c4000236-0000-4000-8000-000000000436', 'c4000236-0000-4000-8000-000000000536', 'LandingMainNavPages18', 'EN', 'Checkout'
  UNION ALL SELECT 'c4000237-0000-4000-8000-000000000437', 'c4000237-0000-4000-8000-000000000537', 'LandingMainNavPages19', 'TR', 'Wishlist'
  UNION ALL SELECT 'c4000238-0000-4000-8000-000000000438', 'c4000238-0000-4000-8000-000000000538', 'LandingMainNavPages19', 'EN', 'Wishlist'
  UNION ALL SELECT 'c4000301-0000-4000-8000-000000000601', 'c4000301-0000-4000-8000-000000000701', 'LandingMainNavPortfolio01', 'TR', 'Standard'
  UNION ALL SELECT 'c4000302-0000-4000-8000-000000000602', 'c4000302-0000-4000-8000-000000000702', 'LandingMainNavPortfolio01', 'EN', 'Standard'
  UNION ALL SELECT 'c4000303-0000-4000-8000-000000000603', 'c4000303-0000-4000-8000-000000000703', 'LandingMainNavPortfolio02', 'TR', 'Interactive'
  UNION ALL SELECT 'c4000304-0000-4000-8000-000000000604', 'c4000304-0000-4000-8000-000000000704', 'LandingMainNavPortfolio02', 'EN', 'Interactive'
  UNION ALL SELECT 'c4000305-0000-4000-8000-000000000605', 'c4000305-0000-4000-8000-000000000705', 'LandingMainNavPortfolio03', 'TR', 'random'
  UNION ALL SELECT 'c4000306-0000-4000-8000-000000000606', 'c4000306-0000-4000-8000-000000000706', 'LandingMainNavPortfolio03', 'EN', 'random'
  UNION ALL SELECT 'c4000307-0000-4000-8000-000000000607', 'c4000307-0000-4000-8000-000000000707', 'LandingMainNavPortfolio04', 'TR', 'showcase parallax'
  UNION ALL SELECT 'c4000308-0000-4000-8000-000000000608', 'c4000308-0000-4000-8000-000000000708', 'LandingMainNavPortfolio04', 'EN', 'showcase parallax'
  UNION ALL SELECT 'c4000309-0000-4000-8000-000000000609', 'c4000309-0000-4000-8000-000000000709', 'LandingMainNavPortfolio05', 'TR', 'Masonry random'
  UNION ALL SELECT 'c4000310-0000-4000-8000-000000000610', 'c4000310-0000-4000-8000-000000000710', 'LandingMainNavPortfolio05', 'EN', 'Masonry random'
  UNION ALL SELECT 'c4000311-0000-4000-8000-000000000611', 'c4000311-0000-4000-8000-000000000711', 'LandingMainNavPortfolio06', 'TR', 'Vertical Carousel'
  UNION ALL SELECT 'c4000312-0000-4000-8000-000000000612', 'c4000312-0000-4000-8000-000000000712', 'LandingMainNavPortfolio06', 'EN', 'Vertical Carousel'
  UNION ALL SELECT 'c4000313-0000-4000-8000-000000000613', 'c4000313-0000-4000-8000-000000000713', 'LandingMainNavPortfolio07', 'TR', 'wrapper'
  UNION ALL SELECT 'c4000314-0000-4000-8000-000000000614', 'c4000314-0000-4000-8000-000000000714', 'LandingMainNavPortfolio07', 'EN', 'wrapper'
  UNION ALL SELECT 'c4000315-0000-4000-8000-000000000615', 'c4000315-0000-4000-8000-000000000715', 'LandingMainNavPortfolio08', 'TR', 'horizontal'
  UNION ALL SELECT 'c4000316-0000-4000-8000-000000000616', 'c4000316-0000-4000-8000-000000000716', 'LandingMainNavPortfolio08', 'EN', 'horizontal'
  UNION ALL SELECT 'c4000317-0000-4000-8000-000000000617', 'c4000317-0000-4000-8000-000000000717', 'LandingMainNavPortfolio09', 'TR', 'Image Slider'
  UNION ALL SELECT 'c4000318-0000-4000-8000-000000000618', 'c4000318-0000-4000-8000-000000000718', 'LandingMainNavPortfolio09', 'EN', 'Image Slider'
  UNION ALL SELECT 'c4000319-0000-4000-8000-000000000619', 'c4000319-0000-4000-8000-000000000719', 'LandingMainNavPortfolio10', 'TR', 'wrapper Slider'
  UNION ALL SELECT 'c4000320-0000-4000-8000-000000000620', 'c4000320-0000-4000-8000-000000000720', 'LandingMainNavPortfolio10', 'EN', 'wrapper Slider'
  UNION ALL SELECT 'c4000321-0000-4000-8000-000000000621', 'c4000321-0000-4000-8000-000000000721', 'LandingMainNavPortfolio11', 'TR', 'parallax showcase'
  UNION ALL SELECT 'c4000322-0000-4000-8000-000000000622', 'c4000322-0000-4000-8000-000000000722', 'LandingMainNavPortfolio11', 'EN', 'parallax showcase'
  UNION ALL SELECT 'c4000323-0000-4000-8000-000000000623', 'c4000323-0000-4000-8000-000000000723', 'LandingMainNavPortfolio12', 'TR', 'Perspective Slider'
  UNION ALL SELECT 'c4000324-0000-4000-8000-000000000624', 'c4000324-0000-4000-8000-000000000724', 'LandingMainNavPortfolio12', 'EN', 'Perspective Slider'
  UNION ALL SELECT 'c4000325-0000-4000-8000-000000000625', 'c4000325-0000-4000-8000-000000000725', 'LandingMainNavPortfolio13', 'TR', 'two-columns'
  UNION ALL SELECT 'c4000326-0000-4000-8000-000000000626', 'c4000326-0000-4000-8000-000000000726', 'LandingMainNavPortfolio13', 'EN', 'two-columns'
  UNION ALL SELECT 'c4000327-0000-4000-8000-000000000627', 'c4000327-0000-4000-8000-000000000727', 'LandingMainNavPortfolio14', 'TR', 'three-columns'
  UNION ALL SELECT 'c4000328-0000-4000-8000-000000000628', 'c4000328-0000-4000-8000-000000000728', 'LandingMainNavPortfolio14', 'EN', 'three-columns'
  UNION ALL SELECT 'c4000329-0000-4000-8000-000000000629', 'c4000329-0000-4000-8000-000000000729', 'LandingMainNavPortfolio15', 'TR', 'three-columns Wide'
  UNION ALL SELECT 'c4000330-0000-4000-8000-000000000630', 'c4000330-0000-4000-8000-000000000730', 'LandingMainNavPortfolio15', 'EN', 'three-columns Wide'
  UNION ALL SELECT 'c4000331-0000-4000-8000-000000000631', 'c4000331-0000-4000-8000-000000000731', 'LandingMainNavPortfolio16', 'TR', 'four-columns'
  UNION ALL SELECT 'c4000332-0000-4000-8000-000000000632', 'c4000332-0000-4000-8000-000000000732', 'LandingMainNavPortfolio16', 'EN', 'four-columns'
  UNION ALL SELECT 'c4000333-0000-4000-8000-000000000633', 'c4000333-0000-4000-8000-000000000733', 'LandingMainNavPortfolio17', 'TR', 'four-columns Wide'
  UNION ALL SELECT 'c4000334-0000-4000-8000-000000000634', 'c4000334-0000-4000-8000-000000000734', 'LandingMainNavPortfolio17', 'EN', 'four-columns Wide'
  UNION ALL SELECT 'c4000335-0000-4000-8000-000000000635', 'c4000335-0000-4000-8000-000000000735', 'LandingMainNavPortfolio18', 'TR', 'Creative'
  UNION ALL SELECT 'c4000336-0000-4000-8000-000000000636', 'c4000336-0000-4000-8000-000000000736', 'LandingMainNavPortfolio18', 'EN', 'Creative'
  UNION ALL SELECT 'c4000337-0000-4000-8000-000000000637', 'c4000337-0000-4000-8000-000000000737', 'LandingMainNavPortfolio19', 'TR', 'images Small'
  UNION ALL SELECT 'c4000338-0000-4000-8000-000000000638', 'c4000338-0000-4000-8000-000000000738', 'LandingMainNavPortfolio19', 'EN', 'images Small'
  UNION ALL SELECT 'c4000339-0000-4000-8000-000000000639', 'c4000339-0000-4000-8000-000000000739', 'LandingMainNavPortfolio20', 'TR', 'Sliding'
  UNION ALL SELECT 'c4000340-0000-4000-8000-000000000640', 'c4000340-0000-4000-8000-000000000740', 'LandingMainNavPortfolio20', 'EN', 'Sliding'
  UNION ALL SELECT 'c4000341-0000-4000-8000-000000000641', 'c4000341-0000-4000-8000-000000000741', 'LandingMainNavPortfolio21', 'TR', 'Image Comparison'
  UNION ALL SELECT 'c4000342-0000-4000-8000-000000000642', 'c4000342-0000-4000-8000-000000000742', 'LandingMainNavPortfolio21', 'EN', 'Image Comparison'
  UNION ALL SELECT 'c4000343-0000-4000-8000-000000000643', 'c4000343-0000-4000-8000-000000000743', 'LandingMainNavPortfolio22', 'TR', 'Video'
  UNION ALL SELECT 'c4000344-0000-4000-8000-000000000644', 'c4000344-0000-4000-8000-000000000744', 'LandingMainNavPortfolio22', 'EN', 'Video'
  UNION ALL SELECT 'c4000345-0000-4000-8000-000000000645', 'c4000345-0000-4000-8000-000000000745', 'LandingMainNavPortfolio23', 'TR', 'CUSTOM LIGHT'
  UNION ALL SELECT 'c4000346-0000-4000-8000-000000000646', 'c4000346-0000-4000-8000-000000000746', 'LandingMainNavPortfolio23', 'EN', 'CUSTOM LIGHT'
  UNION ALL SELECT 'c4000347-0000-4000-8000-000000000647', 'c4000347-0000-4000-8000-000000000747', 'LandingMainNavPortfolio24', 'TR', 'Gallery'
  UNION ALL SELECT 'c4000348-0000-4000-8000-000000000648', 'c4000348-0000-4000-8000-000000000748', 'LandingMainNavPortfolio24', 'EN', 'Gallery'
  UNION ALL SELECT 'c4000349-0000-4000-8000-000000000649', 'c4000349-0000-4000-8000-000000000749', 'LandingMainNavPortfolio25', 'TR', 'Mockups'
  UNION ALL SELECT 'c4000350-0000-4000-8000-000000000650', 'c4000350-0000-4000-8000-000000000750', 'LandingMainNavPortfolio25', 'EN', 'Mockups'
  UNION ALL SELECT 'c4000401-0000-4000-8000-000000000801', 'c4000401-0000-4000-8000-000000000901', 'LandingMainNavBlog01', 'TR', 'Modern'
  UNION ALL SELECT 'c4000402-0000-4000-8000-000000000802', 'c4000402-0000-4000-8000-000000000902', 'LandingMainNavBlog01', 'EN', 'Modern'
  UNION ALL SELECT 'c4000403-0000-4000-8000-000000000803', 'c4000403-0000-4000-8000-000000000903', 'LandingMainNavBlog02', 'TR', 'Classic Sidebar'
  UNION ALL SELECT 'c4000404-0000-4000-8000-000000000804', 'c4000404-0000-4000-8000-000000000904', 'LandingMainNavBlog02', 'EN', 'Classic Sidebar'
  UNION ALL SELECT 'c4000405-0000-4000-8000-000000000805', 'c4000405-0000-4000-8000-000000000905', 'LandingMainNavBlog03', 'TR', 'Minimal List'
  UNION ALL SELECT 'c4000406-0000-4000-8000-000000000806', 'c4000406-0000-4000-8000-000000000906', 'LandingMainNavBlog03', 'EN', 'Minimal List'
  UNION ALL SELECT 'c4000407-0000-4000-8000-000000000807', 'c4000407-0000-4000-8000-000000000907', 'LandingMainNavBlog04', 'TR', 'Post Single'
  UNION ALL SELECT 'c4000408-0000-4000-8000-000000000808', 'c4000408-0000-4000-8000-000000000908', 'LandingMainNavBlog04', 'EN', 'Post Single'
  UNION ALL SELECT 'c4000409-0000-4000-8000-000000000809', 'c4000409-0000-4000-8000-000000000909', 'LandingMainNavBlog05', 'TR', 'Post With Sidebar'
  UNION ALL SELECT 'c4000410-0000-4000-8000-000000000810', 'c4000410-0000-4000-8000-000000000910', 'LandingMainNavBlog05', 'EN', 'Post With Sidebar'
  UNION ALL SELECT 'c4000501-0000-4000-8000-000000001001', 'c4000501-0000-4000-8000-000000001101', 'LandingMainNavContact01', 'TR', 'Contact'
  UNION ALL SELECT 'c4000502-0000-4000-8000-000000001002', 'c4000502-0000-4000-8000-000000001102', 'LandingMainNavContact01', 'EN', 'Contact'
  UNION ALL SELECT 'c4000503-0000-4000-8000-000000001003', 'c4000503-0000-4000-8000-000000001103', 'LandingMainNavContact02', 'TR', 'Get IN touch'
  UNION ALL SELECT 'c4000504-0000-4000-8000-000000001004', 'c4000504-0000-4000-8000-000000001104', 'LandingMainNavContact02', 'EN', 'Get IN touch'
) data ON e.uid = data.entry_uid
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

-- ============================================
-- 6. BIND NAVIGATION-AWARE CHROME COMPONENTS
-- ============================================

UPDATE components c
JOIN navigation_nodes n ON n.uid = 'LandingMainNavNode'
SET c.navigation_node_id = n.id,
    c.navigation_type = 'MAINMENU',
    c.search_box = FALSE,
    c.updated_at = NOW()
WHERE c.uid = 'StorefrontHeaderMainNavigation';

UPDATE components c
JOIN navigation_nodes n ON n.uid = 'LandingFooterNavNode'
SET c.navigation_node_id = n.id,
    c.navigation_type = 'STATICPAGE',
    c.search_box = FALSE,
    c.updated_at = NOW()
WHERE c.uid = 'StorefrontFooterSitemapNavigation';

-- ============================================
-- 7. MAIN NAV CLEANUP (remove demo navigation noise)
-- ============================================

-- Hide any direct root-level entries if the tenant previously imported older demo data.
UPDATE navigation_entries e
JOIN navigation_nodes n ON n.id = e.node_id
SET e.is_visible = FALSE,
    e.updated_at = NOW()
WHERE n.uid = 'LandingMainNavNode';

-- Keep a minimal, working main nav: Home, About Us, Search.
UPDATE navigation_nodes
SET is_visible = CASE uid
        WHEN 'LandingMainNavHomeNode' THEN TRUE
        WHEN 'LandingMainNavPagesNode' THEN TRUE
        WHEN 'LandingMainNavPortfolioNode' THEN TRUE
        WHEN 'LandingMainNavContactNode' THEN TRUE
        ELSE FALSE
    END,
    sort_order = CASE uid
        WHEN 'LandingMainNavHomeNode' THEN 0
        WHEN 'LandingMainNavPagesNode' THEN 1
        WHEN 'LandingMainNavPortfolioNode' THEN 2
        WHEN 'LandingMainNavContactNode' THEN 3
        WHEN 'LandingMainNavBlogNode' THEN 4
        ELSE sort_order
    END,
    updated_at = NOW()
WHERE uid IN (
    'LandingMainNavHomeNode',
    'LandingMainNavPagesNode',
    'LandingMainNavPortfolioNode',
    'LandingMainNavBlogNode',
    'LandingMainNavContactNode'
);

-- Home node -> only homepage.
UPDATE navigation_entries
SET url = '/',
    sort_order = 0,
    is_visible = TRUE,
    updated_at = NOW()
WHERE uid = 'LandingMainNavHome01';

UPDATE navigation_entries
SET is_visible = FALSE,
    updated_at = NOW()
WHERE uid IN (
    'LandingMainNavHome02',
    'LandingMainNavHome03',
    'LandingMainNavHome04',
    'LandingMainNavHome05',
    'LandingMainNavHome06',
    'LandingMainNavHome07',
    'LandingMainNavHome08',
    'LandingMainNavHome09',
    'LandingMainNavHome10',
    'LandingMainNavHome11',
    'LandingMainNavHome12'
);

-- Pages node -> only about page.
UPDATE navigation_entries
SET url = '/about-us',
    sort_order = 0,
    is_visible = TRUE,
    updated_at = NOW()
WHERE uid = 'LandingMainNavPages01';

UPDATE navigation_entries
SET is_visible = FALSE,
    updated_at = NOW()
WHERE uid IN (
    'LandingMainNavPages02',
    'LandingMainNavPages03',
    'LandingMainNavPages04',
    'LandingMainNavPages05',
    'LandingMainNavPages06',
    'LandingMainNavPages07',
    'LandingMainNavPages08',
    'LandingMainNavPages09',
    'LandingMainNavPages10',
    'LandingMainNavPages11',
    'LandingMainNavPages12',
    'LandingMainNavPages13',
    'LandingMainNavPages14',
    'LandingMainNavPages15',
    'LandingMainNavPages16',
    'LandingMainNavPages17',
    'LandingMainNavPages18',
    'LandingMainNavPages19'
);

-- Portfolio node is repurposed as the service entry because /service is backed by CMS seeds.
UPDATE navigation_entries
SET url = '/service',
    sort_order = 0,
    is_visible = TRUE,
    updated_at = NOW()
WHERE uid = 'LandingMainNavPortfolio01';

UPDATE navigation_entries
SET is_visible = FALSE,
    updated_at = NOW()
WHERE uid IN (
    'LandingMainNavPortfolio02',
    'LandingMainNavPortfolio03',
    'LandingMainNavPortfolio04',
    'LandingMainNavPortfolio05',
    'LandingMainNavPortfolio06',
    'LandingMainNavPortfolio07',
    'LandingMainNavPortfolio08',
    'LandingMainNavPortfolio09',
    'LandingMainNavPortfolio10',
    'LandingMainNavPortfolio11',
    'LandingMainNavPortfolio12',
    'LandingMainNavPortfolio13',
    'LandingMainNavPortfolio14',
    'LandingMainNavPortfolio15',
    'LandingMainNavPortfolio16',
    'LandingMainNavPortfolio17',
    'LandingMainNavPortfolio18',
    'LandingMainNavPortfolio19',
    'LandingMainNavPortfolio20',
    'LandingMainNavPortfolio21',
    'LandingMainNavPortfolio22',
    'LandingMainNavPortfolio23',
    'LandingMainNavPortfolio24',
    'LandingMainNavPortfolio25'
);

-- Contact node is repurposed as a safe search entry because /search is guaranteed to exist in seeds.
UPDATE navigation_entries
SET url = '/search',
    sort_order = 0,
    is_visible = TRUE,
    updated_at = NOW()
WHERE uid = 'LandingMainNavContact01';

UPDATE navigation_entries
SET is_visible = FALSE,
    updated_at = NOW()
WHERE uid = 'LandingMainNavContact02';

-- Hide remaining demo-heavy groups entirely.
UPDATE navigation_entries e
JOIN navigation_nodes n ON n.id = e.node_id
SET e.is_visible = FALSE,
    e.updated_at = NOW()
WHERE n.uid IN ('LandingMainNavBlogNode');

-- Localize visible node titles.
UPDATE navigation_node_i18n
SET title = 'Ana Menü',
    updated_at = NOW()
WHERE uid = 'c2000001-0000-4000-8000-000000000001';

UPDATE navigation_node_i18n
SET title = 'Anasayfa',
    updated_at = NOW()
WHERE uid = 'c2000007-0000-4000-8000-000000000007';

UPDATE navigation_node_i18n
SET title = 'Hakkımızda',
    updated_at = NOW()
WHERE uid = 'c2000009-0000-4000-8000-000000000009';

UPDATE navigation_node_i18n
SET title = 'Hizmetler',
    updated_at = NOW()
WHERE uid = 'c2000011-0000-4000-8000-000000000011';

UPDATE navigation_node_i18n
SET title = 'Arama',
    updated_at = NOW()
WHERE uid = 'c2000015-0000-4000-8000-000000000015';

UPDATE navigation_node_i18n
SET title = 'About Us',
    updated_at = NOW()
WHERE uid = 'c2000010-0000-4000-8000-000000000010';

UPDATE navigation_node_i18n
SET title = 'Services',
    updated_at = NOW()
WHERE uid = 'c2000012-0000-4000-8000-000000000012';

UPDATE navigation_node_i18n
SET title = 'Search',
    updated_at = NOW()
WHERE uid = 'c2000016-0000-4000-8000-000000000016';

-- Keep visible entry labels consistent with the simplified nav.
UPDATE navigation_entry_i18n
SET link_name = 'Anasayfa',
    updated_at = NOW()
WHERE uid = 'c4000001-0000-4000-8000-000000000001';

UPDATE navigation_entry_i18n
SET link_name = 'Home',
    updated_at = NOW()
WHERE uid = 'c4000002-0000-4000-8000-000000000002';

UPDATE navigation_entry_i18n
SET link_name = 'Hakkımızda',
    updated_at = NOW()
WHERE uid = 'c4000201-0000-4000-8000-000000000401';

UPDATE navigation_entry_i18n
SET link_name = 'About Us',
    updated_at = NOW()
WHERE uid = 'c4000202-0000-4000-8000-000000000402';

UPDATE navigation_entry_i18n
SET link_name = 'Hizmetler',
    updated_at = NOW()
WHERE uid = 'c4000301-0000-4000-8000-000000000601';

UPDATE navigation_entry_i18n
SET link_name = 'Services',
    updated_at = NOW()
WHERE uid = 'c4000302-0000-4000-8000-000000000602';

UPDATE navigation_entry_i18n
SET link_name = 'Arama',
    updated_at = NOW()
WHERE uid = 'c4000501-0000-4000-8000-000000001001';

UPDATE navigation_entry_i18n
SET link_name = 'Search',
    updated_at = NOW()
WHERE uid = 'c4000502-0000-4000-8000-000000001002';

