-- #CRAFTIVE_IMPEX
-- Service Page — generic CMS seed (TR + EN)
-- Content source: liko-next-js/src/pages/service/service.tsx (+ dependent components)
-- Idempotent: safe to run multiple times.

-- ============================================================
-- 1. COMPONENT TYPES (create missing ones)
-- ============================================================

INSERT INTO component_types (uuid, uid, name, category, is_navigation_aware, created_at, updated_at)
VALUES
  (UUID(), 'ServiceHeroComponent',   'Service Hero',     'hero',    false, NOW(), NOW()),
  (UUID(), 'ServiceCardsGridComponent', 'Service Cards Grid', 'layout', false, NOW(), NOW()),
  (UUID(), 'ServicePanelComponent',  'Service Panels',   'content', false, NOW(), NOW()),
  (UUID(), 'BrandGridComponent',    'Brand Grid',      'gallery', false, NOW(), NOW()),
  (UUID(), 'ImageMarqueeComponent', 'Image Marquee',   'gallery', false, NOW(), NOW()),
  (UUID(), 'BigTextCtaComponent',   'Big Text CTA',    'content', false, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  is_navigation_aware = VALUES(is_navigation_aware),
  updated_at = NOW();

-- ============================================================
-- 2. COMPONENTS
-- ============================================================

INSERT INTO components (
  uuid,
  uid,
  component_type_id,
  name,
  display_order,
  is_visible,
  style_classes,
  status,
  created_at,
  updated_at
)
SELECT seed.uuid, seed.uid, ct.id, seed.name, seed.display_order, TRUE, seed.style_classes, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT
    'e1101001-0000-4000-8000-000000000001' AS uuid,
    'ServiceHeroComponent' AS uid,
    'ServiceHeroComponent' AS component_type_uid,
    'Service Hero' AS name,
    0 AS display_order,
    'service-hero' AS style_classes
  UNION ALL SELECT 'e1101002-0000-4000-8000-000000000002', 'ServiceServicesComponent', 'ServiceCardsGridComponent', 'Service Cards Grid', 0, 'service-cards-grid'
  UNION ALL SELECT 'e1101003-0000-4000-8000-000000000003', 'ServicePanelsComponent', 'ServicePanelComponent', 'Service Panels', 0, 'service-panels'
  UNION ALL SELECT 'e1101004-0000-4000-8000-000000000004', 'ServiceBrandsComponent', 'BrandGridComponent', 'Brand Grid', 0, 'service-brands'
  UNION ALL SELECT 'e1101005-0000-4000-8000-000000000005', 'ServicePortsMarqueeComponent', 'ImageMarqueeComponent', 'Port Image Marquee', 0, 'service-ports-marquee'
  UNION ALL SELECT 'e1101006-0000-4000-8000-000000000006', 'ServiceBigTextCtaComponent', 'BigTextCtaComponent', 'Big Text CTA', 0, 'service-big-text-cta'
) seed
JOIN component_types ct ON ct.uid = seed.component_type_uid
ON DUPLICATE KEY UPDATE
  component_type_id = VALUES(component_type_id),
  name = VALUES(name),
  display_order = VALUES(display_order),
  is_visible = VALUES(is_visible),
  style_classes = VALUES(style_classes),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 3. COMPONENT_I18N
-- ============================================================

