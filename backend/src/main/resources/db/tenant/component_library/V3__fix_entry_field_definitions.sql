-- Fix entry_field_definitions schema to match EntryFieldDefinition entity
-- Sprint 25: Remove unused label columns and add migration tracking fields

ALTER TABLE entry_field_definitions 
    DROP COLUMN label_tr,
    DROP COLUMN label_en,
    ADD COLUMN migration_version VARCHAR(50) NULL AFTER field_type,
    ADD COLUMN applied_at TIMESTAMP NULL AFTER migration_version;

