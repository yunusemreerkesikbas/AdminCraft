-- Seed data for component types
-- Repeatable migration: runs on every checksum change

-- Clear existing component types (for repeatable execution)
-- Note: This will fail if there are existing components referencing these types
-- In production, you may want to use more sophisticated migration logic
DELETE FROM component_types WHERE is_system = TRUE;

-- Header component type
INSERT INTO component_types (
    uuid,
    uid,
    code,
    name,
    category,
    icon,
    is_system,
    created_by
) VALUES (
    UUID(),
    'comp-type-header',
    'header',
    'Header',
    'navigation',
    'header',
    TRUE,
    1  -- System user
);

-- Footer component type
INSERT INTO component_types (
    uuid,
    uid,
    code,
    name,
    category,
    icon,
    is_system,
    created_by
) VALUES (
    UUID(),
    'comp-type-footer',
    'footer',
    'Footer',
    'navigation',
    'footer',
    TRUE,
    1  -- System user
);
