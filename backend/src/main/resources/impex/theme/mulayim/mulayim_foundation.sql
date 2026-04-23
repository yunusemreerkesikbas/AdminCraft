-- #CRAFTIVE_IMPEX
-- Mulayim theme foundation
-- Run via Admin UI /{lang}/impex before theme/mulayim/portfolio_homepage.sql
-- Seeds theme-owned page templates, template slots, shared shell chrome, minimal navigation and search page

-- ============================================================
-- 0. REQUIRED COMPONENT TYPES
-- ============================================================

INSERT INTO component_types (uuid, uid, name, category, is_navigation_aware, created_at, updated_at)
VALUES
  (UUID(), 'NavigationComponent', 'Navigation Component', 'navigation', TRUE, NOW(), NOW()),
  (UUID(), 'CMSLinkComponent', 'CTA Button', 'cta', FALSE, NOW(), NOW()),
  (UUID(), 'CMSParagraphComponent', 'Paragraph', 'content', FALSE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  is_navigation_aware = VALUES(is_navigation_aware),
  updated_at = NOW();

-- ============================================================
-- 1. PAGE TEMPLATES
-- ============================================================

INSERT INTO page_templates (uuid, uid, is_system, is_active)
VALUES
  ('m1000001-0000-4000-8000-000000000001', 'LandingPageTemplate', FALSE, TRUE),
  ('m1000002-0000-4000-8000-000000000002', 'ContentPageTemplate', FALSE, TRUE),
  ('m1000003-0000-4000-8000-000000000003', 'CategoryPageTemplate', FALSE, TRUE),
  ('m1000004-0000-4000-8000-000000000004', 'ProductDetailsPageTemplate', FALSE, TRUE),
  ('m1000005-0000-4000-8000-000000000005', 'SearchResultsPageTemplate', FALSE, TRUE),
  ('m1000006-0000-4000-8000-000000000006', 'ErrorPageTemplate', FALSE, TRUE),
  ('m1000007-0000-4000-8000-000000000007', 'NotFoundPageTemplate', FALSE, TRUE)
ON DUPLICATE KEY UPDATE
  is_system = VALUES(is_system),
  is_active = VALUES(is_active);

INSERT INTO page_template_i18n (uuid, uid, template_id, language, name, description)
SELECT data.uuid, data.uid, pt.id, data.language, data.name, data.description
FROM page_templates pt
JOIN (
  SELECT 'm1100001-0000-4000-8000-000000000001' AS uuid, 'LandingPageTemplateTr' AS uid, 'LandingPageTemplate' AS template_uid, 'TR' AS language, 'Landing Sayfasi Sablonu' AS name, 'Homepage ve portfolio landing sayfalari icin kullanilir' AS description
  UNION ALL SELECT 'm1100002-0000-4000-8000-000000000002', 'LandingPageTemplateEn', 'LandingPageTemplate', 'EN', 'Landing Page Template', 'Used for homepage and portfolio landing pages'
  UNION ALL SELECT 'm1100003-0000-4000-8000-000000000003', 'ContentPageTemplateTr', 'ContentPageTemplate', 'TR', 'Icerik Sayfasi Sablonu', 'Genel icerik sayfalari icin kullanilir'
  UNION ALL SELECT 'm1100004-0000-4000-8000-000000000004', 'ContentPageTemplateEn', 'ContentPageTemplate', 'EN', 'Content Page Template', 'Used for editorial content pages'
  UNION ALL SELECT 'm1100005-0000-4000-8000-000000000005', 'CategoryPageTemplateTr', 'CategoryPageTemplate', 'TR', 'Kategori Sayfasi Sablonu', 'Kategori listeleme sayfalari icin kullanilir'
  UNION ALL SELECT 'm1100006-0000-4000-8000-000000000006', 'CategoryPageTemplateEn', 'CategoryPageTemplate', 'EN', 'Category Page Template', 'Used for category listing pages'
  UNION ALL SELECT 'm1100007-0000-4000-8000-000000000007', 'ProductDetailsPageTemplateTr', 'ProductDetailsPageTemplate', 'TR', 'Urun Detay Sablonu', 'Urun detay sayfalari icin kullanilir'
  UNION ALL SELECT 'm1100008-0000-4000-8000-000000000008', 'ProductDetailsPageTemplateEn', 'ProductDetailsPageTemplate', 'EN', 'Product Details Page Template', 'Used for product detail pages'
  UNION ALL SELECT 'm1100009-0000-4000-8000-000000000009', 'SearchResultsPageTemplateTr', 'SearchResultsPageTemplate', 'TR', 'Arama Sonuclari Sablonu', 'Arama sonuclari sayfasi icin kullanilir'
  UNION ALL SELECT 'm1100010-0000-4000-8000-000000000010', 'SearchResultsPageTemplateEn', 'SearchResultsPageTemplate', 'EN', 'Search Results Template', 'Used for search results pages'
  UNION ALL SELECT 'm1100011-0000-4000-8000-000000000011', 'ErrorPageTemplateTr', 'ErrorPageTemplate', 'TR', 'Hata Sayfasi Sablonu', 'Genel hata sayfasi icin kullanilir'
  UNION ALL SELECT 'm1100012-0000-4000-8000-000000000012', 'ErrorPageTemplateEn', 'ErrorPageTemplate', 'EN', 'Error Page Template', 'Used for generic error pages'
  UNION ALL SELECT 'm1100013-0000-4000-8000-000000000013', 'NotFoundPageTemplateTr', 'NotFoundPageTemplate', 'TR', 'Bulunamadi Sayfasi Sablonu', '404 sayfasi icin kullanilir'
  UNION ALL SELECT 'm1100014-0000-4000-8000-000000000014', 'NotFoundPageTemplateEn', 'NotFoundPageTemplate', 'EN', 'Not Found Page Template', 'Used for 404 pages'
) data ON data.template_uid = pt.uid
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description);

