-- AdminCraft CMS Dummy Data Script
-- This file will be automatically executed by Spring Boot after table creation

-- =====================================================
-- 1. INSERT DUMMY TENANTS
-- =====================================================

-- Demo Company Tenant
INSERT IGNORE INTO tenants (
    subdomain, company_name, database_name, status, admin_email, admin_name,
    default_language, custom_domain, ssl_enabled,
    timezone, currency, storage_used_mb,
    created_at, activated_at
) VALUES (
    'demo-company', 'Demo Sirketi A.S.', 'tenant_demo_company', 'ACTIVE',
    'admin@democompany.com', 'Ahmet Yilmaz',
    'TR', 'democompany.com', TRUE,
    'Europe/Istanbul', 'TRY', 256,
    NOW(), NOW()
);

-- Tech Startup Tenant
INSERT IGNORE INTO tenants (
    subdomain, company_name, database_name, status, admin_email, admin_name,
    default_language, ssl_enabled,
    timezone, currency, storage_used_mb,
    created_at, activated_at
) VALUES (
    'tech-startup', 'TechStart Innovations', 'tenant_tech_startup', 'ACTIVE',
    'admin@techstart.com', 'John Smith',
    'EN', TRUE,
    'Europe/Istanbul', 'USD', 128,
    NOW(), NOW()
);

-- Insert supported languages for tenants
INSERT IGNORE INTO tenant_supported_languages (tenant_id, language) VALUES (1, 'TR');
INSERT IGNORE INTO tenant_supported_languages (tenant_id, language) VALUES (1, 'EN');
INSERT IGNORE INTO tenant_supported_languages (tenant_id, language) VALUES (2, 'EN'); 
INSERT IGNORE INTO tenant_supported_languages (tenant_id, language) VALUES (2, 'TR');

-- =====================================================
-- 2. INSERT DUMMY USERS
-- =====================================================

-- Demo Company Users
INSERT IGNORE INTO users (
    email, password_hash, full_name, role, preferred_language, tenant_id,
    email_verified, is_active, job_title, phone,
    created_at, last_login_at
) VALUES 
(
    'admin@democompany.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfP5fqnQq1dP0m6', -- password: admin123
    'Ahmet Yilmaz', 'TENANT_ADMIN', 'TR', 1,
    TRUE, TRUE, 'Genel Mudur', '+90 532 123 4567',
    NOW(), NOW()
),
(
    'editor@democompany.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfP5fqnQq1dP0m6', -- password: admin123
    'Ayse Kaya', 'EDITOR', 'TR', 1,
    TRUE, TRUE, 'Icerik Editoru', '+90 532 234 5678',
    NOW(), NOW() - INTERVAL 2 HOUR
),
(
    'viewer@democompany.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfP5fqnQq1dP0m6', -- password: admin123
    'Mehmet Ozkan', 'VIEWER', 'TR', 1,
    TRUE, TRUE, 'Icerik Goruntuleyicisi', '+90 532 345 6789',
    NOW(), NOW() - INTERVAL 5 HOUR
);

-- Tech Startup Users  
INSERT IGNORE INTO users (
    email, password_hash, full_name, role, preferred_language, tenant_id,
    email_verified, is_active, job_title,
    created_at, last_login_at
) VALUES 
(
    'admin@techstart.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfP5fqnQq1dP0m6', -- password: admin123
    'John Smith', 'TENANT_ADMIN', 'EN', 2,
    TRUE, TRUE, 'CEO & Founder',
    NOW(), NOW()
),
(
    'content@techstart.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfP5fqnQq1dP0m6', -- password: admin123
    'Sarah Johnson', 'EDITOR', 'EN', 2,
    TRUE, TRUE, 'Content Manager',
    NOW(), NOW() - INTERVAL 1 HOUR
);

-- =====================================================
-- 3. INSERT CONTENT TYPES
-- =====================================================

-- Content Types for Demo Company (Turkish focused)
INSERT IGNORE INTO content_types (
    name, display_name, display_name_tr, display_name_en, 
    description_tr, description_en, fields, tenant_id, created_by,
    supports_multi_language, supports_seo, supports_scheduling
) VALUES 
(
    'page', 'Sayfa', 'Sayfa', 'Page',
    'Statik web sayfalari icin kullanilir', 'Used for static web pages',
    '{"title": {"type": "text", "required": true}, "content": {"type": "richtext", "required": true}, "featured_image": {"type": "media", "required": false}}',
    1, 1, TRUE, TRUE, FALSE
),
(
    'blog', 'Blog Yazısı', 'Blog Yazısı', 'Blog Post',
    'Blog yazilari ve makaleler icin kullanilir', 'Used for blog posts and articles',
    '{"title": {"type": "text", "required": true}, "excerpt": {"type": "textarea", "required": false}, "content": {"type": "richtext", "required": true}, "featured_image": {"type": "media", "required": false}, "tags": {"type": "tags", "required": false}}',
    1, 1, TRUE, TRUE, TRUE
);

