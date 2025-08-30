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
    '$2a$12$zw075tesorhqMJLRVt1RQuiKjBfrex9khteRLv5oMrD61XXlFzHjy', -- password: admin123
    'Ahmet Yilmaz', 'TENANT_ADMIN', 'TR', 1,
    TRUE, TRUE, 'Genel Mudur', '+90 532 123 4567',
    NOW(), NOW()
),
(
    'editor@democompany.com',
    '$2a$12$zw075tesorhqMJLRVt1RQuiKjBfrex9khteRLv5oMrD61XXlFzHjy', -- password: admin123
    'Ayse Kaya', 'EDITOR', 'TR', 1,
    TRUE, TRUE, 'Icerik Editoru', '+90 532 234 5678',
    NOW(), NOW() - INTERVAL 2 HOUR
),
(
    'viewer@democompany.com',
    '$2a$12$zw075tesorhqMJLRVt1RQuiKjBfrex9khteRLv5oMrD61XXlFzHjy', -- password: admin123
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
    '$2a$12$zw075tesorhqMJLRVt1RQuiKjBfrex9khteRLv5oMrD61XXlFzHjy', -- password: admin123
    'John Smith', 'TENANT_ADMIN', 'EN', 2,
    TRUE, TRUE, 'CEO & Founder',
    NOW(), NOW()
),
(
    'content@techstart.com',
    '$2a$12$zw075tesorhqMJLRVt1RQuiKjBfrex9khteRLv5oMrD61XXlFzHjy', -- password: admin123
    'Sarah Johnson', 'EDITOR', 'EN', 2,
    TRUE, TRUE, 'Content Manager',
    NOW(), NOW() - INTERVAL 1 HOUR
);



-- Default password for all users: admin123

-- =====================================================
-- 5. INSERT DUMMY PAGE BUILDER PAGES
-- =====================================================

-- Sample pages for Tenant 1 (Demo Company) for Page Builder
INSERT IGNORE INTO pages (
  tenant_id, title, slug, status, language, category_id,
  meta_title, meta_description, canonical_url,
  published_at, created_at, updated_at, created_by
) VALUES
(
  1, 'Ana Sayfa (PB)', 'ana-sayfa', 'PUBLISHED', 'TR', NULL,
  'Demo Şirketi - Ana Sayfa (PB)',
  'Page Builder ile oluşturulmuş ana sayfa',
  NULL,
  NOW(), NOW(), NOW(), 1
),
(
  1, 'Hakkımızda (PB)', 'hakkimizda', 'DRAFT', 'TR', NULL,
  'Demo Şirketi - Hakkımızda (PB)',
  'Page Builder ile oluşturulmuş hakkımızda sayfası',
  NULL,
  NULL, NOW(), NOW(), 1
),
(
  1, 'Home (PB)', 'home', 'PUBLISHED', 'EN', NULL,
  'Demo Company - Home (PB)',
  'Homepage created with Page Builder',
  NULL,
  NOW(), NOW(), NOW(), 1
);

-- =====================================================
-- 6. INSERT DUMMY PAGE CATEGORIES (Sprint 5)
-- =====================================================

-- Root categories for tenant 1 (TR default)
INSERT IGNORE INTO page_categories (tenant_id, name, slug, parent_id, path, level, sort_order, status)
VALUES
  (1, 'Kurumsal', 'kurumsal', NULL, '/kurumsal', 1, 0, 'ACTIVE'),
  (1, 'Hizmetler', 'hizmetler', NULL, '/hizmetler', 1, 1, 'ACTIVE');

-- Children under Kurumsal
INSERT IGNORE INTO page_categories (tenant_id, name, slug, parent_id, path, level, sort_order, status)
VALUES
  (1, 'Hakkimizda', 'hakkimizda', 1, '/kurumsal/hakkimizda', 2, 0, 'ACTIVE'),
  (1, 'Vizyon-Misyon', 'vizyon-misyon', 1, '/kurumsal/vizyon-misyon', 2, 1, 'ACTIVE');

-- Translations for categories (EN)
INSERT IGNORE INTO page_category_translations (tenant_id, category_id, language, name, slug, description)
VALUES
  (1, 1, 'EN', 'Corporate', 'corporate', 'Corporate root category'),
  (1, 2, 'EN', 'Services', 'services', 'Services root category'),
  (1, 3, 'EN', 'About Us', 'about-us', 'About company'),
  (1, 4, 'EN', 'Vision & Mission', 'vision-mission', 'Vision and Mission');

-- Attach existing sample pages to a valid category (if NULL)
UPDATE pages SET category_id = 1 WHERE tenant_id = 1 AND category_id IS NULL;