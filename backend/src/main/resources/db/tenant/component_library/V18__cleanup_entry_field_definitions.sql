-- Cleanup legacy columns after simplifying entry field definitions

ALTER TABLE entry_field_definitions
    DROP COLUMN migration_version,
    DROP COLUMN applied_at,
    DROP COLUMN is_required,
    DROP COLUMN max_length,
    DROP COLUMN min_value,
    DROP COLUMN max_value;
