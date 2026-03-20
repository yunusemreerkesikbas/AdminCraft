-- #ADMINCRAFT_IMPEX
-- About Content Page — dedicated CMS types for ContentPageTemplate.
-- Run via Admin UI /{lang}/impex after component library repeatable seeds are applied.
-- Idempotent: safe to run multiple times.
-- Reference: Liko about-us (AboutUsHero, AboutUsArea, TeamOne, FunFactOne, BrandFive, AwardOne).

-- ============================================================
-- 1. COMPONENTS
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
  SELECT 'b1100001-0000-4000-8000-000000000001' AS uuid, 'AboutHeroComponent' AS uid, 'ContentHeroComponent' AS component_type_uid, 'About Hero Banner' AS name, 0 AS display_order, 'about-hero' AS style_classes
  UNION ALL SELECT 'b1100002-0000-4000-8000-000000000002', 'AboutStoryComponent', 'SplitMediaIntroComponent', 'About Story Section', 0, 'about-story'
  UNION ALL SELECT 'b1100003-0000-4000-8000-000000000003', 'AboutTeamSection', 'PeopleCarouselComponent', 'About Team Section', 0, 'about-team'
  UNION ALL SELECT 'b1100004-0000-4000-8000-000000000004', 'AboutStatsSection', 'StatsGridComponent', 'About Stats Section', 0, 'about-stats'
  UNION ALL SELECT 'b1100005-0000-4000-8000-000000000005', 'AboutBrandsStrip', 'LogoMarqueeComponent', 'About Brands Strip', 0, 'about-brands'
  UNION ALL SELECT 'b1100006-0000-4000-8000-000000000006', 'AboutAwardsBlock', 'AwardsShowcaseComponent', 'About Awards Block', 0, 'about-awards'
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
-- 2. COMPONENT_I18N (TR + EN)
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
  SELECT 'b1200001-0000-4000-8000-000000000001' AS uuid, 'AboutHeroComponentTr' AS uid, 'AboutHeroComponent' AS component_uid, 'TR' AS language, 'Dijital Varlık İnşası' AS title, 'Dijital yaratıcı ajans' AS subtitle, 'Maksimum duygusal etkiyle dijital deneyimler' AS description
  UNION ALL SELECT 'b1200002-0000-4000-8000-000000000002', 'AboutHeroComponentEn', 'AboutHeroComponent', 'EN', 'Building Digital Presence', 'Digital creative agency', 'Digital experiences with maximum emotional impact'
  UNION ALL SELECT 'b1200003-0000-4000-8000-000000000003', 'AboutStoryComponentTr', 'AboutStoryComponent', 'TR', 'Bir Şey', 'Ne Yapıyorum', 'İşletmeler ve bireyler için yüksek kaliteli tasarım ve markalaşma çözümleri sunan yaratıcı bir stüdyoyuz.

Ekibimiz, unutulmaz dijital markaları ve özenle hazırlanmış ürün deneyimlerini şekillendiren yetenekli tasarımcılar, geliştiriciler ve pazarlamacılardan oluşur.'
  UNION ALL SELECT 'b1200004-0000-4000-8000-000000000004', 'AboutStoryComponentEn', 'AboutStoryComponent', 'EN', 'Something', 'What I Do', 'We are a creative studio that specializes in providing high-quality design and branding solutions to businesses and individuals.

