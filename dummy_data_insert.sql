-- AdminCraft Sprint 1 - Dummy Data Insert Script
-- This script creates realistic test data for all Sprint 1 entities
-- Run this after the Flyway migration has completed successfully

-- =================================================================
-- 1. INSERT TENANTS
-- =================================================================

INSERT INTO tenants (
    subdomain, company_name, database_name, status, default_language, 
    supported_languages, plan_type, contact_email, contact_name, contact_phone,
    created_at, updated_at
) VALUES 
-- Turkish Newspaper
(
    'turkiye-gazetesi', 
    'Türkiye Gazetesi', 
    'turkiye_gazetesi_db', 
    'ACTIVE', 
    'TR',
    '["TR", "EN"]',
    'PROFESSIONAL',
    'info@turkiye-gazetesi.com',
    'Ahmet Yılmaz',
    '+90 212 555 1234',
    NOW(),
    NOW()
),
-- Tech Blog Platform
(
    'tech-blog', 
    'Tech Blog Platform', 
    'tech_blog_db', 
    'ACTIVE', 
    'EN',
    '["EN", "TR"]',
    'BASIC',
    'admin@tech-blog.com',
    'John Smith',
    '+1 555 987 6543',
    NOW(),
    NOW()
),
-- E-commerce Store
(
    'online-magaza', 
    'Online Mağaza Ltd', 
    'online_magaza_db', 
    'PENDING', 
    'TR',
    '["TR", "EN"]',
    'ENTERPRISE',
    'destek@online-magaza.com',
    'Fatma Kaya',
    '+90 532 123 4567',
    NOW(),
    NOW()
),
-- Travel Blog
(
    'travel-stories', 
    'Travel Stories Blog', 
    'travel_stories_db', 
    'ACTIVE', 
    'EN',
    '["EN", "TR", "DE"]',
    'BASIC',
    'hello@travel-stories.com',
    'Emma Wilson',
    '+44 20 7946 0958',
    NOW(),
    NOW()
),
-- Corporate Website
(
    'kurumsal-firma', 
    'Kurumsal Firma A.Ş.', 
    'kurumsal_firma_db', 
    'SUSPENDED', 
    'TR',
    '["TR"]',
    'PROFESSIONAL',
    'iletisim@kurumsal-firma.com',
    'Mehmet Özkan',
    '+90 216 444 5566',
    NOW(),
    NOW()
);

-- =================================================================
-- 2. INSERT USERS
-- =================================================================

