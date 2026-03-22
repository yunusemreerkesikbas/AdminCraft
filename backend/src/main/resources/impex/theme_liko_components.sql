-- #CRAFTIVE_IMPEX
-- Liko Home-2 Components — ImpEx seed file.
-- Run via Admin UI /{lang}/impex after R__seed_page_templates migration is applied.
-- Idempotent: safe to run multiple times.
-- UUID ranges: b1=components, b2=component_i18n, b3=entries, b4=entry_i18n

-- ============================================================
-- 1. COMPONENTS
-- ============================================================

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b1000001-0000-4000-8000-000000000001', 'HomepageHeroBanner', id, 'Homepage Hero Banner', 0, TRUE, 'hero-banner', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b1000002-0000-4000-8000-000000000002', 'HomepageAboutSection', id, 'Homepage About Section', 0, TRUE, 'about-section', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b1000003-0000-4000-8000-000000000003', 'HomepageVideoSection', id, 'Homepage Video Section', 0, TRUE, 'video-section', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b1000004-0000-4000-8000-000000000004', 'HomepageServiceSection', id, 'Homepage Service Section', 0, TRUE, 'service-section', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b1000005-0000-4000-8000-000000000005', 'HomepageProjectSection', id, 'Homepage Project Section', 0, TRUE, 'project-section', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b1000006-0000-4000-8000-000000000006', 'HomepageAwardSection', id, 'Homepage Award Section', 0, TRUE, 'award-section', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b1000007-0000-4000-8000-000000000007', 'HomepageMarqueeText', id, 'Homepage Marquee Text', 0, TRUE, 'marquee-text', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'CMSParagraphComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b1000008-0000-4000-8000-000000000008', 'HomepageInstagramSection', id, 'Homepage Instagram Section', 0, TRUE, 'instagram-section', 'PUBLISHED', NOW(), NOW()
FROM component_types WHERE uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW();

-- ============================================================
-- 2. COMPONENT_I18N (TR + EN)
-- ============================================================

-- HomepageHeroBanner
INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000001-0000-4000-8000-000000000001', 'HomepageHeroBannerTr', c.id, 'TR',
    'Fashion', '& Branding',
    'Moda markanızın benzersiz kimliğini stratejik pazarlama ve reklamcılık yoluyla hayata geçiriyoruz.',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageHeroBanner'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000002-0000-4000-8000-000000000002', 'HomepageHeroBannerEn', c.id, 'EN',
    'Fashion', '& Branding',
    'Bringing Your Fashion Brand''s Unique Identity to Life Through Strategic Marketing and Advertising.',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageHeroBanner'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

-- HomepageAboutSection
INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000003-0000-4000-8000-000000000003', 'HomepageAboutSectionTr', c.id, 'TR',
    'Çeşitli şekil ve formatlarda işbirliği mümkündür',
    'EN İYİ GÖZLÜK İLHAMI İÇİN TAKİP EDİN',
    'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip.',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageAboutSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000004-0000-4000-8000-000000000004', 'HomepageAboutSectionEn', c.id, 'EN',
    'Cooperation is possible within various shapes and formats',
    'FOLLOW FOR THE BEST EYEWEAR INSPIRATION',
    'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip.',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageAboutSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

-- HomepageVideoSection
INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000005-0000-4000-8000-000000000005', 'HomepageVideoSectionTr', c.id, 'TR',
    'Play Reel', 'Hareket halinde çalışma',
    'Çalışmalarımız hareket halinde en iyi şekilde deneyimlenir. Kulaklıklarınızı takmayı unutmayın.',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageVideoSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000006-0000-4000-8000-000000000006', 'HomepageVideoSectionEn', c.id, 'EN',
    'Play Reel', 'Work in motion',
    'Our work is best experienced in motion. Don''t forget to put on your headphones.',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageVideoSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

-- HomepageServiceSection
INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000007-0000-4000-8000-000000000007', 'HomepageServiceSectionTr', c.id, 'TR',
    'Etkili bir bütün çözüm olarak strateji, tasarım ve uygulama. Dijital müşteri mıknatısı olarak özgün web siteniz.',
    'HİZMETLER', NULL,
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageServiceSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000008-0000-4000-8000-000000000008', 'HomepageServiceSectionEn', c.id, 'EN',
    'Strategy, design and implementation as an effective complete solution. Your authentic website as a digital customer magnet.',
    'SERVICES', NULL,
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageServiceSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