-- Content Types for Tech Startup (English focused)
INSERT IGNORE INTO content_types (
    name, display_name, display_name_tr, display_name_en,
    description_tr, description_en, fields, tenant_id, created_by,
    supports_multi_language, supports_seo, supports_scheduling
) VALUES 
(
    'page', 'Page', 'Sayfa', 'Page',
    'Statik web sayfalari icin kullanilir', 'Used for static web pages',
    '{"title": {"type": "text", "required": true}, "content": {"type": "richtext", "required": true}, "hero_image": {"type": "media", "required": false}}',
    2, 4, TRUE, TRUE, FALSE
),
(
    'news', 'News Article', 'Haber Makalesi', 'News Article',
    'Sirket haberleri ve duyurular icin kullanilir', 'Used for company news and announcements',
    '{"headline": {"type": "text", "required": true}, "summary": {"type": "textarea", "required": true}, "content": {"type": "richtext", "required": true}, "featured_image": {"type": "media", "required": false}, "publish_date": {"type": "datetime", "required": true}}',
    2, 4, TRUE, TRUE, TRUE
);

-- =====================================================
-- 4. INSERT DUMMY CONTENTS
-- =====================================================

-- Demo Company Contents (Turkish)
INSERT IGNORE INTO contents (
    title, slug, data, status, language, content_type_id, tenant_id, 
    meta_title, meta_description, created_by, published_at
) VALUES 
(
    'Ana Sayfa', 'ana-sayfa',
    '{"title": "Demo Sirketi A.S.ye Hos Geldiniz", "content": "<h1>Hos Geldiniz</h1><p>Demo Sirketi olarak sizlere en kaliteli hizmeti sunmaktan mutluluk duyuyoruz. 25 yillik tecrubemizle sektorde oncu konumdayiz.</p><h2>Hizmetlerimiz</h2><ul><li>Profesyonel Danismanlk</li><li>Teknik Destek</li><li>Egitim Hizmetleri</li></ul>"}',
    'PUBLISHED', 'TR', 1, 1,
    'Demo Sirketi - Ana Sayfa', 'Demo Sirketi A.S. resmi web sitesi. Profesyonel hizmetler ve cozumler.',
    1, NOW()
),
(
    'Hakkimizda', 'hakkimizda',
    '{"title": "Hakkimizda", "content": "<h1>Sirket Tarihcesi</h1><p>1999 yilinda kurulan Demo Sirketi, teknoloji ve inovasyon alaninda Turkiyenin onde gelen firmalarindan biridir.</p><h2>Misyonumuz</h2><p>Musterilerimize en iyi hizmeti sunarak, sektorde lider olmak.</p><h2>Vizyonumuz</h2><p>Teknoloji ile gelecegi sekillendirmek.</p>"}',
    'PUBLISHED', 'TR', 1, 1,
    'Hakkimizda - Demo Sirketi', 'Demo Sirketi hakkinda bilgiler, misyon ve vizyon.',
    1, NOW() - INTERVAL 1 DAY
),
(
    'Ilk Blog Yazimiz', 'ilk-blog-yazimiz',
    '{"title": "Demo Sirketi Bloguna Hos Geldiniz", "excerpt": "Blog sayfamizda sektorle ilgili guncel bilgileri paylasacagiz.", "content": "<h1>Blogumuz Acildi!</h1><p>Merhaba degerli okuyucular,</p><p>Demo Sirketi olarak blog sayfamizi acmanin heyecanini yasiyoruz. Bu platformda:</p><ul><li>Sektorel gelismeler</li><li>Teknoloji haberleri</li><li>Sirket duyurulari</li><li>Uzman gorusleri</li></ul><p>hakkinda yazilar paylasacagiz.</p>", "tags": ["blog", "duyuru", "hosgeldin"]}',
    'PUBLISHED', 'TR', 2, 1,
    'Ilk Blog Yazimiz - Demo Sirketi', 'Demo Sirketi blog sayfasi acildi. Sektorel icerikler ve guncel bilgiler.',
    2, NOW() - INTERVAL 2 DAY
);

-- Tech Startup Contents (English)
INSERT IGNORE INTO contents (
    title, slug, data, status, language, content_type_id, tenant_id,
    meta_title, meta_description, created_by, published_at
) VALUES 
(
    'Welcome to TechStart', 'welcome-techstart',
    '{"title": "Innovation Starts Here", "content": "<h1>Welcome to TechStart Innovations</h1><p>We are a cutting-edge technology startup focused on revolutionary solutions for tomorrow\'s challenges.</p><h2>What We Do</h2><ul><li>AI & Machine Learning</li><li>Blockchain Solutions</li><li>Mobile App Development</li><li>Cloud Services</li></ul>"}',
    'PUBLISHED', 'EN', 3, 2,
    'TechStart Innovations - Innovation Starts Here', 'Revolutionary technology solutions for tomorrow\'s challenges.',
    4, NOW()
),
(
    'TechStart Raises $5M Series A', 'techstart-raises-5m-series-a',
    '{"headline": "TechStart Raises $5M Series A Funding Round", "summary": "Leading VC firms invest in our revolutionary AI platform.", "content": "<h1>Major Milestone Achieved</h1><p>We are thrilled to announce that TechStart has successfully closed a $5M Series A funding round led by Innovation Ventures.</p><p>This funding will accelerate our product development and market expansion plans.</p>", "publish_date": "2024-01-15T10:00:00"}',
    'PUBLISHED', 'EN', 4, 2,
    'TechStart Raises $5M Series A Funding', 'TechStart successfully closes Series A funding round with leading investors.',
    5, NOW() - INTERVAL 3 DAY
);

-- Default password for all users: admin123