Our team is composed of talented designers, developers, and marketers who shape memorable digital brands and polished product experiences.'
  UNION ALL SELECT 'b1200005-0000-4000-8000-000000000005', 'AboutTeamSectionTr', 'AboutTeamSection', 'TR', 'Her lansmanın arkasındaki ekip', 'Ekibimiz', NULL
  UNION ALL SELECT 'b1200006-0000-4000-8000-000000000006', 'AboutTeamSectionEn', 'AboutTeamSection', 'EN', 'Meet the people behind every launch', 'Our team', NULL
  UNION ALL SELECT 'b1200007-0000-4000-8000-000000000007', 'AboutStatsSectionTr', 'AboutStatsSection', 'TR', 'Ajansın anlık görüntüleri', 'İlginç bilgiler', NULL
  UNION ALL SELECT 'b1200008-0000-4000-8000-000000000008', 'AboutStatsSectionEn', 'AboutStatsSection', 'EN', 'Agency snapshots', 'Fun facts', NULL
  UNION ALL SELECT 'b1200009-0000-4000-8000-000000000009', 'AboutBrandsStripTr', 'AboutBrandsStrip', 'TR', 'Müşterilerimiz', 'Güvenilen', 'Dürüstlük ve gerçek bağa dayalı ortaklıklar kurmaya inanıyoruz. Bu yüzden en büyük şirketlerden bazıları yıllardır bizimle.'
  UNION ALL SELECT 'b1200010-0000-4000-8000-000000000010', 'AboutBrandsStripEn', 'AboutBrandsStrip', 'EN', 'Our clients', 'Trusted by', 'We believe in creating partnerships based on honesty and true connection. That is why some of the biggest companies stayed with us for years.'
  UNION ALL SELECT 'b1200011-0000-4000-8000-000000000011', 'AboutAwardsBlockTr', 'AboutAwardsBlock', 'TR', 'Ödüller ve Takdirler', 'Ödüllerimiz', 'Tasarım, hikayeleştirme ve dijital ustalık kapsamında biriken takdirler.'
  UNION ALL SELECT 'b1200012-0000-4000-8000-000000000012', 'AboutAwardsBlockEn', 'AboutAwardsBlock', 'EN', 'Awards & Recognitions', 'Our awards', 'Recognition collected across design, storytelling, and digital craft.'
) seed
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  subtitle = VALUES(subtitle),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