-- ============================================================
-- 2. TEMPLATE SLOTS
-- ============================================================

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT data.uuid, data.uid, pt.id, data.slot_name, data.position, data.sort_order, data.is_required
FROM page_templates pt
JOIN (
  SELECT 'm1200001-0000-4000-8000-000000000001' AS uuid, 'LandingPageHeaderSlot' AS uid, 'LandingPageTemplate' AS template_uid, 'Header' AS slot_name, 'TOP' AS position, -1 AS sort_order, FALSE AS is_required
  UNION ALL SELECT 'm1200002-0000-4000-8000-000000000002', 'LandingPageSection1Slot', 'LandingPageTemplate', 'Section1', 'TOP', 1, FALSE
  UNION ALL SELECT 'm1200003-0000-4000-8000-000000000003', 'LandingPageSection2Slot', 'LandingPageTemplate', 'Section2', 'CENTER', 2, FALSE
  UNION ALL SELECT 'm1200004-0000-4000-8000-000000000004', 'LandingPageSection3Slot', 'LandingPageTemplate', 'Section3', 'CENTER', 3, FALSE
  UNION ALL SELECT 'm1200010-0000-4000-8000-000000000010', 'LandingPageFooterSlot', 'LandingPageTemplate', 'Footer', 'BOTTOM', 99, FALSE
  UNION ALL SELECT 'm1200011-0000-4000-8000-000000000011', 'ContentPageHeaderSlot', 'ContentPageTemplate', 'Header', 'TOP', -1, FALSE
  UNION ALL SELECT 'm1200012-0000-4000-8000-000000000012', 'ContentPageTopSlot', 'ContentPageTemplate', 'TopContent', 'TOP', 0, FALSE
  UNION ALL SELECT 'm1200013-0000-4000-8000-000000000013', 'ContentPageBodySlot', 'ContentPageTemplate', 'BodyContent', 'CENTER', 1, TRUE
  UNION ALL SELECT 'm1200014-0000-4000-8000-000000000014', 'ContentPageSideSlot', 'ContentPageTemplate', 'SideContent', 'RIGHT', 2, FALSE
  UNION ALL SELECT 'm1200015-0000-4000-8000-000000000015', 'ContentPageFooterSlot', 'ContentPageTemplate', 'Footer', 'BOTTOM', 99, FALSE
  UNION ALL SELECT 'm1200016-0000-4000-8000-000000000016', 'CategoryPageHeaderSlot', 'CategoryPageTemplate', 'Header', 'TOP', -1, FALSE
  UNION ALL SELECT 'm1200017-0000-4000-8000-000000000017', 'CategoryPageTopSlot', 'CategoryPageTemplate', 'TopContent', 'TOP', 0, FALSE
  UNION ALL SELECT 'm1200018-0000-4000-8000-000000000018', 'CategoryPageContentSlot', 'CategoryPageTemplate', 'ProductGrid', 'CENTER', 1, TRUE
  UNION ALL SELECT 'm1200019-0000-4000-8000-000000000019', 'CategoryPageFooterSlot', 'CategoryPageTemplate', 'Footer', 'BOTTOM', 99, FALSE
  UNION ALL SELECT 'm1200020-0000-4000-8000-000000000020', 'ProductDetailsPageHeaderSlot', 'ProductDetailsPageTemplate', 'Header', 'TOP', -1, FALSE
  UNION ALL SELECT 'm1200021-0000-4000-8000-000000000021', 'ProductDetailsSummarySlot', 'ProductDetailsPageTemplate', 'Summary', 'TOP', 0, TRUE
  UNION ALL SELECT 'm1200022-0000-4000-8000-000000000022', 'ProductDetailsTabsSlot', 'ProductDetailsPageTemplate', 'Tabs', 'CENTER', 1, FALSE
  UNION ALL SELECT 'm1200023-0000-4000-8000-000000000023', 'ProductDetailsCrossSellingSlot', 'ProductDetailsPageTemplate', 'CrossSelling', 'BOTTOM', 2, FALSE
  UNION ALL SELECT 'm1200024-0000-4000-8000-000000000024', 'ProductDetailsPageFooterSlot', 'ProductDetailsPageTemplate', 'Footer', 'BOTTOM', 99, FALSE
  UNION ALL SELECT 'm1200025-0000-4000-8000-000000000025', 'SearchResultsPageHeaderSlot', 'SearchResultsPageTemplate', 'Header', 'TOP', -1, FALSE
  UNION ALL SELECT 'm1200026-0000-4000-8000-000000000026', 'SearchResultsTopSlot', 'SearchResultsPageTemplate', 'TopContent', 'TOP', 0, FALSE
  UNION ALL SELECT 'm1200027-0000-4000-8000-000000000027', 'SearchResultsContentSlot', 'SearchResultsPageTemplate', 'Results', 'CENTER', 1, TRUE
  UNION ALL SELECT 'm1200028-0000-4000-8000-000000000028', 'SearchResultsPageFooterSlot', 'SearchResultsPageTemplate', 'Footer', 'BOTTOM', 99, FALSE
  UNION ALL SELECT 'm1200029-0000-4000-8000-000000000029', 'ErrorPageMiddleSlot', 'ErrorPageTemplate', 'MiddleContent', 'CENTER', 0, TRUE
  UNION ALL SELECT 'm1200030-0000-4000-8000-000000000030', 'NotFoundPageMiddleSlot', 'NotFoundPageTemplate', 'MiddleContent', 'CENTER', 0, TRUE
) data ON data.template_uid = pt.uid
ON DUPLICATE KEY UPDATE
  slot_name = VALUES(slot_name),
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_required = VALUES(is_required);