-- HomepageProjectSection
INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000009-0000-4000-8000-000000000009', 'HomepageProjectSectionTr', c.id, 'TR',
    NULL, NULL, NULL,
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageProjectSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000010-0000-4000-8000-000000000010', 'HomepageProjectSectionEn', c.id, 'EN',
    NULL, NULL, NULL,
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageProjectSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

-- HomepageAwardSection
INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000011-0000-4000-8000-000000000011', 'HomepageAwardSectionTr', c.id, 'TR',
    'award winning agency', 'Accolades',
    'Kalite, yaptığımız her şeyin ortak paydası. Bu sadece söylediğimiz bir şey değil, bunun için tanındık. Ve bu ödülleri, işleri yapış biçiminizin ne kadar önemli olduğunu hatırlatan birer anı olarak gururla saklıyoruz.',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageAwardSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000012-0000-4000-8000-000000000012', 'HomepageAwardSectionEn', c.id, 'EN',
    'award winning agency', 'Accolades',
    'Quality is the common thread in everything we do. It''s not something we say, it''s something we''ve been recognized for. And we proudly hold these awards as a reminder of how the way you do things matters.',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageAwardSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

-- HomepageMarqueeText
INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000013-0000-4000-8000-000000000013', 'HomepageMarqueeTextTr', c.id, 'TR',
    NULL, NULL,
    'LinkedIn - Facebook - Twitter - Facebook - Twitter - Facebook - LinkedIn',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageMarqueeText'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000014-0000-4000-8000-000000000014', 'HomepageMarqueeTextEn', c.id, 'EN',
    NULL, NULL,
    'LinkedIn - Facebook - Twitter - Facebook - Twitter - Facebook - LinkedIn',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageMarqueeText'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

-- HomepageInstagramSection
INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000015-0000-4000-8000-000000000015', 'HomepageInstagramSectionTr', c.id, 'TR',
    '@likoagency', 'INSTAGRAM',
    'Hikayelerimizin bir parçası olun! Maceraya katılın.',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageInstagramSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT 'b2000016-0000-4000-8000-000000000016', 'HomepageInstagramSectionEn', c.id, 'EN',
    '@likoagency', 'INSTAGRAM',
    'Become a part of our stories! Join the adventure.',
    'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageInstagramSection'
ON DUPLICATE KEY UPDATE title = VALUES(title), subtitle = VALUES(subtitle), description = VALUES(description), status = VALUES(status), updated_at = NOW();

-- ============================================================
-- 3. COMPONENT_ENTRIES
-- ============================================================

-- HomepageHeroBanner — 1 entry
INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000001-0000-4000-8000-000000000001', 'HomepageHeroBannerEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageHeroBanner'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

-- HomepageAboutSection — 3 entries (3 images)
INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000002-0000-4000-8000-000000000002', 'HomepageAboutSectionEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageAboutSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000003-0000-4000-8000-000000000003', 'HomepageAboutSectionEntry2', c.id, 1, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageAboutSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000004-0000-4000-8000-000000000004', 'HomepageAboutSectionEntry3', c.id, 2, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageAboutSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

-- HomepageVideoSection — 1 entry
INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000005-0000-4000-8000-000000000005', 'HomepageVideoSectionEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageVideoSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

-- HomepageServiceSection — 4 entries
INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000006-0000-4000-8000-000000000006', 'HomepageServiceSectionEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageServiceSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000007-0000-4000-8000-000000000007', 'HomepageServiceSectionEntry2', c.id, 1, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageServiceSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000008-0000-4000-8000-000000000008', 'HomepageServiceSectionEntry3', c.id, 2, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageServiceSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000009-0000-4000-8000-000000000009', 'HomepageServiceSectionEntry4', c.id, 3, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageServiceSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

-- HomepageProjectSection — 7 entries
INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000010-0000-4000-8000-000000000010', 'HomepageProjectSectionEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageProjectSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000011-0000-4000-8000-000000000011', 'HomepageProjectSectionEntry2', c.id, 1, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageProjectSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000012-0000-4000-8000-000000000012', 'HomepageProjectSectionEntry3', c.id, 2, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageProjectSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000013-0000-4000-8000-000000000013', 'HomepageProjectSectionEntry4', c.id, 3, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageProjectSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000014-0000-4000-8000-000000000014', 'HomepageProjectSectionEntry5', c.id, 4, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageProjectSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000015-0000-4000-8000-000000000015', 'HomepageProjectSectionEntry6', c.id, 5, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageProjectSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000016-0000-4000-8000-000000000016', 'HomepageProjectSectionEntry7', c.id, 6, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageProjectSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

