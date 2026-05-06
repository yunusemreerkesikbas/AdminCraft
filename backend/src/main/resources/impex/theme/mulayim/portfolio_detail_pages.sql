-- #CRAFTIVE_IMPEX
-- Mulayim portfolio detail pages seed.
-- Run via Admin UI /{lang}/impex after theme/mulayim/mulayim_foundation.sql, media uploads and theme/mulayim/portfolio_page.sql.
-- Seeds all linked /portfolio/{slug} detail pages using PortfolioDetailPageTemplate.
-- Idempotent: safe to run multiple times.

-- ============================================================
-- 1. COMPONENT TYPES
-- ============================================================

INSERT INTO component_types (uuid, uid, name, category, is_navigation_aware, created_at, updated_at)
VALUES
  (UUID(), 'PortfolioDetailsComponent', 'Portfolio Details Component', 'content', FALSE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  is_navigation_aware = VALUES(is_navigation_aware),
  updated_at = NOW();

-- ============================================================
-- 2. ENTRY FIELD DEFINITIONS
-- ============================================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'PortfolioDetailsComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'altText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'PortfolioDetailsComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================================
-- 3. COMPONENTS
-- ============================================================

INSERT INTO components (uuid, uid, component_type_id, name, display_order, is_visible, style_classes, custom_data, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, ct.id, seed.name, seed.display_order, TRUE, seed.style_classes, seed.custom_data, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT
    'pd610001-0000-4000-8000-000000000001' AS uuid,
    'PortfolioDetailLectusBlock' AS uid,
    'PortfolioDetailsComponent' AS component_type_uid,
    'Portfolio Detail Babil Sanat Block' AS name,
    0 AS display_order,
    'portfolio-detail-block' AS style_classes,
    JSON_OBJECT(
      'projectNumber', '01',
      'client', 'Babil Sanat',
      'projectDate', 'Portfolio',
      'services', JSON_ARRAY('Logo Design', 'Corporate Identity', 'Visual System'),
      'prevUrl', '/portfolio/sen-turistik-logo-tasarimi',
      'prevLabel', 'Şen Turistik Logo Tasarımı',
      'nextUrl', '/portfolio/tufanlar-tohumculuk-logo',
      'nextLabel', 'Tufanlar Tohumculuk Logo'
    ) AS custom_data
  UNION ALL
  SELECT
    'pd610002-0000-4000-8000-000000000002',
    'PortfolioDetailTheStageBlock',
    'PortfolioDetailsComponent',
    'Portfolio Detail Tufanlar Tohumculuk Block',
    0,
    'portfolio-detail-block',
    JSON_OBJECT(
      'projectNumber', '02',
      'client', 'Tufanlar Tohumculuk',
      'projectDate', 'Portfolio',
      'services', JSON_ARRAY('Logo Design', 'Color System', 'Brand Identity'),
      'prevUrl', '/portfolio/babil-sanat-logo-and-kurumsal-kimlik',
      'prevLabel', 'Babil Sanat Logo & Kurumsal Kimlik',
      'nextUrl', '/portfolio/kns-dis-ticaret-danismanlik-logo',
      'nextLabel', 'Kns Dış Ticaret Danışmanlık Logo'
    )
  UNION ALL
  SELECT
    'pd610003-0000-4000-8000-000000000003',
    'PortfolioDetailArtDirectionBlock',
    'PortfolioDetailsComponent',
    'Portfolio Detail Kns Dış Ticaret Block',
    0,
    'portfolio-detail-block',
    JSON_OBJECT(
      'projectNumber', '03',
      'client', 'Kns Dış Ticaret Danışmanlık',
      'projectDate', 'Portfolio',
      'services', JSON_ARRAY('Logo Design', 'Consultancy Brand Identity', 'Typography'),
      'prevUrl', '/portfolio/tufanlar-tohumculuk-logo',
      'prevLabel', 'Tufanlar Tohumculuk Logo',
      'nextUrl', '/portfolio/armin-besi-logo',
      'nextLabel', 'Armin Besi Logo'
    )
  UNION ALL
  SELECT
    'pd610004-0000-4000-8000-000000000004',
    'PortfolioDetailPetitNavireBlock',
    'PortfolioDetailsComponent',
    'Portfolio Detail Armin Besi Block',
    0,
    'portfolio-detail-block',
    JSON_OBJECT(
      'projectNumber', '04',
      'client', 'Armin Besi',
      'projectDate', 'Portfolio',
      'services', JSON_ARRAY('Logo Design', 'Sector Positioning', 'Color System'),
      'prevUrl', '/portfolio/kns-dis-ticaret-danismanlik-logo',
      'prevLabel', 'Kns Dış Ticaret Danışmanlık Logo',
      'nextUrl', '/portfolio/isik-ticaret-askeri-malzeme-logo',
      'nextLabel', 'Işık Ticaret Askeri Malzeme Logo'
    )
  UNION ALL
  SELECT
    'pd610005-0000-4000-8000-000000000005',
    'PortfolioDetailBigDreamBlock',
    'PortfolioDetailsComponent',
    'Portfolio Detail Işık Ticaret Block',
    0,
    'portfolio-detail-block',
    JSON_OBJECT(
      'projectNumber', '05',
      'client', 'Işık Ticaret Askeri Malzeme',
      'projectDate', 'Portfolio',
      'services', JSON_ARRAY('Logo Refresh', 'E-commerce Identity', 'Typography'),
      'prevUrl', '/portfolio/armin-besi-logo',
      'prevLabel', 'Armin Besi Logo',
      'nextUrl', '/portfolio/koktas-i-nsaat-logo-ve-kartvizit',
      'nextLabel', 'Köktaş İnşaat Logo ve Kartvizit'
    )
  UNION ALL
  SELECT
    'pd610006-0000-4000-8000-000000000006',
    'PortfolioDetailTheStageTwoBlock',
    'PortfolioDetailsComponent',
    'Portfolio Detail Köktaş İnşaat Block',
    0,
    'portfolio-detail-block',
    JSON_OBJECT(
      'projectNumber', '06',
      'client', 'Köktaş İnşaat',
      'projectDate', 'Portfolio',
      'services', JSON_ARRAY('Logo Design', 'Business Card', 'Corporate Identity'),
      'prevUrl', '/portfolio/isik-ticaret-askeri-malzeme-logo',
      'prevLabel', 'Işık Ticaret Askeri Malzeme Logo',
      'nextUrl', '/portfolio/hasca-kuruyemis-kahve-logo-tasarimi',
      'nextLabel', 'Hasça Kuruyemiş - Kahve Logo Tasarımı'
    )
  UNION ALL
  SELECT
    'pd610007-0000-4000-8000-000000000007',
    'PortfolioDetailBigDreamTwoBlock',
    'PortfolioDetailsComponent',
    'Portfolio Detail Hasça Kuruyemiş Block',
    0,
    'portfolio-detail-block',
    JSON_OBJECT(
      'projectNumber', '07',
      'client', 'Hasça Kuruyemiş - Kahve',
      'projectDate', 'Portfolio',
      'services', JSON_ARRAY('Logo Design', 'Food Brand Identity', 'Color and Font Selection'),
      'prevUrl', '/portfolio/koktas-i-nsaat-logo-ve-kartvizit',
      'prevLabel', 'Köktaş İnşaat Logo ve Kartvizit',
      'nextUrl', '/portfolio/sen-turistik-logo-tasarimi',
      'nextLabel', 'Şen Turistik Logo Tasarımı'
    )
  UNION ALL
  SELECT
    'pd610008-0000-4000-8000-000000000008',
    'PortfolioDetailBigDreamThreeBlock',
    'PortfolioDetailsComponent',
    'Portfolio Detail Şen Turistik Block',
    0,
    'portfolio-detail-block',
    JSON_OBJECT(
      'projectNumber', '08',
      'client', 'Şen Turistik Hediyelik Eşya ve Züccaciye',
      'projectDate', 'Portfolio',
      'services', JSON_ARRAY('Logo Design', 'Retail Identity', 'Visual Direction'),
      'prevUrl', '/portfolio/hasca-kuruyemis-kahve-logo-tasarimi',
      'prevLabel', 'Hasça Kuruyemiş - Kahve Logo Tasarımı',
      'nextUrl', '/portfolio/babil-sanat-logo-and-kurumsal-kimlik',
      'nextLabel', 'Babil Sanat Logo & Kurumsal Kimlik'
    )
) seed
JOIN component_types ct ON ct.uid = seed.component_type_uid
ON DUPLICATE KEY UPDATE
  component_type_id = VALUES(component_type_id),
  name = VALUES(name),
  display_order = VALUES(display_order),
  is_visible = VALUES(is_visible),
  style_classes = VALUES(style_classes),
  custom_data = VALUES(custom_data),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 4. COMPONENT_I18N
-- ============================================================

INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, c.id, seed.language, seed.title, seed.subtitle, seed.description, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT 'pd620001-0000-4000-8000-000000000001' AS uuid, 'PortfolioDetailLectusBlockTr' AS uid, 'PortfolioDetailLectusBlock' AS component_uid, 'TR' AS language, 'Babil Sanat Logo & Kurumsal Kimlik' AS title, 'Logo Tasarım ve Kurumsal Kimlik' AS subtitle, 'Babil Sanat için hazırlanan logotype ve destekleyici sembol, kültür-sanat etkinliklerinin enerjisini daha bütünlüklü ve akılda kalıcı bir marka kimliğine taşıyor.' AS description
  UNION ALL SELECT 'pd620002-0000-4000-8000-000000000002', 'PortfolioDetailLectusBlockEn', 'PortfolioDetailLectusBlock', 'EN', 'Babil Sanat Logo & Corporate Identity', 'Logo and Corporate Identity', 'The logotype and supporting symbol designed for Babil Sanat turn the energy of cultural events into a cohesive and memorable brand identity.'
  UNION ALL SELECT 'pd620003-0000-4000-8000-000000000003', 'PortfolioDetailTheStageBlockTr', 'PortfolioDetailTheStageBlock', 'TR', 'Tufanlar Tohumculuk Logo', 'Logo Tasarımı', 'Tufanlar Tohumculuk için hazırlanan logo çalışması, tarım sektöründeki kurumsal duruşu güçlendiren renk, font ve form kararları üzerine kuruldu.'
  UNION ALL SELECT 'pd620004-0000-4000-8000-000000000004', 'PortfolioDetailTheStageBlockEn', 'PortfolioDetailTheStageBlock', 'EN', 'Tufanlar Tohumculuk Logo', 'Logo Design', 'The logo design for Tufanlar Tohumculuk builds a stronger agricultural brand presence through considered color, font and form decisions.'
  UNION ALL SELECT 'pd620005-0000-4000-8000-000000000005', 'PortfolioDetailArtDirectionBlockTr', 'PortfolioDetailArtDirectionBlock', 'TR', 'Kns Dış Ticaret Danışmanlık Logo', 'Logo Tasarımı', 'Kns Dış Ticaret Danışmanlık logosu, firmanın global danışmanlık yaklaşımını zincir metaforu, dengeli tipografi ve sektöre uygun renklerle anlatıyor.'
  UNION ALL SELECT 'pd620006-0000-4000-8000-000000000006', 'PortfolioDetailArtDirectionBlockEn', 'PortfolioDetailArtDirectionBlock', 'EN', 'Kns Foreign Trade Consultancy Logo', 'Logo Design', 'The Kns Foreign Trade Consultancy logo expresses a global advisory mindset through a chain metaphor, balanced typography and sector-aware colors.'
  UNION ALL SELECT 'pd620007-0000-4000-8000-000000000007', 'PortfolioDetailPetitNavireBlockTr', 'PortfolioDetailPetitNavireBlock', 'TR', 'Armin Besi Logo', 'Logo Tasarımı', 'Armin Besi için hazırlanan logo, çiftlik ve büyükbaş hayvancılık alanındaki faaliyetleri sade çizgiler, güçlü renkler ve okunaklı bir marka diliyle öne çıkarıyor.'
  UNION ALL SELECT 'pd620008-0000-4000-8000-000000000008', 'PortfolioDetailPetitNavireBlockEn', 'PortfolioDetailPetitNavireBlock', 'EN', 'Armin Besi Logo', 'Logo Design', 'The Armin Besi logo highlights the farm and cattle business with simple lines, strong colors and a clear brand language.'
  UNION ALL SELECT 'pd620009-0000-4000-8000-000000000009', 'PortfolioDetailBigDreamBlockTr', 'PortfolioDetailBigDreamBlock', 'TR', 'Işık Ticaret Askeri Malzeme Logo', 'Logo Revizyonu', 'Işık Ticaret için yapılan logo revizyonu, e-ticarette daha görünür bir kimlik oluşturmak için renk, font ve marka algısını yeniledi.'
  UNION ALL SELECT 'pd620010-0000-4000-8000-000000000010', 'PortfolioDetailBigDreamBlockEn', 'PortfolioDetailBigDreamBlock', 'EN', 'Işık Ticaret Military Equipment Logo', 'Logo Refresh', 'The logo refresh for Işık Ticaret updates color, typography and brand perception to create a stronger identity for e-commerce.'
  UNION ALL SELECT 'pd620011-0000-4000-8000-000000000011', 'PortfolioDetailTheStageTwoBlockTr', 'PortfolioDetailTheStageTwoBlock', 'TR', 'Köktaş İnşaat Logo ve Kartvizit', 'Logo ve Kartvizit Tasarımı', 'Köktaş İnşaat için hazırlanan logo ve kartvizit çalışması, inşaat sektörüne uygun sade, güvenilir ve uygulanabilir bir kurumsal görünüm kuruyor.'
  UNION ALL SELECT 'pd620012-0000-4000-8000-000000000012', 'PortfolioDetailTheStageTwoBlockEn', 'PortfolioDetailTheStageTwoBlock', 'EN', 'Köktaş İnşaat Logo and Business Card', 'Logo and Business Card Design', 'The logo and business card work for Köktaş İnşaat creates a simple, reliable and usable identity for the construction sector.'
  UNION ALL SELECT 'pd620013-0000-4000-8000-000000000013', 'PortfolioDetailBigDreamTwoBlockTr', 'PortfolioDetailBigDreamTwoBlock', 'TR', 'Hasça Kuruyemiş - Kahve Logo Tasarımı', 'Logo Tasarımı', 'Hasça Kuruyemiş - Kahve logosu, markanın yirmi yıllık deneyimini gıda perakendesine uygun renk, font ve yalın bir kimlik düzeniyle görünür kılıyor.'
  UNION ALL SELECT 'pd620014-0000-4000-8000-000000000014', 'PortfolioDetailBigDreamTwoBlockEn', 'PortfolioDetailBigDreamTwoBlock', 'EN', 'Hasça Nuts and Coffee Logo Design', 'Logo Design', 'The Hasça Nuts and Coffee logo makes the brand’s long-standing food retail presence visible through fitting colors, typography and a clean identity system.'
  UNION ALL SELECT 'pd620015-0000-4000-8000-000000000015', 'PortfolioDetailBigDreamThreeBlockTr', 'PortfolioDetailBigDreamThreeBlock', 'TR', 'Şen Turistik Logo Tasarımı', 'Logo Tasarımı', 'Şen Turistik için hazırlanan logo, hediyelik eşya ve züccaciye alanındaki marka hafızasını sade ve kolay uygulanabilir bir görsel kimlikle destekliyor.'
  UNION ALL SELECT 'pd620016-0000-4000-8000-000000000016', 'PortfolioDetailBigDreamThreeBlockEn', 'PortfolioDetailBigDreamThreeBlock', 'EN', 'Şen Turistik Logo Design', 'Logo Design', 'The logo created for Şen Turistik supports the giftware and glassware brand with a simple, memorable and easy-to-apply visual identity.'
) seed
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 5. COMPONENT_ENTRIES
-- ============================================================