-- ============================================================
-- 3. SHARED SHELL COMPONENTS
-- ============================================================

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT data.uuid, data.uid, ct.id, data.name, data.display_order, data.is_visible, data.style_classes, data.custom_data, 'PUBLISHED', NOW(), NOW()
FROM component_types ct
JOIN (
  SELECT 'm1300001-0000-4000-8000-000000000001' AS uuid, 'StorefrontHeaderMainNavigation' AS uid, 'NavigationComponent' AS type_uid, 'Storefront Header Main Navigation' AS name, 0 AS display_order, TRUE AS is_visible, 'header-main-navigation' AS style_classes, JSON_OBJECT('layoutRole', 'header.mainNavigation') AS custom_data
  UNION ALL SELECT 'm1300009-0000-4000-8000-000000000009', 'StorefrontHeaderIntroBlock', 'CMSParagraphComponent', 'Storefront Header Intro Block', 1, TRUE, 'header-intro-block', JSON_OBJECT('layoutRole', 'header.primary.intro')
  UNION ALL SELECT 'm1300002-0000-4000-8000-000000000002', 'StorefrontHeaderSocialLinks', 'CMSLinkComponent', 'Storefront Header Social Links', 2, TRUE, 'header-social-links', JSON_OBJECT('layoutRole', 'header.socialLinks')
  UNION ALL SELECT 'm1300003-0000-4000-8000-000000000003', 'StorefrontHeaderContactInfo', 'CMSLinkComponent', 'Storefront Header Contact Info', 3, TRUE, 'header-contact-info', JSON_OBJECT('layoutRole', 'header.contactInfo')
  UNION ALL SELECT 'm1300004-0000-4000-8000-000000000004', 'StorefrontFooterBrandBlock', 'CMSParagraphComponent', 'Storefront Footer Brand Block', 0, TRUE, 'footer-brand-block', JSON_OBJECT('layoutRole', 'footer.brandBlock')
  UNION ALL SELECT 'm1300005-0000-4000-8000-000000000005', 'StorefrontFooterSitemapNavigation', 'NavigationComponent', 'Storefront Footer Sitemap Navigation', 1, TRUE, 'footer-sitemap-navigation', JSON_OBJECT('layoutRole', 'footer.sitemapNavigation')
  UNION ALL SELECT 'm1300006-0000-4000-8000-000000000006', 'StorefrontFooterOfficeLinks', 'CMSLinkComponent', 'Storefront Footer Office Links', 2, TRUE, 'footer-office-links', JSON_OBJECT('layoutRole', 'footer.officeLinks')
  UNION ALL SELECT 'm1300007-0000-4000-8000-000000000007', 'StorefrontFooterNewsletter', 'CMSLinkComponent', 'Storefront Footer Newsletter', 3, TRUE, 'footer-newsletter', JSON_OBJECT('layoutRole', 'footer.newsletter')
  UNION ALL SELECT 'm1300008-0000-4000-8000-000000000008', 'StorefrontFooterSocialLinks', 'CMSLinkComponent', 'Storefront Footer Social Links', 4, FALSE, 'footer-social-links', JSON_OBJECT('layoutRole', 'footer.socialLinks')
) data ON data.type_uid = ct.uid
ON DUPLICATE KEY UPDATE
  component_type_id = VALUES(component_type_id),
  name = VALUES(name),
  display_order = VALUES(display_order),
  is_visible = VALUES(is_visible),
  style_classes = VALUES(style_classes),
  custom_data = VALUES(custom_data),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT data.uuid, data.uid, c.id, data.language, data.title, NULL, data.description, 'PUBLISHED', NOW(), NOW()
