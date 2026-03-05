-- Seed modules catalog
-- Repeatable migration: runs on every checksum change
-- Uses ON DUPLICATE KEY UPDATE to avoid cascade-deleting tenant_modules rows

INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
VALUES ('core', 'Core Module', 'core', '1.0.0', NULL, TRUE,
        'Core tenant capabilities umbrella: users, roles, sites, navigation, pages, media, and component management.')
AS new_vals
ON DUPLICATE KEY UPDATE
    name = new_vals.name,
    type = new_vals.type,
    version = new_vals.version,
    description = new_vals.description;

INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
VALUES ('pagebuilder', 'Page Builder', 'core', '1.0.0', '["core"]', FALSE,
        'Core capability: visual page builder with multi-language support.')
AS new_vals
ON DUPLICATE KEY UPDATE
    name = new_vals.name,
    type = new_vals.type,
    version = new_vals.version,
    description = new_vals.description;

INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
VALUES ('media', 'Media Library', 'core', '1.0.0', '["core"]', FALSE,
        'Core capability: media file management with responsive media support.')
AS new_vals
ON DUPLICATE KEY UPDATE
    name = new_vals.name,
    type = new_vals.type,
    version = new_vals.version,
    description = new_vals.description;

INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
VALUES ('component_library', 'Component Library', 'core', '1.0.0', '["core"]', FALSE,
        'Core capability: reusable component and entry management.')
AS new_vals
ON DUPLICATE KEY UPDATE
    name = new_vals.name,
    type = new_vals.type,
    version = new_vals.version,
    description = new_vals.description;

INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
VALUES ('product', 'Product Catalog', 'b2c', '1.0.0', '["core"]', FALSE,
        'Optional product catalog management module.')
AS new_vals
ON DUPLICATE KEY UPDATE
    name = new_vals.name,
    type = new_vals.type,
    version = new_vals.version,
    description = new_vals.description;

INSERT INTO modules_catalog (code, name, type, version, deps, enabled_by_default, description)
VALUES ('mail_marketing', 'Mail Marketing', 'b2c', '1.0.0', '["core"]', FALSE,
        'Optional tenant newsletter and announcement mailing capability with tenant-owned provider credentials.')
AS new_vals
ON DUPLICATE KEY UPDATE
    name = new_vals.name,
    type = new_vals.type,
    version = new_vals.version,
    description = new_vals.description;