INSERT INTO component_i18n (
  uuid,
  uid,
  component_id,
  language,
  title,
  subtitle,
  description,
  status,
  created_at,
  updated_at
)
SELECT seed.uuid, seed.uid, c.id, seed.language, seed.title, seed.subtitle, seed.description, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT
    'e1202001-0000-4000-8000-000000000001' AS uuid,
    'ServiceHeroComponentTr' AS uid,
    'ServiceHeroComponent' AS component_uid,
    'TR' AS language,
    'Hizmetlerin en iyisini deneyimleyin.' AS title,
    NULL AS subtitle,
    'Sürekli gelişen doğru yönlendirme ile profesyonel bir anlayışla ilerliyoruz.' AS description
  UNION ALL SELECT 'e1202002-0000-4000-8000-000000000002', 'ServiceHeroComponentEn', 'ServiceHeroComponent', 'EN', 'Experience the best services.', NULL, 'Fulfilled direction use continual set him propriety continued.'

  UNION ALL SELECT 'e1202003-0000-4000-8000-000000000003', 'ServiceServicesComponentTr', 'ServiceServicesComponent', 'TR',
    'Yalnızca stratejiyle güçlendirilmiş tasarımın gerçek sonuçlar sağlayabileceğine inanıyoruz.', 'Hizmetler', NULL
  UNION ALL SELECT 'e1202004-0000-4000-8000-000000000004', 'ServiceServicesComponentEn', 'ServiceServicesComponent', 'EN',
    'We strongly believe that only design reinforced by strategy can provide real results.', 'Services', NULL

  UNION ALL SELECT 'e1202005-0000-4000-8000-000000000005', 'ServicePanelsComponentTr', 'ServicePanelsComponent', 'TR',
    NULL, NULL, NULL
  UNION ALL SELECT 'e1202006-0000-4000-8000-000000000006', 'ServicePanelsComponentEn', 'ServicePanelsComponent', 'EN',
    NULL, NULL, NULL

  UNION ALL SELECT 'e1202007-0000-4000-8000-000000000007', 'ServiceBrandsComponentTr', 'ServiceBrandsComponent', 'TR',
    NULL, NULL, NULL
  UNION ALL SELECT 'e1202008-0000-4000-8000-000000000008', 'ServiceBrandsComponentEn', 'ServiceBrandsComponent', 'EN',
    NULL, NULL, NULL

  UNION ALL SELECT 'e1202009-0000-4000-8000-000000000009', 'ServicePortsMarqueeComponentTr', 'ServicePortsMarqueeComponent', 'TR',
    NULL, NULL, NULL
  UNION ALL SELECT 'e1202010-0000-4000-8000-000000000010', 'ServicePortsMarqueeComponentEn', 'ServicePortsMarqueeComponent', 'EN',
    NULL, NULL, NULL

  UNION ALL SELECT 'e1202011-0000-4000-8000-000000000011', 'ServiceBigTextCtaComponentTr', 'ServiceBigTextCtaComponent', 'TR',
    'DİJİTAL TASARIM DENEYİMİ', 'YARATICI STÜDYO', NULL
  UNION ALL SELECT 'e1202012-0000-4000-8000-000000000012', 'ServiceBigTextCtaComponentEn', 'ServiceBigTextCtaComponent', 'EN',
    'DIGITAL DESIGN EXPERIENCE', 'CREATIVE STUDIO', NULL
) seed
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 4. COMPONENT_ENTRIES
-- ============================================================