FROM components c
JOIN (
  SELECT 'm1400001-0000-4000-8000-000000000001' AS uuid, 'StorefrontHeaderMainNavigationTr' AS uid, 'StorefrontHeaderMainNavigation' AS component_uid, 'TR' AS language, 'Ana Menu' AS title, NULL AS description
  UNION ALL SELECT 'm1400002-0000-4000-8000-000000000002', 'StorefrontHeaderMainNavigationEn', 'StorefrontHeaderMainNavigation', 'EN', 'Main Menu', NULL
  UNION ALL SELECT 'm1400017-0000-4000-8000-000000000017', 'StorefrontHeaderIntroBlockTr', 'StorefrontHeaderIntroBlock', 'TR', 'Merhaba!', 'Secili projeler, marka anlatilari ve yeni is birlikleri icin bize ulasin.'
  UNION ALL SELECT 'm1400018-0000-4000-8000-000000000018', 'StorefrontHeaderIntroBlockEn', 'StorefrontHeaderIntroBlock', 'EN', 'Hello!', 'Reach out for selected projects, brand stories and new collaborations.'
  UNION ALL SELECT 'm1400003-0000-4000-8000-000000000003', 'StorefrontHeaderSocialLinksTr', 'StorefrontHeaderSocialLinks', 'TR', 'Sosyal', NULL
  UNION ALL SELECT 'm1400004-0000-4000-8000-000000000004', 'StorefrontHeaderSocialLinksEn', 'StorefrontHeaderSocialLinks', 'EN', 'Social', NULL
  UNION ALL SELECT 'm1400005-0000-4000-8000-000000000005', 'StorefrontHeaderContactInfoTr', 'StorefrontHeaderContactInfo', 'TR', 'Iletisim', NULL
  UNION ALL SELECT 'm1400006-0000-4000-8000-000000000006', 'StorefrontHeaderContactInfoEn', 'StorefrontHeaderContactInfo', 'EN', 'Contact', NULL
  UNION ALL SELECT 'm1400007-0000-4000-8000-000000000007', 'StorefrontFooterBrandBlockTr', 'StorefrontFooterBrandBlock', 'TR', 'Ahmet Mulayim', 'Secili calismalari ve gorsel anlatilari bir araya getiren portfolio deneyimi.'
  UNION ALL SELECT 'm1400008-0000-4000-8000-000000000008', 'StorefrontFooterBrandBlockEn', 'StorefrontFooterBrandBlock', 'EN', 'Ahmet Mulayim', 'A portfolio experience bringing selected work and visual storytelling together.'
  UNION ALL SELECT 'm1400009-0000-4000-8000-000000000009', 'StorefrontFooterSitemapNavigationTr', 'StorefrontFooterSitemapNavigation', 'TR', 'Site Haritasi', NULL
  UNION ALL SELECT 'm1400010-0000-4000-8000-000000000010', 'StorefrontFooterSitemapNavigationEn', 'StorefrontFooterSitemapNavigation', 'EN', 'Sitemap', NULL
  UNION ALL SELECT 'm1400011-0000-4000-8000-000000000011', 'StorefrontFooterOfficeLinksTr', 'StorefrontFooterOfficeLinks', 'TR', 'Baglantilar', NULL
  UNION ALL SELECT 'm1400012-0000-4000-8000-000000000012', 'StorefrontFooterOfficeLinksEn', 'StorefrontFooterOfficeLinks', 'EN', 'Links', NULL
  UNION ALL SELECT 'm1400013-0000-4000-8000-000000000013', 'StorefrontFooterNewsletterTr', 'StorefrontFooterNewsletter', 'TR', 'Bulten', NULL
  UNION ALL SELECT 'm1400014-0000-4000-8000-000000000014', 'StorefrontFooterNewsletterEn', 'StorefrontFooterNewsletter', 'EN', 'Newsletter', NULL
  UNION ALL SELECT 'm1400015-0000-4000-8000-000000000015', 'StorefrontFooterSocialLinksTr', 'StorefrontFooterSocialLinks', 'TR', 'Sosyal', NULL
  UNION ALL SELECT 'm1400016-0000-4000-8000-000000000016', 'StorefrontFooterSocialLinksEn', 'StorefrontFooterSocialLinks', 'EN', 'Social', NULL
) data ON data.component_uid = c.uid
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT data.uuid, data.uid, c.id, data.sort_order, data.is_visible, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c
JOIN (
  SELECT 'm1500010-0000-4000-8000-000000000010' AS uuid, 'StorefrontHeaderSocialLinksEntry1' AS uid, 'StorefrontHeaderSocialLinks' AS component_uid, 0 AS sort_order, TRUE AS is_visible
  UNION ALL SELECT 'm1500011-0000-4000-8000-000000000011', 'StorefrontHeaderSocialLinksEntry2', 'StorefrontHeaderSocialLinks', 1, TRUE
  UNION ALL SELECT 'm1500012-0000-4000-8000-000000000012', 'StorefrontHeaderSocialLinksEntry3', 'StorefrontHeaderSocialLinks', 2, TRUE
  UNION ALL SELECT 'm1500013-0000-4000-8000-000000000013', 'StorefrontHeaderContactInfoEntry1', 'StorefrontHeaderContactInfo', 0, TRUE
  UNION ALL SELECT 'm1500014-0000-4000-8000-000000000014', 'StorefrontHeaderContactInfoEntry2', 'StorefrontHeaderContactInfo', 1, TRUE
  UNION ALL SELECT 'm1500001-0000-4000-8000-000000000001', 'StorefrontFooterOfficeLinksEntry1', 'StorefrontFooterOfficeLinks', 0, TRUE
  UNION ALL SELECT 'm1500002-0000-4000-8000-000000000002', 'StorefrontFooterNewsletterEntry1', 'StorefrontFooterNewsletter', 0, TRUE
) data ON data.component_uid = c.uid
ON DUPLICATE KEY UPDATE
  component_id = VALUES(component_id),
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT data.uuid, data.uid, e.id, data.language, data.title, NULL, 'PUBLISHED', data.custom_data, NOW(), NOW(), NOW()
FROM component_entries e
JOIN (
  SELECT 'm1600010-0000-4000-8000-000000000010' AS uuid, 'StorefrontHeaderSocialLinksEntry1Tr' AS uid, 'StorefrontHeaderSocialLinksEntry1' AS entry_uid, 'TR' AS language, 'Instagram' AS title, JSON_OBJECT('linkUrl', 'https://www.instagram.com', 'target', '_blank') AS custom_data
  UNION ALL SELECT 'm1600011-0000-4000-8000-000000000011', 'StorefrontHeaderSocialLinksEntry1En', 'StorefrontHeaderSocialLinksEntry1', 'EN', 'Instagram', JSON_OBJECT('linkUrl', 'https://www.instagram.com', 'target', '_blank')
  UNION ALL SELECT 'm1600012-0000-4000-8000-000000000012', 'StorefrontHeaderSocialLinksEntry2Tr', 'StorefrontHeaderSocialLinksEntry2', 'TR', 'Behance', JSON_OBJECT('linkUrl', 'https://www.behance.net', 'target', '_blank')
  UNION ALL SELECT 'm1600013-0000-4000-8000-000000000013', 'StorefrontHeaderSocialLinksEntry2En', 'StorefrontHeaderSocialLinksEntry2', 'EN', 'Behance', JSON_OBJECT('linkUrl', 'https://www.behance.net', 'target', '_blank')
  UNION ALL SELECT 'm1600014-0000-4000-8000-000000000014', 'StorefrontHeaderSocialLinksEntry3Tr', 'StorefrontHeaderSocialLinksEntry3', 'TR', 'Youtube', JSON_OBJECT('linkUrl', 'https://www.youtube.com', 'target', '_blank')
  UNION ALL SELECT 'm1600015-0000-4000-8000-000000000015', 'StorefrontHeaderSocialLinksEntry3En', 'StorefrontHeaderSocialLinksEntry3', 'EN', 'Youtube', JSON_OBJECT('linkUrl', 'https://www.youtube.com', 'target', '_blank')
  UNION ALL SELECT 'm1600016-0000-4000-8000-000000000016', 'StorefrontHeaderContactInfoEntry1Tr', 'StorefrontHeaderContactInfoEntry1', 'TR', 'info@ahmetmulayim.com', JSON_OBJECT('linkUrl', 'mailto:info@ahmetmulayim.com')
  UNION ALL SELECT 'm1600017-0000-4000-8000-000000000017', 'StorefrontHeaderContactInfoEntry1En', 'StorefrontHeaderContactInfoEntry1', 'EN', 'info@ahmetmulayim.com', JSON_OBJECT('linkUrl', 'mailto:info@ahmetmulayim.com')
  UNION ALL SELECT 'm1600018-0000-4000-8000-000000000018', 'StorefrontHeaderContactInfoEntry2Tr', 'StorefrontHeaderContactInfoEntry2', 'TR', 'www.ahmetmulayim.com', JSON_OBJECT('linkUrl', 'https://www.ahmetmulayim.com', 'target', '_blank')
  UNION ALL SELECT 'm1600019-0000-4000-8000-000000000019', 'StorefrontHeaderContactInfoEntry2En', 'StorefrontHeaderContactInfoEntry2', 'EN', 'www.ahmetmulayim.com', JSON_OBJECT('linkUrl', 'https://www.ahmetmulayim.com', 'target', '_blank')
  UNION ALL SELECT 'm1600001-0000-4000-8000-000000000001', 'StorefrontFooterOfficeLinksEntry1Tr', 'StorefrontFooterOfficeLinksEntry1', 'TR', 'www.ahmetmulayim.com', JSON_OBJECT('linkUrl', 'https://www.ahmetmulayim.com', 'target', '_blank')
  UNION ALL SELECT 'm1600002-0000-4000-8000-000000000002', 'StorefrontFooterOfficeLinksEntry1En', 'StorefrontFooterOfficeLinksEntry1', 'EN', 'www.ahmetmulayim.com', JSON_OBJECT('linkUrl', 'https://www.ahmetmulayim.com', 'target', '_blank')
  UNION ALL SELECT 'm1600003-0000-4000-8000-000000000003', 'StorefrontFooterNewsletterEntry1Tr', 'StorefrontFooterNewsletterEntry1', 'TR', NULL, JSON_OBJECT('inputPlaceholder', 'E-posta adresiniz', 'buttonLabel', 'Abone Ol')
  UNION ALL SELECT 'm1600004-0000-4000-8000-000000000004', 'StorefrontFooterNewsletterEntry1En', 'StorefrontFooterNewsletterEntry1', 'EN', NULL, JSON_OBJECT('inputPlaceholder', 'Enter your email', 'buttonLabel', 'Subscribe')
) data ON data.entry_uid = e.uid
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  status = VALUES(status),
  custom_data = VALUES(custom_data),
  published_at = VALUES(published_at),
  updated_at = NOW();