INSERT INTO users (
    email, password_hash, full_name, first_name, last_name, role, 
    preferred_language, tenant_id, phone, job_title, department,
    is_active, email_verified, created_at, updated_at, password_changed_at
) VALUES 
-- Super Admin
(
    'admin@admincraft.com',
    '$2a$12$YQiQxpQRmkxCKo8Pq.xHJ.5HqGx4lGJmQ2H6M8VK9hF7X3B2N1C0O', -- AdminCraft123!
    'Sistem Yöneticisi',
    'Sistem',
    'Yöneticisi',
    'SUPER_ADMIN',
    'TR',
    1,
    '+90 555 000 0001',
    'Sistem Yöneticisi',
    'IT',
    TRUE,
    TRUE,
    NOW(),
    NOW(),
    NOW()
),
-- Tenant Admin - Turkish Newspaper
(
    'admin@turkiye-gazetesi.com',
    '$2a$12$AbCdEfGhIjKlMnOpQrStUv.wxYzAbCdEfGhIjKlMnOpQrStUvWxYz', -- TurkiyeGazete123!
    'Ahmet Yılmaz',
    'Ahmet',
    'Yılmaz',
    'TENANT_ADMIN',
    'TR',
    1,
    '+90 212 555 1234',
    'Genel Koordinatör',
    'Yönetim',
    TRUE,
    TRUE,
    NOW(),
    NOW(),
    NOW()
),
-- Editor - Tech Blog
(
    'editor@tech-blog.com',
    '$2a$12$TechBlogHashedPasswordExampleForTestingPurposes123456', -- TechBlog123!
    'John Smith',
    'John',
    'Smith',
    'EDITOR',
    'EN',
    2,
    '+1 555 987 6543',
    'Content Editor',
    'Editorial',
    TRUE,
    TRUE,
    NOW(),
    NOW(),
    NOW()
),
-- Writer - Turkish Newspaper
(
    'yazar@turkiye-gazetesi.com',
    '$2a$12$YazarHashedPasswordExampleForSecureTestingData789012', -- Yazar123!
    'Elif Demir',
    'Elif',
    'Demir',
    'EDITOR',
    'TR',
    1,
    '+90 532 111 2233',
    'Haber Editörü',
    'Haber',
    TRUE,
    TRUE,
    NOW(),
    NOW(),
    NOW()
),
-- Viewer - E-commerce
(
    'musteri@online-magaza.com',
    '$2a$12$MusteriViewerHashedPasswordForEcommerceTestData345678', -- Musteri123!
    'Ali Veli',
    'Ali',
    'Veli',
    'VIEWER',
    'TR',
    3,
    '+90 533 999 8877',
    'Müşteri Temsilcisi',
    'Müşteri Hizmetleri',
    TRUE,
    TRUE,
    NOW(),
    NOW(),
    NOW()
),
-- Travel Blog Admin
(
    'admin@travel-stories.com',
    '$2a$12$TravelStoriesAdminHashedPasswordForBlogTestingData901', -- TravelStories123!
    'Emma Wilson',
    'Emma',
    'Wilson',
    'TENANT_ADMIN',
    'EN',
    4,
    '+44 20 7946 0958',
    'Blog Administrator',
    'Content Management',
    TRUE,
    TRUE,
    NOW(),
    NOW(),
    NOW()
),
-- Corporate User
(
    'personel@kurumsal-firma.com',
    '$2a$12$KurumsalFirmaPersonelHashedPasswordForTestData234567', -- Kurumsal123!
    'Zeynep Aksoy',
    'Zeynep',
    'Aksoy',
    'EDITOR',
    'TR',
    5,
    '+90 216 333 4455',
    'İçerik Uzmanı',
    'Pazarlama',
    FALSE, -- Inactive because tenant is suspended
    TRUE,
    NOW(),
    NOW(),
    NOW()
);

-- =================================================================
-- 3. INSERT CONTENT TYPES
-- =================================================================

INSERT INTO content_types (
    name, display_name, display_name_tr, display_name_en, 
    description, description_tr, description_en, tenant_id,
    supports_multi_language, supports_seo, supports_scheduling, 
    supports_comments, requires_approval, is_system_type, is_active,
    sort_order, icon, color, created_by, created_at, updated_at
) VALUES 
-- News Article for Turkish Newspaper
(
    'news-article',
    'Haber Makalesi',
    'Haber Makalesi',
    'News Article',
    'Güncel haber içerikleri için kullanılır',
    'Güncel haber içerikleri için kullanılır',
    'Used for current news content',
    1, -- Türkiye Gazetesi
    TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE,
    1, 'newspaper', '#FF5722', 2,
    NOW(), NOW()
),
-- Tech Tutorial for Tech Blog
(
    'tech-tutorial',
    'Tech Tutorial',
    'Teknoloji Eğitimi',
    'Tech Tutorial',
    'Technical tutorials and guides',
    'Teknik eğitimler ve kılavuzlar',
    'Technical tutorials and guides',
    2, -- Tech Blog
    TRUE, TRUE, FALSE, TRUE, FALSE, FALSE, TRUE,
    1, 'code', '#4CAF50', 3,
    NOW(), NOW()
),
-- Product Review for E-commerce
(
    'product-review',
    'Ürün İncelemesi',
    'Ürün İncelemesi',
    'Product Review',
    'Ürün incelemesi ve değerlendirmeleri için',
    'Ürün incelemesi ve değerlendirmeleri için',
    'For product reviews and evaluations',
    3, -- Online Mağaza
    TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, TRUE,
    2, 'star', '#9C27B0', 5,
    NOW(), NOW()
),
-- Travel Story for Travel Blog
(
    'travel-story',
    'Travel Story',
    'Seyahat Hikayesi',
    'Travel Story',
    'Personal travel experiences and stories',
    'Kişisel seyahat deneyimleri ve hikayeleri',
    'Personal travel experiences and stories',
    4, -- Travel Stories
    TRUE, TRUE, FALSE, TRUE, FALSE, FALSE, TRUE,
    1, 'map', '#FF9800', 6,
    NOW(), NOW()
),
-- Corporate Page
(
    'corporate-page',
    'Kurumsal Sayfa',
    'Kurumsal Sayfa',
    'Corporate Page',
    'Kurumsal web sitesi sayfaları için',
    'Kurumsal web sitesi sayfaları için',
    'For corporate website pages',
    5, -- Kurumsal Firma
    FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, -- Inactive
    1, 'building', '#607D8B', 7,
    NOW(), NOW()
);

