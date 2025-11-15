-- Seed data for component types
-- Repeatable migration: runs on every checksum change

-- Header component type (upsert - safe for repeated runs)
INSERT INTO component_types (
    uuid,
    uid,
    code,
    name,
    category,
    icon,
    is_system
) VALUES (
    UUID(),
    'comp-type-header',
    'header',
    'Header',
    'navigation',
    'header',
    TRUE
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    category = VALUES(category),
    icon = VALUES(icon);

-- Footer component type (upsert - safe for repeated runs)
INSERT INTO component_types (
    uuid,
    uid,
    code,
    name,
    category,
    icon,
    is_system
) VALUES (
    UUID(),
    'comp-type-footer',
    'footer',
    'Footer',
    'navigation',
    'footer',
    TRUE
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    category = VALUES(category),
    icon = VALUES(icon);
