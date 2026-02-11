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
    'Core tenant capabilities umbrella: users, roles, sites, navigation, pages, media, and component management.'
);

-- Core capability module: Page Builder
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
VALUES (
    'pagebuilder',
    'Page Builder',
    'core',
    '1.0.0',
    '["core"]',
    FALSE,
    'Core capability: visual page builder with multi-language support.'
);

-- Core capability module: Media Library
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
VALUES (
    'media',
    'Media Library',
    'core',
    '1.0.0',
    '["core"]',
    FALSE,
    'Core capability: media file management with responsive media support.'
);

-- Core capability module: Component Library
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
VALUES (
    'component_library',
    'Component Library',
    'core',
    '1.0.0',
    '["core"]',
    FALSE,
    'Core capability: reusable component and entry management.'
);

-- Product Catalog module (optional)
INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
VALUES (
    'product',
    'Product Catalog',
    'b2c',
    '1.0.0',
    '["core"]',
    FALSE,
    'Optional product catalog management module.'
);