-- =================================================================
-- 4. INSERT CONTENTS
-- =================================================================

INSERT INTO contents (
    title, slug, excerpt, data, status, language, parent_content_id,
    content_type_id, tenant_id, meta_title, meta_description, meta_keywords,
    is_featured, is_sticky, template, layout, created_by, updated_by,
    published_by, published_at, created_at, updated_at
) VALUES 
-- Turkish News Article
(
    'Türkiye''de Teknoloji Sektörü Büyüyor',
    'turkiye-teknoloji-sektoru-buyuyor',
    'Türkiye''deki teknoloji şirketleri 2024 yılında rekor büyüme gösterdi.',
    '<p>Türkiye''deki teknoloji sektörü 2024 yılında <strong>%25</strong> büyüme kaydetti. Bu büyüme özellikle <em>yapay zeka</em> ve <em>bulut teknolojileri</em> alanında yaşandı.</p><h2>Sektör Liderleri</h2><ul><li>Yazılım geliştirme şirketleri</li><li>E-ticaret platformları</li><li>Fintech startupları</li></ul><p>Uzmanlar, bu trendin 2025 yılında da devam edeceğini öngörüyor.</p>',
    'PUBLISHED',
    'TR',
    NULL,
    1, -- news-article
    1, -- Türkiye Gazetesi
    'Türkiye Teknoloji Sektörü 2024 Büyüme Raporu',
    'Türkiye teknoloji sektörünün 2024 yılındaki %25 büyüme performansı ve gelecek öngörüleri.',
    'türkiye, teknoloji, büyüme, 2024, yapay zeka',
    TRUE, FALSE, 'news-article', 'default',
    2, 2, 2, '2024-01-20 09:00:00',
    NOW(), NOW()
),
-- English Tech Tutorial
(
    'Getting Started with React 18 Hooks',
    'getting-started-react-18-hooks',
    'Learn how to use the latest React 18 hooks in your applications.',
    '<p>React 18 introduced several new hooks that make state management and side effects easier to handle.</p><h2>New Hooks Overview</h2><h3>1. useId Hook</h3><p>The <code>useId</code> hook generates unique IDs that are stable across server and client rendering:</p><pre><code>import { useId } from ''react'';\n\nfunction MyComponent() {\n  const id = useId();\n  return &lt;div id={id}&gt;Unique content&lt;/div&gt;;\n}</code></pre><h3>2. useTransition Hook</h3><p>Mark updates as non-urgent to keep UI responsive:</p><pre><code>import { useTransition, useState } from ''react'';\n\nfunction SearchComponent() {\n  const [isPending, startTransition] = useTransition();\n  const [query, setQuery] = useState('''');\n  \n  const handleSearch = (value) => {\n    startTransition(() => {\n      setQuery(value);\n    });\n  };\n}</code></pre><p>These hooks improve performance and user experience significantly.</p>',
    'PUBLISHED',
    'EN',
    NULL,
    2, -- tech-tutorial
    2, -- Tech Blog
    'React 18 Hooks Tutorial - Complete Guide',
    'Complete guide to React 18 hooks including useId, useTransition, and performance optimization tips.',
    'react, hooks, javascript, tutorial, react18',
    TRUE, FALSE, 'tutorial', 'default',
    3, 3, 3, '2024-01-20 10:00:00',
    NOW(), NOW()
),
-- Turkish Translation of React Tutorial
(
    'React 18 Hook''ları ile Başlangıç',
    'react-18-hooklari-baslangic',
    'Uygulamalarınızda en yeni React 18 hook''larını nasıl kullanacağınızı öğrenin.',
    '<p>React 18, durum yönetimi ve yan etkileri ele almayı kolaylaştıran birçok yeni hook sundu.</p><h2>Yeni Hook''lara Genel Bakış</h2><h3>1. useId Hook</h3><p><code>useId</code> hook''u, sunucu ve istemci renderlaması arasında kararlı olan benzersiz ID''ler üretir:</p><pre><code>import { useId } from ''react'';\n\nfunction MyComponent() {\n  const id = useId();\n  return &lt;div id={id}&gt;Benzersiz içerik&lt;/div&gt;;\n}</code></pre><h3>2. useTransition Hook</h3><p>UI''yi duyarlı tutmak için güncellemeleri acil olmayan olarak işaretleyin:</p><pre><code>import { useTransition, useState } from ''react'';\n\nfunction SearchComponent() {\n  const [isPending, startTransition] = useTransition();\n  const [query, setQuery] = useState('''');\n  \n  const handleSearch = (value) => {\n    startTransition(() => {\n      setQuery(value);\n    });\n  };\n}</code></pre><p>Bu hook''lar performansı ve kullanıcı deneyimini önemli ölçüde artırır.</p>',
    'DRAFT',
    'TR',
    2, -- Translation of content ID 2
    2, -- tech-tutorial
    2, -- Tech Blog
    'React 18 Hook''ları Eğitimi - Tam Kılavuz',
    'useId, useTransition dahil React 18 hook''ları ve performans optimizasyonu ipuçları ile tam kılavuz.',
    'react, hooks, javascript, eğitim, react18',
    FALSE, FALSE, 'tutorial', 'default',
    4, NULL, NULL, NULL,
    NOW(), NOW()
),
-- Travel Story
(
    'Amazing Journey Through Cappadocia',
    'amazing-journey-cappadocia',
    'A magical experience exploring the fairy chimneys and underground cities of Cappadocia.',
    '<p>Cappadocia is one of those places that seems too magical to be real. The landscape of <strong>fairy chimneys</strong>, cave churches, and underground cities creates an otherworldly experience.</p><h2>Day 1: Balloon Ride at Sunrise</h2><p>We started our journey with a <em>hot air balloon ride</em> at sunrise. The view from above was absolutely breathtaking:</p><ul><li>Hundreds of colorful balloons floating in the sky</li><li>The unique rock formations stretching endlessly</li><li>Ancient cave dwellings carved into the rocks</li></ul><h2>Day 2: Exploring Underground Cities</h2><p>The underground city of <strong>Derinkuyu</strong> was mind-blowing. Going 8 levels deep, it once housed 20,000 people!</p><h2>Travel Tips</h2><p>Best time to visit: April-May or September-October for perfect weather.</p>',
    'PUBLISHED',
    'EN',
    NULL,
    4, -- travel-story
    4, -- Travel Stories
    'Cappadocia Travel Guide - Complete Journey Experience',
    'Discover the magical landscapes of Cappadocia with hot air balloons, underground cities, and fairy chimneys.',
    'cappadocia, turkey, travel, balloon, underground city, fairy chimneys',
    TRUE, TRUE, 'travel-story', 'default',
    6, 6, 6, '2024-01-18 14:30:00',
    NOW(), NOW()
),
-- Breaking News - Scheduled
(
    'Son Dakika: Yeni Teknoloji Yatırımları Açıklandı',
    'son-dakika-teknoloji-yatirimlari',
    'Hükümet teknoloji sektörüne 5 milyar TL''lik yeni yatırım paketini açıkladı.',
    '<p><strong>Son Dakika:</strong> Sanayi ve Teknoloji Bakanı, teknoloji sektörünü desteklemek için hazırlanan <strong>5 milyar TL''lik</strong> yatırım paketini açıkladı.</p><h2>Yatırım Detayları</h2><ul><li>Yapay zeka araştırmaları: 2 milyar TL</li><li>Siber güvenlik projeleri: 1.5 milyar TL</li><li>Startup destekleri: 1 milyar TL</li><li>Teknoloji transfer ofisleri: 500 milyon TL</li></ul><p>Bu yatırımların 3 yıl içinde <em>50 bin yeni istihdam</em> yaratması hedefleniyor.</p>',
    'SCHEDULED',
    'TR',
    NULL,
    1, -- news-article
    1, -- Türkiye Gazetesi
    'Son Dakika: 5 Milyar TL Teknoloji Yatırımı',
    'Hükümet teknoloji sektörüne 5 milyar TL yatırım yapacak. Detaylar haberimizde.',
    'teknoloji, yatırım, hükümet, yapay zeka, startup',
    TRUE, TRUE, 'breaking-news', 'urgent',
    4, NULL, NULL, NULL,
    NOW(), NOW()
);

