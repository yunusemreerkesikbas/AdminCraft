-- Remove code, icon, and is_system columns from component_types table
ALTER TABLE component_types
DROP COLUMN code,
DROP COLUMN icon,
DROP COLUMN is_system;