-- ============================================================
-- 3. COMPONENT_ENTRIES
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
  SELECT 'b1300001-0000-4000-8000-000000000001' AS uuid, 'AboutHeroPrimary' AS uid, 'AboutHeroComponent' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'b1300002-0000-4000-8000-000000000002', 'AboutStoryMedia1', 'AboutStoryComponent', 0
  UNION ALL SELECT 'b1300003-0000-4000-8000-000000000003', 'AboutStoryMedia2', 'AboutStoryComponent', 1
  UNION ALL SELECT 'b1300004-0000-4000-8000-000000000004', 'AboutStoryMedia3', 'AboutStoryComponent', 2
  UNION ALL SELECT 'b1300005-0000-4000-8000-000000000005', 'AboutStoryList1', 'AboutStoryComponent', 3
  UNION ALL SELECT 'b1300006-0000-4000-8000-000000000006', 'AboutStoryList2', 'AboutStoryComponent', 4
  UNION ALL SELECT 'b1300007-0000-4000-8000-000000000007', 'AboutTeamMember1', 'AboutTeamSection', 0
  UNION ALL SELECT 'b1300008-0000-4000-8000-000000000008', 'AboutTeamMember2', 'AboutTeamSection', 1
  UNION ALL SELECT 'b1300009-0000-4000-8000-000000000009', 'AboutTeamMember3', 'AboutTeamSection', 2
  UNION ALL SELECT 'b1300010-0000-4000-8000-000000000010', 'AboutTeamMember4', 'AboutTeamSection', 3
  UNION ALL SELECT 'b1300011-0000-4000-8000-000000000011', 'AboutTeamMember5', 'AboutTeamSection', 4
  UNION ALL SELECT 'b1300012-0000-4000-8000-000000000012', 'AboutTeamMember6', 'AboutTeamSection', 5
  UNION ALL SELECT 'b1300013-0000-4000-8000-000000000013', 'AboutTeamMember7', 'AboutTeamSection', 6
  UNION ALL SELECT 'b1300014-0000-4000-8000-000000000014', 'AboutTeamMember8', 'AboutTeamSection', 7
  UNION ALL SELECT 'b1300015-0000-4000-8000-000000000015', 'AboutStat1', 'AboutStatsSection', 0
  UNION ALL SELECT 'b1300016-0000-4000-8000-000000000016', 'AboutStat2', 'AboutStatsSection', 1
  UNION ALL SELECT 'b1300017-0000-4000-8000-000000000017', 'AboutStat3', 'AboutStatsSection', 2
  UNION ALL SELECT 'b1300018-0000-4000-8000-000000000018', 'AboutStat4', 'AboutStatsSection', 3
  UNION ALL SELECT 'b1300019-0000-4000-8000-000000000019', 'AboutBrandLogo1', 'AboutBrandsStrip', 0
  UNION ALL SELECT 'b1300020-0000-4000-8000-000000000020', 'AboutBrandLogo2', 'AboutBrandsStrip', 1
  UNION ALL SELECT 'b1300021-0000-4000-8000-000000000021', 'AboutBrandLogo3', 'AboutBrandsStrip', 2
  UNION ALL SELECT 'b1300022-0000-4000-8000-000000000022', 'AboutBrandLogo4', 'AboutBrandsStrip', 3
  UNION ALL SELECT 'b1300023-0000-4000-8000-000000000023', 'AboutBrandLogo5', 'AboutBrandsStrip', 4
  UNION ALL SELECT 'b1300024-0000-4000-8000-000000000024', 'AboutBrandLogo6', 'AboutBrandsStrip', 5
  UNION ALL SELECT 'b1300025-0000-4000-8000-000000000025', 'AboutAward1', 'AboutAwardsBlock', 0
  UNION ALL SELECT 'b1300026-0000-4000-8000-000000000026', 'AboutAward2', 'AboutAwardsBlock', 1
  UNION ALL SELECT 'b1300027-0000-4000-8000-000000000027', 'AboutAward3', 'AboutAwardsBlock', 2
  UNION ALL SELECT 'b1300028-0000-4000-8000-000000000028', 'AboutAward4', 'AboutAwardsBlock', 3
  UNION ALL SELECT 'b1300029-0000-4000-8000-000000000029', 'AboutAward5', 'AboutAwardsBlock', 4
  UNION ALL SELECT 'b1300030-0000-4000-8000-000000000030', 'AboutAward6', 'AboutAwardsBlock', 5
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
-- 4. COMPONENT_ENTRY_I18N
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
  SELECT 'b1400001-0000-4000-8000-000000000001' AS uuid, 'AboutHeroPrimaryTr' AS uid, 'AboutHeroPrimary' AS entry_uid, 'TR' AS language, 'Hero arka planı' AS title, 'Liko, sonuç üreten web siteleri ve yaratıcı kampanyalar geliştirir, tasarlar ve hayata geçirir.' AS description, JSON_OBJECT('mediaUid', 'about-hero-bg', 'buttonText', 'Hikayemiz', 'buttonUrl', '#AboutStoryComponent', 'supportingText', 'Liko, sonuç üreten web siteleri ve yaratıcı kampanyalar geliştirir, tasarlar ve hayata geçirir.', 'scrollTarget', 'AboutStoryComponent') AS custom_data
  UNION ALL SELECT 'b1400002-0000-4000-8000-000000000002', 'AboutHeroPrimaryEn', 'AboutHeroPrimary', 'EN', 'About hero background', 'Liko develops, designs and delivers websites and creative campaigns that drive results.', JSON_OBJECT('mediaUid', 'about-hero-bg', 'buttonText', 'Our Story', 'buttonUrl', '#AboutStoryComponent', 'supportingText', 'Liko develops, designs and delivers websites and creative campaigns that drive results.', 'scrollTarget', 'AboutStoryComponent')
  UNION ALL SELECT 'b1400003-0000-4000-8000-000000000003', 'AboutStoryMedia1Tr', 'AboutStoryMedia1', 'TR', 'Stüdyo portresi', NULL, JSON_OBJECT('mediaUid', 'about-story-1', 'introLabel', 'Merhaba!')
  UNION ALL SELECT 'b1400004-0000-4000-8000-000000000004', 'AboutStoryMedia1En', 'AboutStoryMedia1', 'EN', 'Studio portrait', NULL, JSON_OBJECT('mediaUid', 'about-story-1', 'introLabel', 'Hi!')
  UNION ALL SELECT 'b1400005-0000-4000-8000-000000000005', 'AboutStoryMedia2Tr', 'AboutStoryMedia2', 'TR', 'Yaratıcı stüdyo', NULL, JSON_OBJECT('mediaUid', 'about-story-2')
  UNION ALL SELECT 'b1400006-0000-4000-8000-000000000006', 'AboutStoryMedia2En', 'AboutStoryMedia2', 'EN', 'Creative studio', NULL, JSON_OBJECT('mediaUid', 'about-story-2')
  UNION ALL SELECT 'b1400007-0000-4000-8000-000000000007', 'AboutStoryMedia3Tr', 'AboutStoryMedia3', 'TR', 'Ürün iş birliği', NULL, JSON_OBJECT('mediaUid', 'about-story-3')
  UNION ALL SELECT 'b1400008-0000-4000-8000-000000000008', 'AboutStoryMedia3En', 'AboutStoryMedia3', 'EN', 'Product collaboration', NULL, JSON_OBJECT('mediaUid', 'about-story-3')
  UNION ALL SELECT 'b1400009-0000-4000-8000-000000000009', 'AboutStoryList1Tr', 'AboutStoryList1', 'TR', 'Marka & sanat yönetimi', NULL, JSON_OBJECT('items', JSON_ARRAY('Sanat yönetimi', 'Markalaşma', 'İçerik üretimi', 'Kullanıcı arayüz tasarımı', 'Animasyon'))
  UNION ALL SELECT 'b1400010-0000-4000-8000-000000000010', 'AboutStoryList1En', 'AboutStoryList1', 'EN', 'Brand & art direction', NULL, JSON_OBJECT('items', JSON_ARRAY('Art direction', 'Branding', 'Content Production', 'User Interface Design', 'Animation'))
  UNION ALL SELECT 'b1400011-0000-4000-8000-000000000011', 'AboutStoryList2Tr', 'AboutStoryList2', 'TR', 'Deneyim sistemleri', NULL, JSON_OBJECT('items', JSON_ARRAY('Marka kimliği', 'Kullanıcı arayüz', 'Kullanıcı deneyimi', 'Duyarlı tasarım'))
  UNION ALL SELECT 'b1400012-0000-4000-8000-000000000012', 'AboutStoryList2En', 'AboutStoryList2', 'EN', 'Experience systems', NULL, JSON_OBJECT('items', JSON_ARRAY('Brand Identity', 'User Interface', 'User Experience', 'Responsive Design'))
  UNION ALL SELECT 'b1400013-0000-4000-8000-000000000013', 'AboutTeamMember1Tr', 'AboutTeamMember1', 'TR', 'Jane Mills', 'Lansman kampanyaları ve yeni ürün çalışmalarında marka sistemlerini ve görsel anlatıları şekillendirir.', JSON_OBJECT('role', 'Sanat yönetimi', 'mediaUid', 'about-team-1')
  UNION ALL SELECT 'b1400014-0000-4000-8000-000000000014', 'AboutTeamMember1En', 'AboutTeamMember1', 'EN', 'Jane Mills', 'Shapes brand systems and visual narratives across launch campaigns and new product work.', JSON_OBJECT('role', 'Art direction', 'mediaUid', 'about-team-1')
  UNION ALL SELECT 'b1400015-0000-4000-8000-000000000015', 'AboutTeamMember2Tr', 'AboutTeamMember2', 'TR', 'Jim Mathis', 'Stratejiyi net marka yönlerine, sunum sistemlerine ve pitch anlatılarına dönüştürür.', JSON_OBJECT('role', 'Marka stratejisi', 'mediaUid', 'about-team-2')
  UNION ALL SELECT 'b1400016-0000-4000-8000-000000000016', 'AboutTeamMember2En', 'AboutTeamMember2', 'EN', 'Jim Mathis', 'Translates strategy into clear brand directions, presentation systems, and pitch narratives.', JSON_OBJECT('role', 'Brand strategy', 'mediaUid', 'about-team-2')
  UNION ALL SELECT 'b1400017-0000-4000-8000-000000000017', 'AboutTeamMember3Tr', 'AboutTeamMember3', 'TR', 'Randolph Cole', 'Dijital yolculuklara ritim katan hareket sistemleri ve dokunsal etkileşimler kurar.', JSON_OBJECT('role', 'Hareket tasarımı', 'mediaUid', 'about-team-3')
  UNION ALL SELECT 'b1400018-0000-4000-8000-000000000018', 'AboutTeamMember3En', 'AboutTeamMember3', 'EN', 'Randolph Cole', 'Builds motion systems and tactile interactions that add rhythm to digital journeys.', JSON_OBJECT('role', 'Motion design', 'mediaUid', 'about-team-3')
  UNION ALL SELECT 'b1400019-0000-4000-8000-000000000019', 'AboutTeamMember4Tr', 'AboutTeamMember4', 'TR', 'Anna Mullins', 'Güçlü bir editoryal bakışla ve sağlam bir bileşen yaklaşımıyla kusursuz arayüzler tasarlar.', JSON_OBJECT('role', 'Dijital tasarım', 'mediaUid', 'about-team-4')
  UNION ALL SELECT 'b1400020-0000-4000-8000-000000000020', 'AboutTeamMember4En', 'AboutTeamMember4', 'EN', 'Anna Mullins', 'Designs polished interfaces with a strong editorial eye and durable component thinking.', JSON_OBJECT('role', 'Digital design', 'mediaUid', 'about-team-4')
  UNION ALL SELECT 'b1400021-0000-4000-8000-000000000021', 'AboutTeamMember5Tr', 'AboutTeamMember5', 'TR', 'Devin Minor', 'Çok kanallı kampanyalar için araştırma, mesajlaşma ve ürün hikayelerini bir araya getirir.', JSON_OBJECT('role', 'Ürün hikayeleştirmesi', 'mediaUid', 'about-team-5')
  UNION ALL SELECT 'b1400022-0000-4000-8000-000000000022', 'AboutTeamMember5En', 'AboutTeamMember5', 'EN', 'Devin Minor', 'Bridges research, messaging, and product storytelling for multi-channel campaigns.', JSON_OBJECT('role', 'Product storytelling', 'mediaUid', 'about-team-5')
  UNION ALL SELECT 'b1400023-0000-4000-8000-000000000023', 'AboutTeamMember6Tr', 'AboutTeamMember6', 'TR', 'Lucas Gray', 'Arayüz detayını hizmet deneyimiyle ve dönüşüme odaklı akışlarla birleştirir.', JSON_OBJECT('role', 'Deneyim tasarımı', 'mediaUid', 'about-team-6')
  UNION ALL SELECT 'b1400024-0000-4000-8000-000000000024', 'AboutTeamMember6En', 'AboutTeamMember6', 'EN', 'Lucas Gray', 'Connects interface detail with service experience and conversion-focused flows.', JSON_OBJECT('role', 'Experience design', 'mediaUid', 'about-team-6')
  UNION ALL SELECT 'b1400025-0000-4000-8000-000000000025', 'AboutTeamMember7Tr', 'AboutTeamMember7', 'TR', 'Deborah Jones', 'Karmaşık hikayeleri okunabilir kılan kısa metinler ve içerik kurguları yazar.', JSON_OBJECT('role', 'Metin & içerik', 'mediaUid', 'about-team-7')
  UNION ALL SELECT 'b1400026-0000-4000-8000-000000000026', 'AboutTeamMember7En', 'AboutTeamMember7', 'EN', 'Deborah Jones', 'Writes concise copy and content structures that keep complex stories readable.', JSON_OBJECT('role', 'Copy & content', 'mediaUid', 'about-team-7')
  UNION ALL SELECT 'b1400027-0000-4000-8000-000000000027', 'AboutTeamMember8Tr', 'AboutTeamMember8', 'TR', 'Miller Stone', 'Tasarım ustalığını ön yüz deneyleriyle birleştirerek fikirleri kullanılabilir sistemlere dönüştürür.', JSON_OBJECT('role', 'Yaratıcı geliştirme', 'mediaUid', 'about-team-8')
  UNION ALL SELECT 'b1400028-0000-4000-8000-000000000028', 'AboutTeamMember8En', 'AboutTeamMember8', 'EN', 'Miller Stone', 'Pairs design craft with frontend experimentation to turn concepts into usable systems.', JSON_OBJECT('role', 'Creative development', 'mediaUid', 'about-team-8')
  UNION ALL SELECT 'b1400029-0000-4000-8000-000000000029', 'AboutStat1Tr', 'AboutStat1', 'TR', 'TAMAMLANAN PROJELER', NULL, JSON_OBJECT('statValue', 245, 'statSuffix', '+')
  UNION ALL SELECT 'b1400030-0000-4000-8000-000000000030', 'AboutStat1En', 'AboutStat1', 'EN', 'PROJECTS FINISHED', NULL, JSON_OBJECT('statValue', 245, 'statSuffix', '+')
  UNION ALL SELECT 'b1400031-0000-4000-8000-000000000031', 'AboutStat2Tr', 'AboutStat2', 'TR', 'TECRÜBE YILLARI', NULL, JSON_OBJECT('statValue', 17, 'statSuffix', '+')
  UNION ALL SELECT 'b1400032-0000-4000-8000-000000000032', 'AboutStat2En', 'AboutStat2', 'EN', 'YEARS OF EXPERIENCE', NULL, JSON_OBJECT('statValue', 17, 'statSuffix', '+')
  UNION ALL SELECT 'b1400033-0000-4000-8000-000000000033', 'AboutStat3Tr', 'AboutStat3', 'TR', 'EKİP ÜYELERİ', NULL, JSON_OBJECT('statValue', 9, 'statSuffix', '+')
  UNION ALL SELECT 'b1400034-0000-4000-8000-000000000034', 'AboutStat3En', 'AboutStat3', 'EN', 'TEAM MEMBERS', NULL, JSON_OBJECT('statValue', 9, 'statSuffix', '+')
  UNION ALL SELECT 'b1400035-0000-4000-8000-000000000035', 'AboutStat4Tr', 'AboutStat4', 'TR', 'BÜYÜYEN AJANS', NULL, JSON_OBJECT('statValue', 194, 'statSuffix', '%')
  UNION ALL SELECT 'b1400036-0000-4000-8000-000000000036', 'AboutStat4En', 'AboutStat4', 'EN', 'GROWING AGENCY', NULL, JSON_OBJECT('statValue', 194, 'statSuffix', '%')
  UNION ALL SELECT 'b1400037-0000-4000-8000-000000000037', 'AboutBrandLogo1Tr', 'AboutBrandLogo1', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-1', 'altText', 'Ortak logo 1')
  UNION ALL SELECT 'b1400038-0000-4000-8000-000000000038', 'AboutBrandLogo1En', 'AboutBrandLogo1', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-1', 'altText', 'Partner logo 1')
  UNION ALL SELECT 'b1400039-0000-4000-8000-000000000039', 'AboutBrandLogo2Tr', 'AboutBrandLogo2', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-2', 'altText', 'Ortak logo 2')
  UNION ALL SELECT 'b1400040-0000-4000-8000-000000000040', 'AboutBrandLogo2En', 'AboutBrandLogo2', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-2', 'altText', 'Partner logo 2')
  UNION ALL SELECT 'b1400041-0000-4000-8000-000000000041', 'AboutBrandLogo3Tr', 'AboutBrandLogo3', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-3', 'altText', 'Ortak logo 3')
  UNION ALL SELECT 'b1400042-0000-4000-8000-000000000042', 'AboutBrandLogo3En', 'AboutBrandLogo3', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-3', 'altText', 'Partner logo 3')
  UNION ALL SELECT 'b1400043-0000-4000-8000-000000000043', 'AboutBrandLogo4Tr', 'AboutBrandLogo4', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-4', 'altText', 'Ortak logo 4')
  UNION ALL SELECT 'b1400044-0000-4000-8000-000000000044', 'AboutBrandLogo4En', 'AboutBrandLogo4', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-4', 'altText', 'Partner logo 4')
  UNION ALL SELECT 'b1400045-0000-4000-8000-000000000045', 'AboutBrandLogo5Tr', 'AboutBrandLogo5', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-5', 'altText', 'Ortak logo 5')
  UNION ALL SELECT 'b1400046-0000-4000-8000-000000000046', 'AboutBrandLogo5En', 'AboutBrandLogo5', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-5', 'altText', 'Partner logo 5')
  UNION ALL SELECT 'b1400047-0000-4000-8000-000000000047', 'AboutBrandLogo6Tr', 'AboutBrandLogo6', 'TR', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-2', 'altText', 'Ortak logo 6')
  UNION ALL SELECT 'b1400048-0000-4000-8000-000000000048', 'AboutBrandLogo6En', 'AboutBrandLogo6', 'EN', NULL, NULL, JSON_OBJECT('mediaUid', 'about-brand-2', 'altText', 'Partner logo 6')
  UNION ALL SELECT 'b1400049-0000-4000-8000-000000000049', 'AboutAward1Tr', 'AboutAward1', 'TR', 'FWA, Günün Sitesi', NULL, JSON_OBJECT('subtitle', 'x2', 'date', '24 Haziran, 2024', 'mediaUid', 'homepage-award-1')
  UNION ALL SELECT 'b1400050-0000-4000-8000-000000000050', 'AboutAward1En', 'AboutAward1', 'EN', 'FWA, Site of the Day', NULL, JSON_OBJECT('subtitle', 'x2', 'date', 'Jun 24, 2024', 'mediaUid', 'homepage-award-1')
  UNION ALL SELECT 'b1400051-0000-4000-8000-000000000051', 'AboutAward2Tr', 'AboutAward2', 'TR', 'Awwwards İç Mekan Mükemmelliği', NULL, JSON_OBJECT('subtitle', 'x3', 'date', '24 Kasım, 2022', 'mediaUid', 'about-award-2')
  UNION ALL SELECT 'b1400052-0000-4000-8000-000000000052', 'AboutAward2En', 'AboutAward2', 'EN', 'Awwwards Interior Excellence', NULL, JSON_OBJECT('subtitle', 'x3', 'date', 'Nov 24, 2022', 'mediaUid', 'about-award-2')
  UNION ALL SELECT 'b1400053-0000-4000-8000-000000000053', 'AboutAward3Tr', 'AboutAward3', 'TR', 'Loki, Sınırları Zorlayan Yıl Özeti 2022', NULL, JSON_OBJECT('subtitle', 'x1', 'date', '24 Mayıs, 2022', 'mediaUid', 'about-award-3')
  UNION ALL SELECT 'b1400054-0000-4000-8000-000000000054', 'AboutAward3En', 'AboutAward3', 'EN', 'Loki boundary pushing year in Review 2022', NULL, JSON_OBJECT('subtitle', 'x1', 'date', 'May 24, 2022', 'mediaUid', 'about-award-3')
  UNION ALL SELECT 'b1400055-0000-4000-8000-000000000055', 'AboutAward4Tr', 'AboutAward4', 'TR', 'Yeni Liko Tools Web Sitesi Yayında.', NULL, JSON_OBJECT('subtitle', 'x1', 'date', '10 Eylül, 2021', 'mediaUid', 'about-award-4')
  UNION ALL SELECT 'b1400056-0000-4000-8000-000000000056', 'AboutAward4En', 'AboutAward4', 'EN', 'The New Liko Tools Website is Live.', NULL, JSON_OBJECT('subtitle', 'x1', 'date', 'Sep 10, 2021', 'mediaUid', 'about-award-4')
  UNION ALL SELECT 'b1400057-0000-4000-8000-000000000057', 'AboutAward5Tr', 'AboutAward5', 'TR', 'Dünya Çapında Dijital Ajanslar', NULL, JSON_OBJECT('subtitle', 'x2', 'date', '12 Haziran, 2021', 'mediaUid', 'about-award-5')
  UNION ALL SELECT 'b1400058-0000-4000-8000-000000000058', 'AboutAward5En', 'AboutAward5', 'EN', 'Digital Agencies Worldwide', NULL, JSON_OBJECT('subtitle', 'x2', 'date', 'Jun 12, 2021', 'mediaUid', 'about-award-5')
  UNION ALL SELECT 'b1400059-0000-4000-8000-000000000059', 'AboutAward6Tr', 'AboutAward6', 'TR', 'FWA, Günün Sitesi', NULL, JSON_OBJECT('subtitle', 'x1', 'date', '18 Ağustos, 2022', 'mediaUid', 'about-award-6')
  UNION ALL SELECT 'b1400060-0000-4000-8000-000000000060', 'AboutAward6En', 'AboutAward6', 'EN', 'FWA, Site of the Day', NULL, JSON_OBJECT('subtitle', 'x1', 'date', 'Aug 18, 2022', 'mediaUid', 'about-award-6')
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
-- 5. PAGES (ContentPageTemplate, CONTENT type, /about-us)
-- ============================================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'f0000005-0000-0000-0000-000000000005', 'about-us',
  (SELECT id FROM page_templates WHERE uid = 'ContentPageTemplate'),
  'PUBLISHED', 'CONTENT', FALSE, 'INDEX_FOLLOW', NULL