INSERT INTO component_entries (
  uuid,
  uid,
  component_id,
  sort_order,
  is_visible,
  style_classes,
  status,
  created_at,
  updated_at
)
SELECT seed.uuid, seed.uid, c.id, seed.sort_order, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM (
  -- Hero background entry
  SELECT 'e1303001-0000-4000-8000-000000000001' AS uuid, 'ServiceHeroBackground' AS uid, 'ServiceHeroComponent' AS component_uid, 0 AS sort_order

  -- Accordion entries (service-five data)
  UNION ALL SELECT 'e1303002-0000-4000-8000-000000000002', 'ServiceItemBranding', 'ServiceServicesComponent', 0
  UNION ALL SELECT 'e1303003-0000-4000-8000-000000000003', 'ServiceItemWebsiteDesign', 'ServiceServicesComponent', 1
  UNION ALL SELECT 'e1303004-0000-4000-8000-000000000004', 'ServiceItemMarketing', 'ServiceServicesComponent', 2

  -- Panels entries (service-six data)
  UNION ALL SELECT 'e1303005-0000-4000-8000-000000000005', 'ServicePanel1', 'ServicePanelsComponent', 0
  UNION ALL SELECT 'e1303006-0000-4000-8000-000000000006', 'ServicePanel2', 'ServicePanelsComponent', 1
  UNION ALL SELECT 'e1303007-0000-4000-8000-000000000007', 'ServicePanel3', 'ServicePanelsComponent', 2
  UNION ALL SELECT 'e1303008-0000-4000-8000-000000000008', 'ServicePanel4', 'ServicePanelsComponent', 3

  -- Brand grid entries
  UNION ALL SELECT 'e1303009-0000-4000-8000-000000000009', 'ServiceBrand1', 'ServiceBrandsComponent', 0
  UNION ALL SELECT 'e1303010-0000-4000-8000-000000000010', 'ServiceBrand2', 'ServiceBrandsComponent', 1
  UNION ALL SELECT 'e1303011-0000-4000-8000-000000000011', 'ServiceBrand3', 'ServiceBrandsComponent', 2
  UNION ALL SELECT 'e1303012-0000-4000-8000-000000000012', 'ServiceBrand4', 'ServiceBrandsComponent', 3
  UNION ALL SELECT 'e1303013-0000-4000-8000-000000000013', 'ServiceBrand5', 'ServiceBrandsComponent', 4
  UNION ALL SELECT 'e1303014-0000-4000-8000-000000000014', 'ServiceBrand6', 'ServiceBrandsComponent', 5
  UNION ALL SELECT 'e1303015-0000-4000-8000-000000000015', 'ServiceBrand7', 'ServiceBrandsComponent', 6
  UNION ALL SELECT 'e1303016-0000-4000-8000-000000000016', 'ServiceBrand8', 'ServiceBrandsComponent', 7

  -- Port marquee entries (service line image slider order)
  UNION ALL SELECT 'e1303017-0000-4000-8000-000000000017', 'ServicePortEntry1', 'ServicePortsMarqueeComponent', 0
  UNION ALL SELECT 'e1303018-0000-4000-8000-000000000018', 'ServicePortEntry2', 'ServicePortsMarqueeComponent', 1
  UNION ALL SELECT 'e1303019-0000-4000-8000-000000000019', 'ServicePortEntry3', 'ServicePortsMarqueeComponent', 2
  UNION ALL SELECT 'e1303020-0000-4000-8000-000000000020', 'ServicePortEntry4', 'ServicePortsMarqueeComponent', 3
  UNION ALL SELECT 'e1303021-0000-4000-8000-000000000021', 'ServicePortEntry5', 'ServicePortsMarqueeComponent', 4
  UNION ALL SELECT 'e1303022-0000-4000-8000-000000000022', 'ServicePortEntry6', 'ServicePortsMarqueeComponent', 5
  UNION ALL SELECT 'e1303023-0000-4000-8000-000000000023', 'ServicePortEntry7', 'ServicePortsMarqueeComponent', 6

  -- Big text CTA button entry
  UNION ALL SELECT 'e1303024-0000-4000-8000-000000000024', 'ServiceBigTextCtaButton', 'ServiceBigTextCtaComponent', 0
) seed
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  component_id = VALUES(component_id),
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible),
  style_classes = VALUES(style_classes),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 5. COMPONENT_ENTRY_I18N
-- ============================================================

