-- Seed modules catalog
-- Repeatable migration: runs on every checksum change

-- Clear existing modules (for repeatable execution)
DELETE FROM modules_catalog;

-- Core module (required for all tenants)
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'core', 
    'Core Module', 
    'core', 
    '1.0.0', 
    NULL, 
    TRUE,
    'Essential tables: users, roles, site_settings. Required for all tenants.'
);

-- Page Builder module
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'pagebuilder', 
    'Page Builder', 
    'core', 
    '1.0.0', 
    '["core"]',
    TRUE,
    'Visual page builder with multi-language support. Includes pages, page_i18n, page_categories, sections, and blocks.'
);

-- Site Settings module
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'site_settings', 
    'Site Settings', 
    'core', 
    '1.0.0', 
    '["core"]',
    TRUE,
    'Global and language-specific site configuration settings.'
);

-- Media Library module
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'media', 
    'Media Library', 
    'core', 
    '1.0.0', 
    '["core"]',
    TRUE,
    'Media file management with multi-language alt text support.'
);