INSERT INTO component_entries (uuid, uid, component_id, sort_order, is_visible, style_classes, status, created_at, updated_at)
SELECT seed.uuid, seed.uid, c.id, seed.sort_order, TRUE, NULL, 'PUBLISHED', NOW(), NOW()
FROM (
  SELECT 'pd630001-0000-4000-8000-000000000001' AS uuid, 'PortfolioDetailLectusImage01' AS uid, 'PortfolioDetailLectusBlock' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'pd630002-0000-4000-8000-000000000002', 'PortfolioDetailLectusImage02', 'PortfolioDetailLectusBlock', 1
  UNION ALL SELECT 'pd630003-0000-4000-8000-000000000003', 'PortfolioDetailLectusImage03', 'PortfolioDetailLectusBlock', 2
  UNION ALL SELECT 'pd630004-0000-4000-8000-000000000004', 'PortfolioDetailLectusImage04', 'PortfolioDetailLectusBlock', 3
  UNION ALL SELECT 'pd630005-0000-4000-8000-000000000005', 'PortfolioDetailTheStageImage01', 'PortfolioDetailTheStageBlock', 0
  UNION ALL SELECT 'pd630006-0000-4000-8000-000000000006', 'PortfolioDetailTheStageImage02', 'PortfolioDetailTheStageBlock', 1
  UNION ALL SELECT 'pd630007-0000-4000-8000-000000000007', 'PortfolioDetailTheStageImage03', 'PortfolioDetailTheStageBlock', 2
  UNION ALL SELECT 'pd630008-0000-4000-8000-000000000008', 'PortfolioDetailTheStageImage04', 'PortfolioDetailTheStageBlock', 3
  UNION ALL SELECT 'pd630009-0000-4000-8000-000000000009', 'PortfolioDetailArtDirectionImage01', 'PortfolioDetailArtDirectionBlock', 0
  UNION ALL SELECT 'pd630010-0000-4000-8000-000000000010', 'PortfolioDetailArtDirectionImage02', 'PortfolioDetailArtDirectionBlock', 1
  UNION ALL SELECT 'pd630011-0000-4000-8000-000000000011', 'PortfolioDetailArtDirectionImage03', 'PortfolioDetailArtDirectionBlock', 2
  UNION ALL SELECT 'pd630012-0000-4000-8000-000000000012', 'PortfolioDetailArtDirectionImage04', 'PortfolioDetailArtDirectionBlock', 3
  UNION ALL SELECT 'pd630013-0000-4000-8000-000000000013', 'PortfolioDetailPetitNavireImage01', 'PortfolioDetailPetitNavireBlock', 0
  UNION ALL SELECT 'pd630014-0000-4000-8000-000000000014', 'PortfolioDetailPetitNavireImage02', 'PortfolioDetailPetitNavireBlock', 1
  UNION ALL SELECT 'pd630015-0000-4000-8000-000000000015', 'PortfolioDetailPetitNavireImage03', 'PortfolioDetailPetitNavireBlock', 2
  UNION ALL SELECT 'pd630016-0000-4000-8000-000000000016', 'PortfolioDetailPetitNavireImage04', 'PortfolioDetailPetitNavireBlock', 3
  UNION ALL SELECT 'pd630017-0000-4000-8000-000000000017', 'PortfolioDetailBigDreamImage01', 'PortfolioDetailBigDreamBlock', 0
  UNION ALL SELECT 'pd630018-0000-4000-8000-000000000018', 'PortfolioDetailBigDreamImage02', 'PortfolioDetailBigDreamBlock', 1
  UNION ALL SELECT 'pd630019-0000-4000-8000-000000000019', 'PortfolioDetailBigDreamImage03', 'PortfolioDetailBigDreamBlock', 2
  UNION ALL SELECT 'pd630020-0000-4000-8000-000000000020', 'PortfolioDetailBigDreamImage04', 'PortfolioDetailBigDreamBlock', 3
  UNION ALL SELECT 'pd630021-0000-4000-8000-000000000021', 'PortfolioDetailTheStageTwoImage01', 'PortfolioDetailTheStageTwoBlock', 0
  UNION ALL SELECT 'pd630022-0000-4000-8000-000000000022', 'PortfolioDetailTheStageTwoImage02', 'PortfolioDetailTheStageTwoBlock', 1
  UNION ALL SELECT 'pd630023-0000-4000-8000-000000000023', 'PortfolioDetailTheStageTwoImage03', 'PortfolioDetailTheStageTwoBlock', 2
  UNION ALL SELECT 'pd630024-0000-4000-8000-000000000024', 'PortfolioDetailTheStageTwoImage04', 'PortfolioDetailTheStageTwoBlock', 3
  UNION ALL SELECT 'pd630025-0000-4000-8000-000000000025', 'PortfolioDetailBigDreamTwoImage01', 'PortfolioDetailBigDreamTwoBlock', 0
  UNION ALL SELECT 'pd630026-0000-4000-8000-000000000026', 'PortfolioDetailBigDreamTwoImage02', 'PortfolioDetailBigDreamTwoBlock', 1
  UNION ALL SELECT 'pd630027-0000-4000-8000-000000000027', 'PortfolioDetailBigDreamTwoImage03', 'PortfolioDetailBigDreamTwoBlock', 2
  UNION ALL SELECT 'pd630028-0000-4000-8000-000000000028', 'PortfolioDetailBigDreamTwoImage04', 'PortfolioDetailBigDreamTwoBlock', 3
  UNION ALL SELECT 'pd630029-0000-4000-8000-000000000029', 'PortfolioDetailBigDreamThreeImage01', 'PortfolioDetailBigDreamThreeBlock', 0
  UNION ALL SELECT 'pd630030-0000-4000-8000-000000000030', 'PortfolioDetailBigDreamThreeImage02', 'PortfolioDetailBigDreamThreeBlock', 1
  UNION ALL SELECT 'pd630031-0000-4000-8000-000000000031', 'PortfolioDetailBigDreamThreeImage03', 'PortfolioDetailBigDreamThreeBlock', 2
  UNION ALL SELECT 'pd630032-0000-4000-8000-000000000032', 'PortfolioDetailBigDreamThreeImage04', 'PortfolioDetailBigDreamThreeBlock', 3
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
-- 6. COMPONENT_ENTRY_I18N
-- ============================================================

INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, status, custom_data, published_at, created_at, updated_at)
SELECT seed.uuid, seed.uid, e.id, seed.language, seed.title, NULL, 'PUBLISHED', seed.custom_data, NOW(), NOW(), NOW()
FROM (
  SELECT 'pd640001-0000-4000-8000-000000000001' AS uuid, 'PortfolioDetailLectusImage01Tr' AS uid, 'PortfolioDetailLectusImage01' AS entry_uid, 'TR' AS language, 'Babil Sanat 1' AS title, JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'altText', 'Babil Sanat logo ve kurumsal kimlik galeri görseli 1') AS custom_data
  UNION ALL SELECT 'pd640002-0000-4000-8000-000000000002', 'PortfolioDetailLectusImage01En', 'PortfolioDetailLectusImage01', 'EN', 'Babil Sanat 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'altText', 'Babil Sanat logo and corporate identity gallery image 1')
  UNION ALL SELECT 'pd640003-0000-4000-8000-000000000003', 'PortfolioDetailLectusImage02Tr', 'PortfolioDetailLectusImage02', 'TR', 'Babil Sanat 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'altText', 'Babil Sanat logo ve kurumsal kimlik galeri görseli 2')
  UNION ALL SELECT 'pd640004-0000-4000-8000-000000000004', 'PortfolioDetailLectusImage02En', 'PortfolioDetailLectusImage02', 'EN', 'Babil Sanat 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'altText', 'Babil Sanat logo and corporate identity gallery image 2')
  UNION ALL SELECT 'pd640005-0000-4000-8000-000000000005', 'PortfolioDetailLectusImage03Tr', 'PortfolioDetailLectusImage03', 'TR', 'Babil Sanat 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'altText', 'Babil Sanat logo ve kurumsal kimlik galeri görseli 3')
  UNION ALL SELECT 'pd640006-0000-4000-8000-000000000006', 'PortfolioDetailLectusImage03En', 'PortfolioDetailLectusImage03', 'EN', 'Babil Sanat 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'altText', 'Babil Sanat logo and corporate identity gallery image 3')
  UNION ALL SELECT 'pd640007-0000-4000-8000-000000000007', 'PortfolioDetailLectusImage04Tr', 'PortfolioDetailLectusImage04', 'TR', 'Babil Sanat 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'altText', 'Babil Sanat logo ve kurumsal kimlik galeri görseli 4')
  UNION ALL SELECT 'pd640008-0000-4000-8000-000000000008', 'PortfolioDetailLectusImage04En', 'PortfolioDetailLectusImage04', 'EN', 'Babil Sanat 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'altText', 'Babil Sanat logo and corporate identity gallery image 4')
  UNION ALL SELECT 'pd640009-0000-4000-8000-000000000009', 'PortfolioDetailTheStageImage01Tr', 'PortfolioDetailTheStageImage01', 'TR', 'Tufanlar Tohumculuk 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'altText', 'Tufanlar Tohumculuk logo tasarımı galeri görseli 1')
  UNION ALL SELECT 'pd640010-0000-4000-8000-000000000010', 'PortfolioDetailTheStageImage01En', 'PortfolioDetailTheStageImage01', 'EN', 'Tufanlar Tohumculuk 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'altText', 'Tufanlar Tohumculuk logo design gallery image 1')
  UNION ALL SELECT 'pd640011-0000-4000-8000-000000000011', 'PortfolioDetailTheStageImage02Tr', 'PortfolioDetailTheStageImage02', 'TR', 'Tufanlar Tohumculuk 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'altText', 'Tufanlar Tohumculuk logo tasarımı galeri görseli 2')
  UNION ALL SELECT 'pd640012-0000-4000-8000-000000000012', 'PortfolioDetailTheStageImage02En', 'PortfolioDetailTheStageImage02', 'EN', 'Tufanlar Tohumculuk 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'altText', 'Tufanlar Tohumculuk logo design gallery image 2')
  UNION ALL SELECT 'pd640013-0000-4000-8000-000000000013', 'PortfolioDetailTheStageImage03Tr', 'PortfolioDetailTheStageImage03', 'TR', 'Tufanlar Tohumculuk 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'altText', 'Tufanlar Tohumculuk logo tasarımı galeri görseli 3')
  UNION ALL SELECT 'pd640014-0000-4000-8000-000000000014', 'PortfolioDetailTheStageImage03En', 'PortfolioDetailTheStageImage03', 'EN', 'Tufanlar Tohumculuk 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'altText', 'Tufanlar Tohumculuk logo design gallery image 3')
  UNION ALL SELECT 'pd640015-0000-4000-8000-000000000015', 'PortfolioDetailTheStageImage04Tr', 'PortfolioDetailTheStageImage04', 'TR', 'Tufanlar Tohumculuk 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'altText', 'Tufanlar Tohumculuk logo tasarımı galeri görseli 4')
  UNION ALL SELECT 'pd640016-0000-4000-8000-000000000016', 'PortfolioDetailTheStageImage04En', 'PortfolioDetailTheStageImage04', 'EN', 'Tufanlar Tohumculuk 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'altText', 'Tufanlar Tohumculuk logo design gallery image 4')
  UNION ALL SELECT 'pd640017-0000-4000-8000-000000000017', 'PortfolioDetailArtDirectionImage01Tr', 'PortfolioDetailArtDirectionImage01', 'TR', 'Kns Dış Ticaret 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'altText', 'Kns Dış Ticaret Danışmanlık logo tasarımı galeri görseli 1')
  UNION ALL SELECT 'pd640018-0000-4000-8000-000000000018', 'PortfolioDetailArtDirectionImage01En', 'PortfolioDetailArtDirectionImage01', 'EN', 'Kns Foreign Trade 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'altText', 'Kns Foreign Trade Consultancy logo design gallery image 1')
  UNION ALL SELECT 'pd640019-0000-4000-8000-000000000019', 'PortfolioDetailArtDirectionImage02Tr', 'PortfolioDetailArtDirectionImage02', 'TR', 'Kns Dış Ticaret 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'altText', 'Kns Dış Ticaret Danışmanlık logo tasarımı galeri görseli 2')
  UNION ALL SELECT 'pd640020-0000-4000-8000-000000000020', 'PortfolioDetailArtDirectionImage02En', 'PortfolioDetailArtDirectionImage02', 'EN', 'Kns Foreign Trade 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'altText', 'Kns Foreign Trade Consultancy logo design gallery image 2')
  UNION ALL SELECT 'pd640021-0000-4000-8000-000000000021', 'PortfolioDetailArtDirectionImage03Tr', 'PortfolioDetailArtDirectionImage03', 'TR', 'Kns Dış Ticaret 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'altText', 'Kns Dış Ticaret Danışmanlık logo tasarımı galeri görseli 3')
  UNION ALL SELECT 'pd640022-0000-4000-8000-000000000022', 'PortfolioDetailArtDirectionImage03En', 'PortfolioDetailArtDirectionImage03', 'EN', 'Kns Foreign Trade 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'altText', 'Kns Foreign Trade Consultancy logo design gallery image 3')
  UNION ALL SELECT 'pd640023-0000-4000-8000-000000000023', 'PortfolioDetailArtDirectionImage04Tr', 'PortfolioDetailArtDirectionImage04', 'TR', 'Kns Dış Ticaret 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'altText', 'Kns Dış Ticaret Danışmanlık logo tasarımı galeri görseli 4')
  UNION ALL SELECT 'pd640024-0000-4000-8000-000000000024', 'PortfolioDetailArtDirectionImage04En', 'PortfolioDetailArtDirectionImage04', 'EN', 'Kns Foreign Trade 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'altText', 'Kns Foreign Trade Consultancy logo design gallery image 4')
  UNION ALL SELECT 'pd640025-0000-4000-8000-000000000025', 'PortfolioDetailPetitNavireImage01Tr', 'PortfolioDetailPetitNavireImage01', 'TR', 'Armin Besi 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'altText', 'Armin Besi logo tasarımı galeri görseli 1')
  UNION ALL SELECT 'pd640026-0000-4000-8000-000000000026', 'PortfolioDetailPetitNavireImage01En', 'PortfolioDetailPetitNavireImage01', 'EN', 'Armin Besi 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-04', 'altText', 'Armin Besi logo design gallery image 1')
  UNION ALL SELECT 'pd640027-0000-4000-8000-000000000027', 'PortfolioDetailPetitNavireImage02Tr', 'PortfolioDetailPetitNavireImage02', 'TR', 'Armin Besi 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'altText', 'Armin Besi logo tasarımı galeri görseli 2')
  UNION ALL SELECT 'pd640028-0000-4000-8000-000000000028', 'PortfolioDetailPetitNavireImage02En', 'PortfolioDetailPetitNavireImage02', 'EN', 'Armin Besi 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'altText', 'Armin Besi logo design gallery image 2')
  UNION ALL SELECT 'pd640029-0000-4000-8000-000000000029', 'PortfolioDetailPetitNavireImage03Tr', 'PortfolioDetailPetitNavireImage03', 'TR', 'Armin Besi 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'altText', 'Armin Besi logo tasarımı galeri görseli 3')
  UNION ALL SELECT 'pd640030-0000-4000-8000-000000000030', 'PortfolioDetailPetitNavireImage03En', 'PortfolioDetailPetitNavireImage03', 'EN', 'Armin Besi 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'altText', 'Armin Besi logo design gallery image 3')
  UNION ALL SELECT 'pd640031-0000-4000-8000-000000000031', 'PortfolioDetailPetitNavireImage04Tr', 'PortfolioDetailPetitNavireImage04', 'TR', 'Armin Besi 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'altText', 'Armin Besi logo tasarımı galeri görseli 4')
  UNION ALL SELECT 'pd640032-0000-4000-8000-000000000032', 'PortfolioDetailPetitNavireImage04En', 'PortfolioDetailPetitNavireImage04', 'EN', 'Armin Besi 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'altText', 'Armin Besi logo design gallery image 4')
  UNION ALL SELECT 'pd640033-0000-4000-8000-000000000033', 'PortfolioDetailBigDreamImage01Tr', 'PortfolioDetailBigDreamImage01', 'TR', 'Işık Ticaret 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'altText', 'Işık Ticaret Askeri Malzeme logo revizyonu galeri görseli 1')
  UNION ALL SELECT 'pd640034-0000-4000-8000-000000000034', 'PortfolioDetailBigDreamImage01En', 'PortfolioDetailBigDreamImage01', 'EN', 'Işık Ticaret 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-05', 'altText', 'Işık Ticaret Military Equipment logo refresh gallery image 1')
  UNION ALL SELECT 'pd640035-0000-4000-8000-000000000035', 'PortfolioDetailBigDreamImage02Tr', 'PortfolioDetailBigDreamImage02', 'TR', 'Işık Ticaret 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'altText', 'Işık Ticaret Askeri Malzeme logo revizyonu galeri görseli 2')
  UNION ALL SELECT 'pd640036-0000-4000-8000-000000000036', 'PortfolioDetailBigDreamImage02En', 'PortfolioDetailBigDreamImage02', 'EN', 'Işık Ticaret 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'altText', 'Işık Ticaret Military Equipment logo refresh gallery image 2')
  UNION ALL SELECT 'pd640037-0000-4000-8000-000000000037', 'PortfolioDetailBigDreamImage03Tr', 'PortfolioDetailBigDreamImage03', 'TR', 'Işık Ticaret 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'altText', 'Işık Ticaret Askeri Malzeme logo revizyonu galeri görseli 3')
  UNION ALL SELECT 'pd640038-0000-4000-8000-000000000038', 'PortfolioDetailBigDreamImage03En', 'PortfolioDetailBigDreamImage03', 'EN', 'Işık Ticaret 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'altText', 'Işık Ticaret Military Equipment logo refresh gallery image 3')
  UNION ALL SELECT 'pd640039-0000-4000-8000-000000000039', 'PortfolioDetailBigDreamImage04Tr', 'PortfolioDetailBigDreamImage04', 'TR', 'Işık Ticaret 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'altText', 'Işık Ticaret Askeri Malzeme logo revizyonu galeri görseli 4')
  UNION ALL SELECT 'pd640040-0000-4000-8000-000000000040', 'PortfolioDetailBigDreamImage04En', 'PortfolioDetailBigDreamImage04', 'EN', 'Işık Ticaret 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'altText', 'Işık Ticaret Military Equipment logo refresh gallery image 4')
  UNION ALL SELECT 'pd640041-0000-4000-8000-000000000041', 'PortfolioDetailTheStageTwoImage01Tr', 'PortfolioDetailTheStageTwoImage01', 'TR', 'Köktaş İnşaat 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'altText', 'Köktaş İnşaat logo ve kartvizit galeri görseli 1')
  UNION ALL SELECT 'pd640042-0000-4000-8000-000000000042', 'PortfolioDetailTheStageTwoImage01En', 'PortfolioDetailTheStageTwoImage01', 'EN', 'Köktaş İnşaat 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-06', 'altText', 'Köktaş İnşaat logo and business card gallery image 1')
  UNION ALL SELECT 'pd640043-0000-4000-8000-000000000043', 'PortfolioDetailTheStageTwoImage02Tr', 'PortfolioDetailTheStageTwoImage02', 'TR', 'Köktaş İnşaat 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'altText', 'Köktaş İnşaat logo ve kartvizit galeri görseli 2')
  UNION ALL SELECT 'pd640044-0000-4000-8000-000000000044', 'PortfolioDetailTheStageTwoImage02En', 'PortfolioDetailTheStageTwoImage02', 'EN', 'Köktaş İnşaat 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'altText', 'Köktaş İnşaat logo and business card gallery image 2')
  UNION ALL SELECT 'pd640045-0000-4000-8000-000000000045', 'PortfolioDetailTheStageTwoImage03Tr', 'PortfolioDetailTheStageTwoImage03', 'TR', 'Köktaş İnşaat 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'altText', 'Köktaş İnşaat logo ve kartvizit galeri görseli 3')
  UNION ALL SELECT 'pd640046-0000-4000-8000-000000000046', 'PortfolioDetailTheStageTwoImage03En', 'PortfolioDetailTheStageTwoImage03', 'EN', 'Köktaş İnşaat 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'altText', 'Köktaş İnşaat logo and business card gallery image 3')
  UNION ALL SELECT 'pd640047-0000-4000-8000-000000000047', 'PortfolioDetailTheStageTwoImage04Tr', 'PortfolioDetailTheStageTwoImage04', 'TR', 'Köktaş İnşaat 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'altText', 'Köktaş İnşaat logo ve kartvizit galeri görseli 4')
  UNION ALL SELECT 'pd640048-0000-4000-8000-000000000048', 'PortfolioDetailTheStageTwoImage04En', 'PortfolioDetailTheStageTwoImage04', 'EN', 'Köktaş İnşaat 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'altText', 'Köktaş İnşaat logo and business card gallery image 4')
  UNION ALL SELECT 'pd640049-0000-4000-8000-000000000049', 'PortfolioDetailBigDreamTwoImage01Tr', 'PortfolioDetailBigDreamTwoImage01', 'TR', 'Hasça Kuruyemiş 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'altText', 'Hasça Kuruyemiş Kahve logo tasarımı galeri görseli 1')
  UNION ALL SELECT 'pd640050-0000-4000-8000-000000000050', 'PortfolioDetailBigDreamTwoImage01En', 'PortfolioDetailBigDreamTwoImage01', 'EN', 'Hasça Nuts and Coffee 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-07', 'altText', 'Hasça Nuts and Coffee logo design gallery image 1')
  UNION ALL SELECT 'pd640051-0000-4000-8000-000000000051', 'PortfolioDetailBigDreamTwoImage02Tr', 'PortfolioDetailBigDreamTwoImage02', 'TR', 'Hasça Kuruyemiş 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'altText', 'Hasça Kuruyemiş Kahve logo tasarımı galeri görseli 2')
  UNION ALL SELECT 'pd640052-0000-4000-8000-000000000052', 'PortfolioDetailBigDreamTwoImage02En', 'PortfolioDetailBigDreamTwoImage02', 'EN', 'Hasça Nuts and Coffee 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'altText', 'Hasça Nuts and Coffee logo design gallery image 2')
  UNION ALL SELECT 'pd640053-0000-4000-8000-000000000053', 'PortfolioDetailBigDreamTwoImage03Tr', 'PortfolioDetailBigDreamTwoImage03', 'TR', 'Hasça Kuruyemiş 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'altText', 'Hasça Kuruyemiş Kahve logo tasarımı galeri görseli 3')
  UNION ALL SELECT 'pd640054-0000-4000-8000-000000000054', 'PortfolioDetailBigDreamTwoImage03En', 'PortfolioDetailBigDreamTwoImage03', 'EN', 'Hasça Nuts and Coffee 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'altText', 'Hasça Nuts and Coffee logo design gallery image 3')
  UNION ALL SELECT 'pd640055-0000-4000-8000-000000000055', 'PortfolioDetailBigDreamTwoImage04Tr', 'PortfolioDetailBigDreamTwoImage04', 'TR', 'Hasça Kuruyemiş 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'altText', 'Hasça Kuruyemiş Kahve logo tasarımı galeri görseli 4')
  UNION ALL SELECT 'pd640056-0000-4000-8000-000000000056', 'PortfolioDetailBigDreamTwoImage04En', 'PortfolioDetailBigDreamTwoImage04', 'EN', 'Hasça Nuts and Coffee 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'altText', 'Hasça Nuts and Coffee logo design gallery image 4')
  UNION ALL SELECT 'pd640057-0000-4000-8000-000000000057', 'PortfolioDetailBigDreamThreeImage01Tr', 'PortfolioDetailBigDreamThreeImage01', 'TR', 'Şen Turistik 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'altText', 'Şen Turistik logo tasarımı galeri görseli 1')
  UNION ALL SELECT 'pd640058-0000-4000-8000-000000000058', 'PortfolioDetailBigDreamThreeImage01En', 'PortfolioDetailBigDreamThreeImage01', 'EN', 'Şen Turistik 1', JSON_OBJECT('mediaUid', 'portfolio-grid-image-08', 'altText', 'Şen Turistik logo design gallery image 1')
  UNION ALL SELECT 'pd640059-0000-4000-8000-000000000059', 'PortfolioDetailBigDreamThreeImage02Tr', 'PortfolioDetailBigDreamThreeImage02', 'TR', 'Şen Turistik 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'altText', 'Şen Turistik logo tasarımı galeri görseli 2')
  UNION ALL SELECT 'pd640060-0000-4000-8000-000000000060', 'PortfolioDetailBigDreamThreeImage02En', 'PortfolioDetailBigDreamThreeImage02', 'EN', 'Şen Turistik 2', JSON_OBJECT('mediaUid', 'portfolio-grid-image-01', 'altText', 'Şen Turistik logo design gallery image 2')
  UNION ALL SELECT 'pd640061-0000-4000-8000-000000000061', 'PortfolioDetailBigDreamThreeImage03Tr', 'PortfolioDetailBigDreamThreeImage03', 'TR', 'Şen Turistik 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'altText', 'Şen Turistik logo tasarımı galeri görseli 3')
  UNION ALL SELECT 'pd640062-0000-4000-8000-000000000062', 'PortfolioDetailBigDreamThreeImage03En', 'PortfolioDetailBigDreamThreeImage03', 'EN', 'Şen Turistik 3', JSON_OBJECT('mediaUid', 'portfolio-grid-image-02', 'altText', 'Şen Turistik logo design gallery image 3')
  UNION ALL SELECT 'pd640063-0000-4000-8000-000000000063', 'PortfolioDetailBigDreamThreeImage04Tr', 'PortfolioDetailBigDreamThreeImage04', 'TR', 'Şen Turistik 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'altText', 'Şen Turistik logo tasarımı galeri görseli 4')
  UNION ALL SELECT 'pd640064-0000-4000-8000-000000000064', 'PortfolioDetailBigDreamThreeImage04En', 'PortfolioDetailBigDreamThreeImage04', 'EN', 'Şen Turistik 4', JSON_OBJECT('mediaUid', 'portfolio-grid-image-03', 'altText', 'Şen Turistik logo design gallery image 4')
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
-- 7. DETAIL PAGES
-- ============================================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT seed.uuid, seed.uid, pt.id, 'PUBLISHED', 'CONTENT', FALSE, 'INDEX_FOLLOW', NULL
FROM (
  SELECT 'pd650001-0000-4000-8000-000000000001' AS uuid, 'portfolio-detail-lectus' AS uid
  UNION ALL SELECT 'pd650002-0000-4000-8000-000000000002', 'portfolio-detail-the-stage'
  UNION ALL SELECT 'pd650003-0000-4000-8000-000000000003', 'portfolio-detail-art-direction'
  UNION ALL SELECT 'pd650004-0000-4000-8000-000000000004', 'portfolio-detail-petit-navire'
  UNION ALL SELECT 'pd650005-0000-4000-8000-000000000005', 'portfolio-detail-big-dream'
  UNION ALL SELECT 'pd650006-0000-4000-8000-000000000006', 'portfolio-detail-the-stage-2'
  UNION ALL SELECT 'pd650007-0000-4000-8000-000000000007', 'portfolio-detail-big-dream-2'
  UNION ALL SELECT 'pd650008-0000-4000-8000-000000000008', 'portfolio-detail-big-dream-3'
) seed
JOIN page_templates pt ON pt.uid = 'PortfolioDetailPageTemplate'
ON DUPLICATE KEY UPDATE
  template_id = VALUES(template_id),
  status = VALUES(status),
  page_type = VALUES(page_type),
  is_home = VALUES(is_home),
  robot_tag = VALUES(robot_tag);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, description, canonical_url, status)
