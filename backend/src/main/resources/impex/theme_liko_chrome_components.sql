-- #CRAFTIVE_IMPEX
-- Liko Home-2 Header/Footer Chrome Components — ImpEx seed file.
-- Run via Admin UI /{lang}/impex before seed_navigation.sql.
-- Idempotent: safe to run multiple times.
-- Prerequisites:
--   1. component_types seeded by Flyway
-- Note: seed_liko_pages_and_slots.sql must run AFTER this file.

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