INSERT INTO component_entry_i18n (
  uuid,
  uid,
  entry_id,
  language,
  title,
  description,
  status,
  custom_data,
  published_at,
  created_at,
  updated_at
)
SELECT seed.uuid, seed.uid, e.id, seed.language, seed.title, seed.description, 'PUBLISHED', seed.custom_data, NOW(), NOW(), NOW()
FROM (
  -- Service hero background
  SELECT
    'e1404001-0000-4000-8000-000000000001' AS uuid,
    'ServiceHeroBackgroundTr' AS uid,
    'ServiceHeroBackground' AS entry_uid,
    'TR' AS language,
    NULL AS title,
    NULL AS description,
    JSON_OBJECT('mediaUid', 'service-hero-bg', 'overlayMediaUid', 'service-hero-shape') AS custom_data
  UNION ALL SELECT 'e1404002-0000-4000-8000-000000000002', 'ServiceHeroBackgroundEn', 'ServiceHeroBackground', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'service-hero-bg', 'overlayMediaUid', 'service-hero-shape')

  -- Accordion entries
  UNION ALL SELECT 'e1404003-0000-4000-8000-000000000003', 'ServiceItemBrandingTr', 'ServiceItemBranding', 'TR',
    'Markalaşma',
    'Markalaşma, herhangi bir işletmenin başarısında en önemli bileşenlerden biridir.',
    JSON_OBJECT('mediaUid', 'service-icon-2')
  UNION ALL SELECT 'e1404004-0000-4000-8000-000000000004', 'ServiceItemBrandingEn', 'ServiceItemBranding', 'EN',
    'Branding',
    'Branding is one of the most important ingredients for the success of any business.',
    JSON_OBJECT('mediaUid', 'service-icon-2')

  UNION ALL SELECT 'e1404005-0000-4000-8000-000000000005', 'ServiceItemWebsiteDesignTr', 'ServiceItemWebsiteDesign', 'TR',
    'Web Tasarımı',
    'İçeceğinizin bardağının boyutu ne olursa olsun, sunumunuz aynı şekilde kusursuz görünmeli ve tadı da mükemmel kalmalıdır.',
    JSON_OBJECT('mediaUid', 'service-icon-1')
  UNION ALL SELECT 'e1404006-0000-4000-8000-000000000006', 'ServiceItemWebsiteDesignEn', 'ServiceItemWebsiteDesign', 'EN',
    'Website design',
    'The perfect cocktail should still look and taste perfect no matter the size of the glass you serve it in.',
    JSON_OBJECT('mediaUid', 'service-icon-1')

  UNION ALL SELECT 'e1404007-0000-4000-8000-000000000007', 'ServiceItemMarketingTr', 'ServiceItemMarketing', 'TR',
    'Pazarlama',
    'İster uygulamalar ister web siteleri olsun, oluşturduğumuz projelerde aynı yaklaşımı kullanıyoruz. Oraya, genel deneyim için geliyorsunuz.',
    JSON_OBJECT('mediaUid', 'service-icon-3')
  UNION ALL SELECT 'e1404008-0000-4000-8000-000000000008', 'ServiceItemMarketingEn', 'ServiceItemMarketing', 'EN',
    'Marketing',
    'We take that same approach with the apps & websites we create. you go there because of the overall experience.',
    JSON_OBJECT('mediaUid', 'service-icon-3')

  -- Panels entries
  UNION ALL SELECT 'e1404009-0000-4000-8000-000000000009', 'ServicePanel1Tr', 'ServicePanel1', 'TR',
    'Logos ve markalaşma',
    'Grafik tasarımlarınızı bütçenize ve ihtiyaçlarınıza göre oluşturuyoruz. Marka imajınızı ortaya çıkarır ve kitlenizi yakalarsınız.',
    JSON_OBJECT(
      'mediaUid', 'service-panel-img-1',
      'panelId', 1,
      'panelSubtitle', 'Tasarim Stüdyosu',
      'items', JSON_ARRAY('Logo Tasarimi', 'Grafik Kimliği', 'İş İletişimi', 'Web Tasarimi'),
      'buttonText', 'Detaylari Gör',
      'buttonUrl', '/service-details'
    )
  UNION ALL SELECT 'e1404010-0000-4000-8000-000000000010', 'ServicePanel1En', 'ServicePanel1', 'EN',
    'Logos and branding',
    'We create your graphic designs according to your budget and your needs. Reveal your brand image and capture your audience.',
    JSON_OBJECT(
      'mediaUid', 'service-panel-img-1',
      'panelId', 1,
      'panelSubtitle', 'Design Studio',
      'items', JSON_ARRAY('Logo Design', 'Graphic identity', 'Business communication', 'Web design'),
      'buttonText', 'See Details',
      'buttonUrl', '/service-details'
    )

  UNION ALL SELECT 'e1404011-0000-4000-8000-000000000011', 'ServicePanel2Tr', 'ServicePanel2', 'TR',
    'Web Tasarim',
    'Grafik tasarımlarınızı bütçenize ve ihtiyaçlarınıza göre oluşturuyoruz. Marka imajınızı ortaya çıkarır ve kitlenizi yakalarsınız.',
    JSON_OBJECT(
      'mediaUid', 'service-panel-img-2',
      'panelId', 2,
      'panelSubtitle', 'Tasarim Stüdyosu',
      'items', JSON_ARRAY('Logo Tasarimi', 'Grafik Kimliği', 'İş İletişimi', 'Web Tasarimi'),
      'buttonText', 'Detaylari Gör',
      'buttonUrl', '/service-details'
    )
  UNION ALL SELECT 'e1404012-0000-4000-8000-000000000012', 'ServicePanel2En', 'ServicePanel2', 'EN',
    'Web Design',
    'We create your graphic designs according to your budget and your needs. Reveal your brand image and capture your audience.',
    JSON_OBJECT(
      'mediaUid', 'service-panel-img-2',
      'panelId', 2,
      'panelSubtitle', 'Design Studio',
      'items', JSON_ARRAY('Logo Design', 'Graphic identity', 'Business communication', 'Web design'),
      'buttonText', 'See Details',
      'buttonUrl', '/service-details'
    )

  UNION ALL SELECT 'e1404013-0000-4000-8000-000000000013', 'ServicePanel3Tr', 'ServicePanel3', 'TR',
    'Motion Tasarim',
    'Grafik tasarımlarınızı bütçenize ve ihtiyaçlarınıza göre oluşturuyoruz. Marka imajınızı ortaya çıkarır ve kitlenizi yakalarsınız.',
    JSON_OBJECT(
      'mediaUid', 'service-panel-img-3',
      'panelId', 3,
      'panelSubtitle', 'Tasarim Stüdyosu',
      'items', JSON_ARRAY('Logo Tasarimi', 'Grafik Kimliği', 'İş İletişimi', 'Web Tasarimi'),
      'buttonText', 'Detaylari Gör',
      'buttonUrl', '/service-details'
    )
  UNION ALL SELECT 'e1404014-0000-4000-8000-000000000014', 'ServicePanel3En', 'ServicePanel3', 'EN',
    'Motion-Design',
    'We create your graphic designs according to your budget and your needs. Reveal your brand image and capture your audience.',
    JSON_OBJECT(
      'mediaUid', 'service-panel-img-3',
      'panelId', 3,
      'panelSubtitle', 'Design Studio',
      'items', JSON_ARRAY('Logo Design', 'Graphic identity', 'Business communication', 'Web design'),
      'buttonText', 'See Details',
      'buttonUrl', '/service-details'
    )

  UNION ALL SELECT 'e1404015-0000-4000-8000-000000000015', 'ServicePanel4Tr', 'ServicePanel4', 'TR',
    'Web Analitiği',
    'Grafik tasarımlarınızı bütçenize ve ihtiyaçlarınıza göre oluşturuyoruz. Marka imajınızı ortaya çıkarır ve kitlenizi yakalarsınız.',
    JSON_OBJECT(
      'mediaUid', 'service-panel-img-4',
      'panelId', 4,
      'panelSubtitle', 'Tasarim Stüdyosu',
      'items', JSON_ARRAY('Logo Tasarimi', 'Grafik Kimliği', 'İş İletişimi', 'Web Tasarimi'),
      'buttonText', 'Detaylari Gör',
      'buttonUrl', '/service-details'
    )
  UNION ALL SELECT 'e1404016-0000-4000-8000-000000000016', 'ServicePanel4En', 'ServicePanel4', 'EN',
    'Web Analytics',
    'We create your graphic designs according to your budget and your needs. Reveal your brand image and capture your audience.',
    JSON_OBJECT(
      'mediaUid', 'service-panel-img-4',
      'panelId', 4,
      'panelSubtitle', 'Design Studio',
      'items', JSON_ARRAY('Logo Design', 'Graphic identity', 'Business communication', 'Web design'),
      'buttonText', 'See Details',
      'buttonUrl', '/service-details'
    )

  -- Brand grid entries (texts are repeated)
  UNION ALL SELECT 'e1404017-0000-4000-8000-000000000017', 'ServiceBrand1Tr', 'ServiceBrand1', 'TR',
    'Brand 1', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-1', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))
  UNION ALL SELECT 'e1404018-0000-4000-8000-000000000018', 'ServiceBrand1En', 'ServiceBrand1', 'EN',
    'Brand 1', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-1', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))

  UNION ALL SELECT 'e1404019-0000-4000-8000-000000000019', 'ServiceBrand2Tr', 'ServiceBrand2', 'TR',
    'Brand 2', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-2', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))
  UNION ALL SELECT 'e1404020-0000-4000-8000-000000000020', 'ServiceBrand2En', 'ServiceBrand2', 'EN',
    'Brand 2', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-2', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))

  UNION ALL SELECT 'e1404021-0000-4000-8000-000000000021', 'ServiceBrand3Tr', 'ServiceBrand3', 'TR',
    'Brand 3', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-3', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))
  UNION ALL SELECT 'e1404022-0000-4000-8000-000000000022', 'ServiceBrand3En', 'ServiceBrand3', 'EN',
    'Brand 3', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-3', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))

  UNION ALL SELECT 'e1404023-0000-4000-8000-000000000023', 'ServiceBrand4Tr', 'ServiceBrand4', 'TR',
    'Brand 4', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-4', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))
  UNION ALL SELECT 'e1404024-0000-4000-8000-000000000024', 'ServiceBrand4En', 'ServiceBrand4', 'EN',
    'Brand 4', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-4', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))

  UNION ALL SELECT 'e1404025-0000-4000-8000-000000000025', 'ServiceBrand5Tr', 'ServiceBrand5', 'TR',
    'Brand 5', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-5', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))
  UNION ALL SELECT 'e1404026-0000-4000-8000-000000000026', 'ServiceBrand5En', 'ServiceBrand5', 'EN',
    'Brand 5', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-5', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))

  UNION ALL SELECT 'e1404027-0000-4000-8000-000000000027', 'ServiceBrand6Tr', 'ServiceBrand6', 'TR',
    'Brand 6', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-6', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))
  UNION ALL SELECT 'e1404028-0000-4000-8000-000000000028', 'ServiceBrand6En', 'ServiceBrand6', 'EN',
    'Brand 6', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-6', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))

  UNION ALL SELECT 'e1404029-0000-4000-8000-000000000029', 'ServiceBrand7Tr', 'ServiceBrand7', 'TR',
    'Brand 7', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-7', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))
  UNION ALL SELECT 'e1404030-0000-4000-8000-000000000030', 'ServiceBrand7En', 'ServiceBrand7', 'EN',
    'Brand 7', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-7', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))

  UNION ALL SELECT 'e1404031-0000-4000-8000-000000000031', 'ServiceBrand8Tr', 'ServiceBrand8', 'TR',
    'Brand 8', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-8', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))
  UNION ALL SELECT 'e1404032-0000-4000-8000-000000000032', 'ServiceBrand8En', 'ServiceBrand8', 'EN',
    'Brand 8', NULL,
    JSON_OBJECT('mediaUid', 'service-brand-8', 'texts', JSON_ARRAY('TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com','TopoChico.com'))

  -- Port marquee entries
  UNION ALL SELECT 'e1404033-0000-4000-8000-000000000033', 'ServicePortEntry1Tr', 'ServicePortEntry1', 'TR',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-2', 'altText', 'Port image 1')
  UNION ALL SELECT 'e1404034-0000-4000-8000-000000000034', 'ServicePortEntry1En', 'ServicePortEntry1', 'EN',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-2', 'altText', 'Port image 1')

  UNION ALL SELECT 'e1404035-0000-4000-8000-000000000035', 'ServicePortEntry2Tr', 'ServicePortEntry2', 'TR',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-2', 'altText', 'Port image 2')
  UNION ALL SELECT 'e1404036-0000-4000-8000-000000000036', 'ServicePortEntry2En', 'ServicePortEntry2', 'EN',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-2', 'altText', 'Port image 2')

  UNION ALL SELECT 'e1404037-0000-4000-8000-000000000037', 'ServicePortEntry3Tr', 'ServicePortEntry3', 'TR',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-3', 'altText', 'Port image 3')
  UNION ALL SELECT 'e1404038-0000-4000-8000-000000000038', 'ServicePortEntry3En', 'ServicePortEntry3', 'EN',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-3', 'altText', 'Port image 3')

  UNION ALL SELECT 'e1404039-0000-4000-8000-000000000039', 'ServicePortEntry4Tr', 'ServicePortEntry4', 'TR',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-4', 'altText', 'Port image 4')
  UNION ALL SELECT 'e1404040-0000-4000-8000-000000000040', 'ServicePortEntry4En', 'ServicePortEntry4', 'EN',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-4', 'altText', 'Port image 4')

  UNION ALL SELECT 'e1404041-0000-4000-8000-000000000041', 'ServicePortEntry5Tr', 'ServicePortEntry5', 'TR',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-1', 'altText', 'Port image 5')
  UNION ALL SELECT 'e1404042-0000-4000-8000-000000000042', 'ServicePortEntry5En', 'ServicePortEntry5', 'EN',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-1', 'altText', 'Port image 5')

  UNION ALL SELECT 'e1404043-0000-4000-8000-000000000043', 'ServicePortEntry6Tr', 'ServicePortEntry6', 'TR',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-2', 'altText', 'Port image 6')
  UNION ALL SELECT 'e1404044-0000-4000-8000-000000000044', 'ServicePortEntry6En', 'ServicePortEntry6', 'EN',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-2', 'altText', 'Port image 6')

  UNION ALL SELECT 'e1404045-0000-4000-8000-000000000045', 'ServicePortEntry7Tr', 'ServicePortEntry7', 'TR',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-3', 'altText', 'Port image 7')
  UNION ALL SELECT 'e1404046-0000-4000-8000-000000000046', 'ServicePortEntry7En', 'ServicePortEntry7', 'EN',
    NULL, NULL,
    JSON_OBJECT('mediaUid', 'service-port-3', 'altText', 'Port image 7')

  -- Big text CTA button entry
  UNION ALL SELECT 'e1404047-0000-4000-8000-000000000047', 'ServiceBigTextCtaButtonTr', 'ServiceBigTextCtaButton', 'TR',
    NULL, NULL,
    JSON_OBJECT('buttonText', 'İletişime Geç', 'buttonUrl', '/contact')
  UNION ALL SELECT 'e1404048-0000-4000-8000-000000000048', 'ServiceBigTextCtaButtonEn', 'ServiceBigTextCtaButton', 'EN',
    NULL, NULL,
    JSON_OBJECT('buttonText', 'Get in Touch', 'buttonUrl', '/contact')
) seed
JOIN component_entries e ON e.uid = seed.entry_uid
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  status = VALUES(status),
  custom_data = VALUES(custom_data),
  published_at = VALUES(published_at),
  updated_at = NOW();