SELECT seed.uuid, seed.uid, p.id, seed.language, seed.name, seed.title, seed.description, seed.canonical_url, 'PUBLISHED'
FROM (
  SELECT 'pd660001-0000-4000-8000-000000000001' AS uuid, 'portfolio-detail-lectus-tr' AS uid, 'portfolio-detail-lectus' AS page_uid, 'TR' AS language, 'Babil Sanat Logo & Kurumsal Kimlik' AS name, 'Babil Sanat Logo & Kurumsal Kimlik' AS title, 'Babil Sanat için hazırlanan logotype ve destekleyici sembol, kültür-sanat etkinliklerinin enerjisini daha bütünlüklü ve akılda kalıcı bir marka kimliğine taşıyor.' AS description, '/portfolio/babil-sanat-logo-and-kurumsal-kimlik' AS canonical_url
  UNION ALL SELECT 'pd660002-0000-4000-8000-000000000002', 'portfolio-detail-lectus-en', 'portfolio-detail-lectus', 'EN', 'Babil Sanat Logo & Corporate Identity', 'Babil Sanat Logo & Corporate Identity', 'The logotype and supporting symbol designed for Babil Sanat turn the energy of cultural events into a cohesive and memorable brand identity.', '/portfolio/babil-sanat-logo-and-kurumsal-kimlik'
  UNION ALL SELECT 'pd660003-0000-4000-8000-000000000003', 'portfolio-detail-the-stage-tr', 'portfolio-detail-the-stage', 'TR', 'Tufanlar Tohumculuk Logo', 'Tufanlar Tohumculuk Logo', 'Tufanlar Tohumculuk için hazırlanan logo çalışması, tarım sektöründeki kurumsal duruşu güçlendiren renk, font ve form kararları üzerine kuruldu.', '/portfolio/tufanlar-tohumculuk-logo'
  UNION ALL SELECT 'pd660004-0000-4000-8000-000000000004', 'portfolio-detail-the-stage-en', 'portfolio-detail-the-stage', 'EN', 'Tufanlar Tohumculuk Logo', 'Tufanlar Tohumculuk Logo', 'The logo design for Tufanlar Tohumculuk builds a stronger agricultural brand presence through considered color, font and form decisions.', '/portfolio/tufanlar-tohumculuk-logo'
  UNION ALL SELECT 'pd660005-0000-4000-8000-000000000005', 'portfolio-detail-art-direction-tr', 'portfolio-detail-art-direction', 'TR', 'Kns Dış Ticaret Danışmanlık Logo', 'Kns Dış Ticaret Danışmanlık Logo', 'Kns Dış Ticaret Danışmanlık logosu, firmanın global danışmanlık yaklaşımını zincir metaforu, dengeli tipografi ve sektöre uygun renklerle anlatıyor.', '/portfolio/kns-dis-ticaret-danismanlik-logo'
  UNION ALL SELECT 'pd660006-0000-4000-8000-000000000006', 'portfolio-detail-art-direction-en', 'portfolio-detail-art-direction', 'EN', 'Kns Foreign Trade Consultancy Logo', 'Kns Foreign Trade Consultancy Logo', 'The Kns Foreign Trade Consultancy logo expresses a global advisory mindset through a chain metaphor, balanced typography and sector-aware colors.', '/portfolio/kns-dis-ticaret-danismanlik-logo'
  UNION ALL SELECT 'pd660007-0000-4000-8000-000000000007', 'portfolio-detail-petit-navire-tr', 'portfolio-detail-petit-navire', 'TR', 'Armin Besi Logo', 'Armin Besi Logo', 'Armin Besi için hazırlanan logo, çiftlik ve büyükbaş hayvancılık alanındaki faaliyetleri sade çizgiler, güçlü renkler ve okunaklı bir marka diliyle öne çıkarıyor.', '/portfolio/armin-besi-logo'
  UNION ALL SELECT 'pd660008-0000-4000-8000-000000000008', 'portfolio-detail-petit-navire-en', 'portfolio-detail-petit-navire', 'EN', 'Armin Besi Logo', 'Armin Besi Logo', 'The Armin Besi logo highlights the farm and cattle business with simple lines, strong colors and a clear brand language.', '/portfolio/armin-besi-logo'
  UNION ALL SELECT 'pd660009-0000-4000-8000-000000000009', 'portfolio-detail-big-dream-tr', 'portfolio-detail-big-dream', 'TR', 'Işık Ticaret Askeri Malzeme Logo', 'Işık Ticaret Askeri Malzeme Logo', 'Işık Ticaret için yapılan logo revizyonu, e-ticarette daha görünür bir kimlik oluşturmak için renk, font ve marka algısını yeniledi.', '/portfolio/isik-ticaret-askeri-malzeme-logo'
  UNION ALL SELECT 'pd660010-0000-4000-8000-000000000010', 'portfolio-detail-big-dream-en', 'portfolio-detail-big-dream', 'EN', 'Işık Ticaret Military Equipment Logo', 'Işık Ticaret Military Equipment Logo', 'The logo refresh for Işık Ticaret updates color, typography and brand perception to create a stronger identity for e-commerce.', '/portfolio/isik-ticaret-askeri-malzeme-logo'
  UNION ALL SELECT 'pd660011-0000-4000-8000-000000000011', 'portfolio-detail-the-stage-2-tr', 'portfolio-detail-the-stage-2', 'TR', 'Köktaş İnşaat Logo ve Kartvizit', 'Köktaş İnşaat Logo ve Kartvizit', 'Köktaş İnşaat için hazırlanan logo ve kartvizit çalışması, inşaat sektörüne uygun sade, güvenilir ve uygulanabilir bir kurumsal görünüm kuruyor.', '/portfolio/koktas-i-nsaat-logo-ve-kartvizit'
  UNION ALL SELECT 'pd660012-0000-4000-8000-000000000012', 'portfolio-detail-the-stage-2-en', 'portfolio-detail-the-stage-2', 'EN', 'Köktaş İnşaat Logo and Business Card', 'Köktaş İnşaat Logo and Business Card', 'The logo and business card work for Köktaş İnşaat creates a simple, reliable and usable identity for the construction sector.', '/portfolio/koktas-i-nsaat-logo-ve-kartvizit'
  UNION ALL SELECT 'pd660013-0000-4000-8000-000000000013', 'portfolio-detail-big-dream-2-tr', 'portfolio-detail-big-dream-2', 'TR', 'Hasça Kuruyemiş - Kahve Logo Tasarımı', 'Hasça Kuruyemiş - Kahve Logo Tasarımı', 'Hasça Kuruyemiş - Kahve logosu, markanın yirmi yıllık deneyimini gıda perakendesine uygun renk, font ve yalın bir kimlik düzeniyle görünür kılıyor.', '/portfolio/hasca-kuruyemis-kahve-logo-tasarimi'
  UNION ALL SELECT 'pd660014-0000-4000-8000-000000000014', 'portfolio-detail-big-dream-2-en', 'portfolio-detail-big-dream-2', 'EN', 'Hasça Nuts and Coffee Logo Design', 'Hasça Nuts and Coffee Logo Design', 'The Hasça Nuts and Coffee logo makes the brand''s long-standing food retail presence visible through fitting colors, typography and a clean identity system.', '/portfolio/hasca-kuruyemis-kahve-logo-tasarimi'
  UNION ALL SELECT 'pd660015-0000-4000-8000-000000000015', 'portfolio-detail-big-dream-3-tr', 'portfolio-detail-big-dream-3', 'TR', 'Şen Turistik Logo Tasarımı', 'Şen Turistik Logo Tasarımı', 'Şen Turistik için hazırlanan logo, hediyelik eşya ve züccaciye alanındaki marka hafızasını sade ve kolay uygulanabilir bir görsel kimlikle destekliyor.', '/portfolio/sen-turistik-logo-tasarimi'
  UNION ALL SELECT 'pd660016-0000-4000-8000-000000000016', 'portfolio-detail-big-dream-3-en', 'portfolio-detail-big-dream-3', 'EN', 'Şen Turistik Logo Design', 'Şen Turistik Logo Design', 'The logo created for Şen Turistik supports the giftware and glassware brand with a simple, memorable and easy-to-apply visual identity.', '/portfolio/sen-turistik-logo-tasarimi'
) seed
JOIN pages p ON p.uid = seed.page_uid
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  description = VALUES(description),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

