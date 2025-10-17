-- Seed sample pages for demo purposes
-- Repeatable migration: runs on every checksum change

-- Note: This seed requires at least one user to exist (created_by constraint)
-- First user is typically created during tenant provisioning

-- Check if sample pages already exist to avoid duplicates
SET @sample_exists = (SELECT COUNT(*) FROM pages WHERE uid = 'homepage');

-- Only seed if no sample pages exist
INSERT INTO pages (uid, uuid, status, is_home, sort_order, created_by, updated_by)
SELECT 'homepage', UUID(), 'PUBLISHED', TRUE, 0, 
       (SELECT id FROM users ORDER BY id LIMIT 1),
       (SELECT id FROM users ORDER BY id LIMIT 1)
WHERE @sample_exists = 0;

-- Turkish translation for homepage
INSERT INTO page_i18n (page_id, uid, uuid, language, url_path, title, subtitle, meta_title, meta_description, description, status, published_at)
SELECT 
    (SELECT id FROM pages WHERE uid = 'homepage'),
    'homepage-tr',
    UUID(),
    'TR',
    '/ana-sayfa',
    'Ana Sayfa',
    'Hoş Geldiniz',
    'Ana Sayfa',
    'Hoş geldiniz sayfası',
    'Bu sayfa Page Builder ile oluşturulmuştur.',
    'PUBLISHED',
    NOW()
WHERE @sample_exists = 0;

-- English translation for homepage
INSERT INTO page_i18n (page_id, uid, uuid, language, url_path, title, subtitle, meta_title, meta_description, description, status, published_at)
SELECT 
    (SELECT id FROM pages WHERE uid = 'homepage'),
    'homepage-en',
    UUID(),
    'EN',
    '/home',
    'Home',
    'Welcome',
    'Home Page',
    'Welcome page',
    'This page was created with Page Builder.',
    'PUBLISHED',
    NOW()
WHERE @sample_exists = 0;