-- ============================================================
-- 6. PAGES (ContentPageTemplate, CONTENT type, /service)
-- ============================================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'f0100001-0000-0000-0000-000000000001', 'service',
  (SELECT id FROM page_templates WHERE uid = 'ContentPageTemplate'),
  'PUBLISHED', 'CONTENT', FALSE, 'INDEX_FOLLOW', NULL
ON DUPLICATE KEY UPDATE
  template_id = VALUES(template_id),
  status = VALUES(status),
  page_type = VALUES(page_type),
  is_home = VALUES(is_home),
  robot_tag = VALUES(robot_tag);

-- ============================================================
-- 7. PAGE_I18N
-- ============================================================

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0100001-0000-0001-0000-000000000001', 'service-tr', p.id, 'TR', 'Hizmetler', 'Hizmetler', '/service', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'service'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0100001-0000-0001-0000-000000000002', 'service-en', p.id, 'EN', 'Services', 'Services', '/service', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'service'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

-- ============================================================
-- 8. PAGE_SLOTS (TopContent, BodyContent, SideContent)
-- ============================================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT UUID(), CONCAT(p.uid, '-', ts.slot_name, 'Slot'), p.id, ts.slot_name, ts.position, ts.sort_order, TRUE, FALSE, NOW(), NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid = 'service'
  AND ts.slot_name IN ('TopContent', 'BodyContent', 'SideContent')
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  updated_at = NOW();

-- ============================================================
-- 9. SLOT_COMPONENTS
-- ============================================================

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, seed.sort_order, TRUE, NOW()
FROM (
  SELECT 'service-TopContentSlot' AS slot_uid, 'ServiceHeroComponent' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'service-BodyContentSlot', 'ServiceServicesComponent', 0
  UNION ALL SELECT 'service-BodyContentSlot', 'ServicePanelsComponent', 1
  UNION ALL SELECT 'service-BodyContentSlot', 'ServiceBrandsComponent', 2
  UNION ALL SELECT 'service-BodyContentSlot', 'ServicePortsMarqueeComponent', 3
  UNION ALL SELECT 'service-BodyContentSlot', 'ServiceBigTextCtaComponent', 4
) seed
JOIN page_slots ps ON ps.uid = seed.slot_uid
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible);