-- ============================================================
-- 4. NAVIGATION
-- ============================================================

INSERT INTO navigation_nodes (uuid, uid, parent_id, position, sort_order, is_visible, is_tab, created_at, updated_at, created_by, updated_by)
VALUES
  ('m1700001-0000-4000-8000-000000000001', 'LandingMainNavNode', NULL, 'TOP', 0, TRUE, FALSE, NOW(), NOW(), NULL, NULL),
  ('m1700002-0000-4000-8000-000000000002', 'LandingFooterNavNode', NULL, 'BOTTOM', 1, TRUE, FALSE, NOW(), NOW(), NULL, NULL)
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible),
  is_tab = VALUES(is_tab),
  updated_at = NOW();

INSERT INTO navigation_node_i18n (uid, uuid, node_id, language, title, created_at, updated_at, created_by, updated_by)
SELECT data.uid, data.uuid, n.id, data.language, data.title, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n
JOIN (
  SELECT 'm1800001-0000-4000-8000-000000000001' AS uid, 'm1801001-0000-4000-8000-000000000001' AS uuid, 'LandingMainNavNode' AS node_uid, 'TR' AS language, 'Ana Menu' AS title
  UNION ALL SELECT 'm1800002-0000-4000-8000-000000000002', 'm1801002-0000-4000-8000-000000000002', 'LandingMainNavNode', 'EN', 'Main Menu'
  UNION ALL SELECT 'm1800003-0000-4000-8000-000000000003', 'm1801003-0000-4000-8000-000000000003', 'LandingFooterNavNode', 'TR', 'Site Haritasi'
  UNION ALL SELECT 'm1800004-0000-4000-8000-000000000004', 'm1801004-0000-4000-8000-000000000004', 'LandingFooterNavNode', 'EN', 'Sitemap'
) data ON data.node_uid = n.uid
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  updated_at = NOW();

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT data.uuid, data.uid, n.id, 'URL', NULL, data.url, NULL, '_self', FALSE, data.sort_order, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n
JOIN (
  SELECT 'm1900001-0000-4000-8000-000000000001' AS uuid, 'LandingMainNavHomeEntry' AS uid, 'LandingMainNavNode' AS node_uid, '/' AS url, 0 AS sort_order
  UNION ALL SELECT 'm1900005-0000-4000-8000-000000000005', 'LandingMainNavAboutEntry', 'LandingMainNavNode', '/about', 1
  UNION ALL SELECT 'm1900002-0000-4000-8000-000000000002', 'LandingMainNavSearchEntry', 'LandingMainNavNode', '/search', 2
  UNION ALL SELECT 'm1900003-0000-4000-8000-000000000003', 'LandingFooterHomeEntry', 'LandingFooterNavNode', '/', 0
  UNION ALL SELECT 'm1900006-0000-4000-8000-000000000006', 'LandingFooterAboutEntry', 'LandingFooterNavNode', '/about', 1
  UNION ALL SELECT 'm1900004-0000-4000-8000-000000000004', 'LandingFooterSearchEntry', 'LandingFooterNavNode', '/search', 2
) data ON data.node_uid = n.uid
ON DUPLICATE KEY UPDATE
  node_id = VALUES(node_id),
  item_type = VALUES(item_type),
  item_id = VALUES(item_id),
  url = VALUES(url),
  target = VALUES(target),
  is_external = VALUES(is_external),
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible),
  updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT data.uid, data.uuid, e.id, data.language, data.link_name, NOW(), NOW(), NULL, NULL