ON DUPLICATE KEY UPDATE
  template_id = VALUES(template_id),
  status = VALUES(status),
  page_type = VALUES(page_type),
  is_home = VALUES(is_home),
  robot_tag = VALUES(robot_tag);

-- ============================================================
-- 6. PAGE_I18N
-- ============================================================

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000005-0000-0001-0000-000000000001', 'about-us-tr', p.id, 'TR', 'Hakkımızda', 'Hakkımızda', '/about-us', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'about-us'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000005-0000-0001-0000-000000000002', 'about-us-en', p.id, 'EN', 'About Us', 'About Us', '/about-us', 'PUBLISHED'
FROM pages p
WHERE p.uid = 'about-us'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  title = VALUES(title),
  canonical_url = VALUES(canonical_url),
  status = VALUES(status);

-- ============================================================
-- 7. PAGE_SLOTS (TopContent, BodyContent, SideContent)
-- ============================================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT UUID(), CONCAT(p.uid, '-', ts.slot_name, 'Slot'), p.id, ts.slot_name, ts.position, ts.sort_order, TRUE, FALSE, NOW(), NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid = 'about-us'
  AND ts.slot_name IN ('TopContent', 'BodyContent', 'SideContent')
ON DUPLICATE KEY UPDATE
  position = VALUES(position),
  sort_order = VALUES(sort_order),
  updated_at = NOW();