-- =================================================================
-- 5. INSERT MEDIA FILES
-- =================================================================

INSERT INTO media_files (
    original_name, file_name, file_path, mime_type, file_size, file_extension,
    width, height, has_thumbnails, thumbnail_path,
    alt_text_tr, alt_text_en, description_tr, description_en, title_tr, title_en,
    folder, category, tags, tenant_id, uploaded_by, is_public, is_optimized,
    usage_count, storage_provider, metadata, created_at, updated_at, last_accessed_at
) VALUES 
-- Company Logo - Turkish Newspaper
(
    'turkiye-gazetesi-logo.png',
    'logo-turkiye-gazetesi-001.png',
    '/uploads/logos/logo-turkiye-gazetesi-001.png',
    'image/png',
    45760,
    'png',
    300, 120, FALSE, NULL,
    'Türkiye Gazetesi resmi logosu',
    'Turkey Newspaper official logo',
    'Türkiye Gazetesi''nin resmi logo dosyası, marka kimliği için kullanılır',
    'Official logo file of Turkey Newspaper, used for brand identity',
    'Türkiye Gazetesi Logo',
    'Turkey Newspaper Logo',
    'logos', 'branding', '["logo", "brand", "newspaper", "turkish"]',
    1, 2, TRUE, TRUE, 0,
    'local',
    '{"uploaded_from": "admin_panel", "image_quality": "high", "color_profile": "sRGB"}',
    NOW(), NOW(), NOW()
),
-- Tech News Image
(
    'teknoloji-haberi-gorsel.jpg',
    'tech-news-image-001.jpg',
    '/uploads/news-images/tech-news-image-001.jpg',
    'image/jpeg',
    245760,
    'jpg',
    800, 600, TRUE, '/uploads/thumbnails/tech-news-image-001-thumb.jpg',
    'Teknoloji sektörü büyüme grafiği',
    'Technology sector growth chart',
    'Türkiye teknoloji sektörü 2024 büyüme istatistikleri grafiği',
    'Turkey technology sector 2024 growth statistics chart',
    'Teknoloji Büyüme Grafiği',
    'Technology Growth Chart',
    'news-images', 'charts', '["technology", "growth", "statistics", "2024"]',
    1, 2, TRUE, TRUE, 1,
    'local',
    '{"exif_data": {"camera": "Canon EOS R5", "iso": 200}, "compression_ratio": 85}',
    NOW(), NOW(), '2024-01-20 09:05:00'
),
-- Profile Photo - John Smith
(
    'john-smith-profile.jpg',
    'profile-john-smith-002.jpg',
    '/uploads/profiles/profile-john-smith-002.jpg',
    'image/jpeg',
    128560,
    'jpg',
    400, 400, TRUE, '/uploads/thumbnails/profile-john-smith-002-thumb.jpg',
    'John Smith profil fotoğrafı',
    'John Smith profile photo',
    'Tech Blog editörü John Smith''in profil fotoğrafı',
    'Profile photo of Tech Blog editor John Smith',
    'John Smith',
    'John Smith',
    'profiles', 'user-avatars', '["profile", "editor", "tech-blog", "author"]',
    2, 3, FALSE, TRUE, 3,
    'local',
    '{"face_detection": true, "crop_focus": "center", "privacy_level": "private"}',
    NOW(), NOW(), '2024-01-20 10:30:00'
),
-- Tutorial Screenshot
(
    'react-18-hooks-screenshot.png',
    'react18-hooks-demo-003.png',
    '/uploads/tutorial-images/react18-hooks-demo-003.png',
    'image/png',
    512000,
    'png',
    1200, 800, TRUE, '/uploads/thumbnails/react18-hooks-demo-003-thumb.png',
    'React 18 hooks kod örneği ekran görüntüsü',
    'React 18 hooks code example screenshot',
    'React 18 useId ve useTransition hook''ları kod örneği ekran görüntüsü',
    'React 18 useId and useTransition hooks code example screenshot',
    'React 18 Hooks Kodu',
    'React 18 Hooks Code',
    'tutorial-images', 'screenshots', '["react", "hooks", "code", "tutorial", "javascript"]',
    2, 3, TRUE, FALSE, 2,
    'local',
    '{"screenshot_tool": "Chrome DevTools", "code_theme": "VS Code Dark", "syntax": "javascript"}',
    NOW(), NOW(), '2024-01-20 11:15:00'
),
-- Travel Photo - Cappadocia
(
    'cappadocia-balloons-sunrise.jpg',
    'cappadocia-sunrise-balloons-004.jpg',
    '/uploads/travel-photos/cappadocia-sunrise-balloons-004.jpg',
    'image/jpeg',
    892400,
    'jpg',
    1920, 1280, TRUE, '/uploads/thumbnails/cappadocia-sunrise-balloons-004-thumb.jpg',
    'Kapadokya''da gün doğumunda balon turu',
    'Hot air balloons at sunrise in Cappadocia',
    'Kapadokya''da gün doğumunda yüzlerce sıcak hava balonu manzarası',
    'Hundreds of hot air balloons at sunrise in Cappadocia landscape',
    'Kapadokya Balon Turu',
    'Cappadocia Balloon Tour',
    'travel-photos', 'landscapes', '["cappadocia", "balloons", "sunrise", "turkey", "travel", "landscape"]',
    4, 6, TRUE, TRUE, 1,
    'local',
    '{"gps_coordinates": {"lat": 38.6431, "lng": 34.8597}, "camera_settings": {"aperture": "f/8", "shutter": "1/125"}}',
    NOW(), NOW(), '2024-01-18 15:00:00'
),
-- Breaking News Placeholder
(
    'breaking-news-placeholder.svg',
    'breaking-news-bg-005.svg',
    '/uploads/news-images/breaking-news-bg-005.svg',
    'image/svg+xml',
    8420,
    'svg',
    600, 300, FALSE, NULL,
    'Son dakika haber arka planı',
    'Breaking news background',
    'Son dakika haberler için kullanılan arka plan görseli',
    'Background image used for breaking news stories',
    'Son Dakika Arka Plan',
    'Breaking News Background',
    'news-images', 'backgrounds', '["breaking-news", "background", "urgent", "red"]',
    1, 4, TRUE, TRUE, 0,
    'local',
    '{"vector_format": "SVG", "scalable": true, "color_scheme": "red_urgent"}',
    NOW(), NOW(), NOW()
);