FROM navigation_entries e
JOIN (
  SELECT 'm1950001-0000-4000-8000-000000000001' AS uid, 'm1951001-0000-4000-8000-000000000001' AS uuid, 'LandingMainNavHomeEntry' AS entry_uid, 'TR' AS language, 'Anasayfa' AS link_name
  UNION ALL SELECT 'm1950002-0000-4000-8000-000000000002', 'm1951002-0000-4000-8000-000000000002', 'LandingMainNavHomeEntry', 'EN', 'Home'
  UNION ALL SELECT 'm1950009-0000-4000-8000-000000000009', 'm1951009-0000-4000-8000-000000000009', 'LandingMainNavAboutEntry', 'TR', 'Hakkımda'
  UNION ALL SELECT 'm1950010-0000-4000-8000-000000000010', 'm1951010-0000-4000-8000-000000000010', 'LandingMainNavAboutEntry', 'EN', 'About'
  UNION ALL SELECT 'm1950003-0000-4000-8000-000000000003', 'm1951003-0000-4000-8000-000000000003', 'LandingMainNavSearchEntry', 'TR', 'Arama'
  UNION ALL SELECT 'm1950004-0000-4000-8000-000000000004', 'm1951004-0000-4000-8000-000000000004', 'LandingMainNavSearchEntry', 'EN', 'Search'
  UNION ALL SELECT 'm1950005-0000-4000-8000-000000000005', 'm1951005-0000-4000-8000-000000000005', 'LandingFooterHomeEntry', 'TR', 'Anasayfa'
  UNION ALL SELECT 'm1950006-0000-4000-8000-000000000006', 'm1951006-0000-4000-8000-000000000006', 'LandingFooterHomeEntry', 'EN', 'Home'
  UNION ALL SELECT 'm1950011-0000-4000-8000-000000000011', 'm1951011-0000-4000-8000-000000000011', 'LandingFooterAboutEntry', 'TR', 'Hakkımda'
  UNION ALL SELECT 'm1950012-0000-4000-8000-000000000012', 'm1951012-0000-4000-8000-000000000012', 'LandingFooterAboutEntry', 'EN', 'About'
  UNION ALL SELECT 'm1950007-0000-4000-8000-000000000007', 'm1951007-0000-4000-8000-000000000007', 'LandingFooterSearchEntry', 'TR', 'Arama'
  UNION ALL SELECT 'm1950008-0000-4000-8000-000000000008', 'm1951008-0000-4000-8000-000000000008', 'LandingFooterSearchEntry', 'EN', 'Search'
) data ON data.entry_uid = e.uid
ON DUPLICATE KEY UPDATE
  link_name = VALUES(link_name),
  updated_at = NOW();

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

-- ============================================================
-- 5. SEARCH PAGE
-- ============================================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'm2000001-0000-4000-8000-000000000001', 'search-page', pt.id, 'PUBLISHED', 'SEARCH', FALSE, 'INDEX_FOLLOW', NULL
FROM page_templates pt
WHERE pt.uid = 'SearchResultsPageTemplate'
ON DUPLICATE KEY UPDATE
  template_id = VALUES(template_id),
  status = VALUES(status),
  page_type = VALUES(page_type),
  is_home = VALUES(is_home),
  robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'm2100001-0000-4000-8000-000000000001', 'search-page-tr', p.id, 'TR', 'Arama', 'Arama', '/search', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'search-page'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'm2100002-0000-4000-8000-000000000002', 'search-page-en', p.id, 'EN', 'Search', 'Search', '/search', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'search-page'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);