-- HomepageAwardSection — 1 entry
INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000017-0000-4000-8000-000000000017', 'HomepageAwardSectionEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageAwardSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

-- HomepageInstagramSection — 7 entries (grid images)
INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000018-0000-4000-8000-000000000018', 'HomepageInstagramSectionEntry1', c.id, 0, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageInstagramSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000019-0000-4000-8000-000000000019', 'HomepageInstagramSectionEntry2', c.id, 1, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageInstagramSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000020-0000-4000-8000-000000000020', 'HomepageInstagramSectionEntry3', c.id, 2, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageInstagramSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000021-0000-4000-8000-000000000021', 'HomepageInstagramSectionEntry4', c.id, 3, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageInstagramSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000022-0000-4000-8000-000000000022', 'HomepageInstagramSectionEntry5', c.id, 4, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageInstagramSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000023-0000-4000-8000-000000000023', 'HomepageInstagramSectionEntry6', c.id, 5, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageInstagramSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT 'b3000024-0000-4000-8000-000000000024', 'HomepageInstagramSectionEntry7', c.id, 6, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM components c WHERE c.uid = 'HomepageInstagramSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), status = VALUES(status), updated_at = NOW();

-- ============================================================
-- 4. COMPONENT_ENTRY_I18N
-- ============================================================