-- ============================================================
-- 8. PAGE SLOTS
-- ============================================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT
  UUID(),
  CONCAT(p.uid, '-', ts.slot_name, 'Slot'),
  p.id,
  ts.slot_name,
  ts.position,
  ts.sort_order,
  TRUE,
  FALSE,
  NOW(),
  NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid IN (
  'portfolio-detail-lectus',
  'portfolio-detail-the-stage',
  'portfolio-detail-art-direction',
  'portfolio-detail-petit-navire',
  'portfolio-detail-big-dream',
  'portfolio-detail-the-stage-2',
  'portfolio-detail-big-dream-2',
  'portfolio-detail-big-dream-3'
)
  AND ts.slot_name = 'MainContent'
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  is_active = VALUES(is_active),
  is_shared = VALUES(is_shared),
  updated_at = NOW();

-- ============================================================
-- 9. SLOT COMPONENTS
-- ============================================================

UPDATE slot_components sc
JOIN page_slots ps ON ps.id = sc.slot_id
JOIN components c ON c.id = sc.component_id
SET sc.is_visible = FALSE
WHERE ps.uid IN (
  'portfolio-detail-lectus-MainContentSlot',
  'portfolio-detail-the-stage-MainContentSlot',
  'portfolio-detail-art-direction-MainContentSlot',
  'portfolio-detail-petit-navire-MainContentSlot',
  'portfolio-detail-big-dream-MainContentSlot',
  'portfolio-detail-the-stage-2-MainContentSlot',
  'portfolio-detail-big-dream-2-MainContentSlot',
  'portfolio-detail-big-dream-3-MainContentSlot'
)
  AND c.uid = 'PortfolioDetailBrandStrip';

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, seed.sort_order, TRUE, NOW()
FROM (
  SELECT 'portfolio-detail-lectus-MainContentSlot' AS slot_uid, 'PortfolioDetailLectusBlock' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'portfolio-detail-lectus-MainContentSlot', 'PortfolioPageBrandStrip', 1
  UNION ALL SELECT 'portfolio-detail-the-stage-MainContentSlot', 'PortfolioDetailTheStageBlock', 0
  UNION ALL SELECT 'portfolio-detail-the-stage-MainContentSlot', 'PortfolioPageBrandStrip', 1
  UNION ALL SELECT 'portfolio-detail-art-direction-MainContentSlot', 'PortfolioDetailArtDirectionBlock', 0
  UNION ALL SELECT 'portfolio-detail-art-direction-MainContentSlot', 'PortfolioPageBrandStrip', 1
  UNION ALL SELECT 'portfolio-detail-petit-navire-MainContentSlot', 'PortfolioDetailPetitNavireBlock', 0
  UNION ALL SELECT 'portfolio-detail-petit-navire-MainContentSlot', 'PortfolioPageBrandStrip', 1
  UNION ALL SELECT 'portfolio-detail-big-dream-MainContentSlot', 'PortfolioDetailBigDreamBlock', 0
  UNION ALL SELECT 'portfolio-detail-big-dream-MainContentSlot', 'PortfolioPageBrandStrip', 1
  UNION ALL SELECT 'portfolio-detail-the-stage-2-MainContentSlot', 'PortfolioDetailTheStageTwoBlock', 0
  UNION ALL SELECT 'portfolio-detail-the-stage-2-MainContentSlot', 'PortfolioPageBrandStrip', 1
  UNION ALL SELECT 'portfolio-detail-big-dream-2-MainContentSlot', 'PortfolioDetailBigDreamTwoBlock', 0
  UNION ALL SELECT 'portfolio-detail-big-dream-2-MainContentSlot', 'PortfolioPageBrandStrip', 1
  UNION ALL SELECT 'portfolio-detail-big-dream-3-MainContentSlot', 'PortfolioDetailBigDreamThreeBlock', 0
  UNION ALL SELECT 'portfolio-detail-big-dream-3-MainContentSlot', 'PortfolioPageBrandStrip', 1
) seed
JOIN page_slots ps ON ps.uid = seed.slot_uid
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible);

-- ============================================================
-- 10. REQUIRED MEDIA
-- ============================================================
-- Required media UIDs:
--   portfolio-grid-image-01
--   portfolio-grid-image-02
--   portfolio-grid-image-03
--   portfolio-grid-image-04
--   portfolio-grid-image-05
--   portfolio-grid-image-06
--   portfolio-grid-image-07
--   portfolio-grid-image-08
