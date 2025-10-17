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
    'Visual page builder with multi-language support: pages, page_i18n, categories, sections, blocks.'
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

-- Content Management module
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'content', 
    'Content Management', 
    'core', 
    '1.0.0', 
    '["core"]',
    FALSE,
    'Generic content types and contents with multi-language support.'
);

-- Media Library module
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'media', 
    'Media Library', 
    'core', 
    '1.0.0', 
    '["core"]',
    FALSE,
    'Media file management with multi-language alt text support.'
);

-- B2C E-commerce modules (future expansion)
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'b2c_products', 
    'B2C Products', 
    'b2c', 
    '1.0.0', 
    '["core", "media"]',
    FALSE,
    'Product catalog for B2C e-commerce.'
);

INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'b2c_orders', 
    'B2C Orders', 
    'b2c', 
    '1.0.0', 
    '["core", "b2c_products"]',
    FALSE,
    'Order management for B2C e-commerce.'
);

-- B2B modules (future expansion)
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'b2b_quotes', 
    'B2B Quotes', 
    'b2b', 
    '1.0.0', 
    '["core"]',
    FALSE,
    'Quote request and management for B2B.'
);

INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description) 
VALUES (
    'b2b_contracts', 
    'B2B Contracts', 
    'b2b', 
    '1.0.0', 
    '["core", "b2b_quotes"]',
    FALSE,
    'Contract management for B2B relationships.'
);