-- HomepageHeroBannerEntry1
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000001-0000-4000-8000-000000000001', 'HomepageHeroBannerEntry1Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-hero-bg', 'buttonUrl', '/portfolio-grid-col-3-fullwidth', 'buttonText', 'Daha Fazla Gör'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageHeroBannerEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000002-0000-4000-8000-000000000002', 'HomepageHeroBannerEntry1En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-hero-bg', 'buttonUrl', '/portfolio-grid-col-3-fullwidth', 'buttonText', 'View More'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageHeroBannerEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageAboutSectionEntry1 (ab-1, main left image)
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000003-0000-4000-8000-000000000003', 'HomepageAboutSectionEntry1Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-about-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageAboutSectionEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000004-0000-4000-8000-000000000004', 'HomepageAboutSectionEntry1En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-about-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageAboutSectionEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageAboutSectionEntry2 (ab-2, inner image with label)
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000005-0000-4000-8000-000000000005', 'HomepageAboutSectionEntry2Tr', e.id, 'TR',
    'BİR GÜNEŞ GÖZLÜĞÜ ETKİLEYİCİSİYİM', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-about-2'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageAboutSectionEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000006-0000-4000-8000-000000000006', 'HomepageAboutSectionEntry2En', e.id, 'EN',
    'I''M A SUNGLASSES INFLUENCER', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-about-2'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageAboutSectionEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageAboutSectionEntry3 (ab-3, right image)
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000007-0000-4000-8000-000000000007', 'HomepageAboutSectionEntry3Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-about-3'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageAboutSectionEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000008-0000-4000-8000-000000000008', 'HomepageAboutSectionEntry3En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-about-3'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageAboutSectionEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageVideoSectionEntry1
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000009-0000-4000-8000-000000000009', 'HomepageVideoSectionEntry1Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED',
    JSON_OBJECT('videoUrl', 'https://html.aqlova.com/videos/liko/liko.mp4', 'mediaUid', 'homepage-video-poster'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageVideoSectionEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000010-0000-4000-8000-000000000010', 'HomepageVideoSectionEntry1En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED',
    JSON_OBJECT('videoUrl', 'https://html.aqlova.com/videos/liko/liko.mp4', 'mediaUid', 'homepage-video-poster'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageVideoSectionEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageServiceSectionEntry1 (Branding)
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000011-0000-4000-8000-000000000011', 'HomepageServiceSectionEntry1Tr', e.id, 'TR',
    'Markalaşma',
    'Güçlü bir marka kimliği oluşturmak, moda markalarının pazarda kendini kanıtlaması için şarttır. Hizmetlerimiz, stratejik markalaşma girişimleriyle moda markalarına benzersiz kimliklerini tanımlamalarında yardımcı olmaya adanmıştır.',
    'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-service-icon-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageServiceSectionEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000012-0000-4000-8000-000000000012', 'HomepageServiceSectionEntry1En', e.id, 'EN',
    'Branding',
    'Creating a strong brand identity is essential for fashion brands to establish themselves in the market. Our services are dedicated to helping fashion brands define their unique identity through strategic branding initiatives.',
    'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-service-icon-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageServiceSectionEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageServiceSectionEntry2 (Identity)
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000013-0000-4000-8000-000000000013', 'HomepageServiceSectionEntry2Tr', e.id, 'TR',
    'Kimlik',
    'Güçlü bir marka kimliği oluşturmak, moda markalarının pazarda kendini kanıtlaması için şarttır. Hizmetlerimiz, stratejik markalaşma girişimleriyle moda markalarına benzersiz kimliklerini tanımlamalarında yardımcı olmaya adanmıştır.',
    'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-service-icon-2'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageServiceSectionEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000014-0000-4000-8000-000000000014', 'HomepageServiceSectionEntry2En', e.id, 'EN',
    'Identity',
    'Creating a strong brand identity is essential for fashion brands to establish themselves in the market. Our services are dedicated to helping fashion brands define their unique identity through strategic branding initiatives.',
    'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-service-icon-2'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageServiceSectionEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageServiceSectionEntry3 (Ecommerce)
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000015-0000-4000-8000-000000000015', 'HomepageServiceSectionEntry3Tr', e.id, 'TR',
    'E-Ticaret',
    'Güçlü bir marka kimliği oluşturmak, moda markalarının pazarda kendini kanıtlaması için şarttır. Hizmetlerimiz, stratejik markalaşma girişimleriyle moda markalarına benzersiz kimliklerini tanımlamalarında yardımcı olmaya adanmıştır.',
    'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-service-icon-3'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageServiceSectionEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000016-0000-4000-8000-000000000016', 'HomepageServiceSectionEntry3En', e.id, 'EN',
    'Ecommerce',
    'Creating a strong brand identity is essential for fashion brands to establish themselves in the market. Our services are dedicated to helping fashion brands define their unique identity through strategic branding initiatives.',
    'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-service-icon-3'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageServiceSectionEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageServiceSectionEntry4 (Marketing)
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000017-0000-4000-8000-000000000017', 'HomepageServiceSectionEntry4Tr', e.id, 'TR',
    'Pazarlama',
    'Güçlü bir marka kimliği oluşturmak, moda markalarının pazarda kendini kanıtlaması için şarttır. Hizmetlerimiz, stratejik markalaşma girişimleriyle moda markalarına benzersiz kimliklerini tanımlamalarında yardımcı olmaya adanmıştır.',
    'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-service-icon-4'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageServiceSectionEntry4'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000018-0000-4000-8000-000000000018', 'HomepageServiceSectionEntry4En', e.id, 'EN',
    'Marketing',
    'Creating a strong brand identity is essential for fashion brands to establish themselves in the market. Our services are dedicated to helping fashion brands define their unique identity through strategic branding initiatives.',
    'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-service-icon-4'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageServiceSectionEntry4'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageProjectSection entries (TR and EN share same content — subtitle stored in custom_data)
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000019-0000-4000-8000-000000000019', 'HomepageProjectSectionEntry1Tr', e.id, 'TR',
    'High Lights', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-1', 'subtitle', 'Concept', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000020-0000-4000-8000-000000000020', 'HomepageProjectSectionEntry1En', e.id, 'EN',
    'High Lights', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-1', 'subtitle', 'Concept', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000021-0000-4000-8000-000000000021', 'HomepageProjectSectionEntry2Tr', e.id, 'TR',
    'Fashion', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-2', 'subtitle', 'Branding', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000022-0000-4000-8000-000000000022', 'HomepageProjectSectionEntry2En', e.id, 'EN',
    'Fashion', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-2', 'subtitle', 'Branding', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry2'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000023-0000-4000-8000-000000000023', 'HomepageProjectSectionEntry3Tr', e.id, 'TR',
    'Branding', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-3', 'subtitle', 'Concept', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000024-0000-4000-8000-000000000024', 'HomepageProjectSectionEntry3En', e.id, 'EN',
    'Branding', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-3', 'subtitle', 'Concept', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry3'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000025-0000-4000-8000-000000000025', 'HomepageProjectSectionEntry4Tr', e.id, 'TR',
    'High Lights', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-4', 'subtitle', 'Concept', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry4'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000026-0000-4000-8000-000000000026', 'HomepageProjectSectionEntry4En', e.id, 'EN',
    'High Lights', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-4', 'subtitle', 'Concept', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry4'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000027-0000-4000-8000-000000000027', 'HomepageProjectSectionEntry5Tr', e.id, 'TR',
    'High Lights', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-5', 'subtitle', 'Concept', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry5'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000028-0000-4000-8000-000000000028', 'HomepageProjectSectionEntry5En', e.id, 'EN',
    'High Lights', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-5', 'subtitle', 'Concept', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry5'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000029-0000-4000-8000-000000000029', 'HomepageProjectSectionEntry6Tr', e.id, 'TR',
    'Fashion', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-6', 'subtitle', 'Branding', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry6'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000030-0000-4000-8000-000000000030', 'HomepageProjectSectionEntry6En', e.id, 'EN',
    'Fashion', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-6', 'subtitle', 'Branding', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry6'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000031-0000-4000-8000-000000000031', 'HomepageProjectSectionEntry7Tr', e.id, 'TR',
    'Branding', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-7', 'subtitle', 'Concept', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry7'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000032-0000-4000-8000-000000000032', 'HomepageProjectSectionEntry7En', e.id, 'EN',
    'Branding', NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-project-7', 'subtitle', 'Concept', 'linkUrl', '/portfolio-details-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageProjectSectionEntry7'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageAwardSectionEntry1
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000033-0000-4000-8000-000000000033', 'HomepageAwardSectionEntry1Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-award-1', 'buttonUrl', '/portfolio-details-1', 'buttonText', 'Ödüllerimizi Gör'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageAwardSectionEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000034-0000-4000-8000-000000000034', 'HomepageAwardSectionEntry1En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED',
    JSON_OBJECT('mediaUid', 'homepage-award-1', 'buttonUrl', '/portfolio-details-1', 'buttonText', 'See Our Awards'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageAwardSectionEntry1'
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

-- HomepageInstagramSection entries
INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000035-0000-4000-8000-000000000035', 'HomepageInstagramSectionEntry1Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry1'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000036-0000-4000-8000-000000000036', 'HomepageInstagramSectionEntry1En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-1'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry1'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000037-0000-4000-8000-000000000037', 'HomepageInstagramSectionEntry2Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-2'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry2'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000038-0000-4000-8000-000000000038', 'HomepageInstagramSectionEntry2En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-2'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry2'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000039-0000-4000-8000-000000000039', 'HomepageInstagramSectionEntry3Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-3'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry3'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000040-0000-4000-8000-000000000040', 'HomepageInstagramSectionEntry3En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-3'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry3'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000041-0000-4000-8000-000000000041', 'HomepageInstagramSectionEntry4Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-4'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry4'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000042-0000-4000-8000-000000000042', 'HomepageInstagramSectionEntry4En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-4'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry4'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000043-0000-4000-8000-000000000043', 'HomepageInstagramSectionEntry5Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-5'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry5'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000044-0000-4000-8000-000000000044', 'HomepageInstagramSectionEntry5En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-5'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry5'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000045-0000-4000-8000-000000000045', 'HomepageInstagramSectionEntry6Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-6'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry6'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000046-0000-4000-8000-000000000046', 'HomepageInstagramSectionEntry6En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-6'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry6'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000047-0000-4000-8000-000000000047', 'HomepageInstagramSectionEntry7Tr', e.id, 'TR',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-7'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry7'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT 'b4000048-0000-4000-8000-000000000048', 'HomepageInstagramSectionEntry7En', e.id, 'EN',
    NULL, NULL, 'PUBLISHED', JSON_OBJECT('mediaUid', 'homepage-instagram-7'),
    NOW(), NOW(), NOW()
FROM component_entries e WHERE e.uid = 'HomepageInstagramSectionEntry7'
ON DUPLICATE KEY UPDATE status = VALUES(status), custom_data = VALUES(custom_data), published_at = VALUES(published_at), updated_at = NOW();