-- ============================================================
-- 8. SLOT_COMPONENTS
-- ============================================================
-- ImpEx allows only INSERT, UPDATE, SELECT. Hide AboutBrandsStrip
-- from SideContent slot if present (do not delete).

UPDATE slot_components sc
JOIN page_slots ps ON ps.id = sc.slot_id
JOIN components c ON c.id = sc.component_id
SET sc.is_visible = FALSE
WHERE ps.uid = 'about-us-SideContentSlot'
  AND c.uid = 'AboutBrandsStrip';

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, seed.sort_order, TRUE, NOW()
FROM (
  SELECT 'about-us-TopContentSlot' AS slot_uid, 'AboutHeroComponent' AS component_uid, 0 AS sort_order
  UNION ALL SELECT 'about-us-BodyContentSlot', 'AboutStoryComponent', 0
  UNION ALL SELECT 'about-us-BodyContentSlot', 'AboutTeamSection', 1
  UNION ALL SELECT 'about-us-BodyContentSlot', 'AboutStatsSection', 2
  UNION ALL SELECT 'about-us-BodyContentSlot', 'AboutBrandsStrip', 3
  UNION ALL SELECT 'about-us-BodyContentSlot', 'AboutAwardsBlock', 4
) seed
JOIN page_slots ps ON ps.uid = seed.slot_uid
JOIN components c ON c.uid = seed.component_uid
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  is_visible = VALUES(is_visible);