-- =================================================================
-- 6. UPDATE CONTENT USAGE COUNTS FOR MEDIA
-- =================================================================

-- Update media usage counts based on content associations
UPDATE media_files SET usage_count = 1 WHERE id = 2; -- tech news image used in content
UPDATE media_files SET usage_count = 3 WHERE id = 3; -- profile photo used in multiple places  
UPDATE media_files SET usage_count = 2 WHERE id = 4; -- tutorial screenshot used in tutorial
UPDATE media_files SET usage_count = 1 WHERE id = 5; -- travel photo used in travel story

-- =================================================================
-- 7. UPDATE CONTENT SCHEDULED TIMES
-- =================================================================

-- Set scheduled time for the breaking news article
UPDATE contents 
SET scheduled_at = DATE_ADD(NOW(), INTERVAL 2 HOUR)
WHERE id = 5 AND status = 'SCHEDULED';

-- =================================================================
-- 8. VERIFY DATA INTEGRITY
-- =================================================================

-- Show summary of created data
SELECT 'TENANTS' as entity, COUNT(*) as count FROM tenants
UNION ALL
SELECT 'USERS' as entity, COUNT(*) as count FROM users
UNION ALL  
SELECT 'CONTENT_TYPES' as entity, COUNT(*) as count FROM content_types
UNION ALL
SELECT 'CONTENTS' as entity, COUNT(*) as count FROM contents
UNION ALL
SELECT 'MEDIA_FILES' as entity, COUNT(*) as count FROM media_files;

-- =================================================================
-- DUMMY DATA SUMMARY
-- =================================================================

/*
Created Test Data:
- 5 Tenants (various industries and languages)
- 7 Users (different roles and tenants)
- 5 Content Types (news, tutorials, reviews, travel, corporate)
- 5 Contents (published, draft, scheduled - multi-language)
- 6 Media Files (images, logos, screenshots with metadata)

Test Scenarios Covered:
✅ Multi-tenant architecture
✅ Multi-language content (TR/EN)
✅ Different user roles and permissions
✅ Content workflow (draft → published → scheduled)
✅ Media management with categorization
✅ SEO-friendly content structure
✅ Parent-child content relationships (translations)
✅ Tenant status variations (active, pending, suspended)

Ready for Postman API Testing! 🚀
*/