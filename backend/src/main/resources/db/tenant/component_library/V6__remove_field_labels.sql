ALTER TABLE entry_field_definitions 
  DROP COLUMN label_tr,
  DROP COLUMN label_en,
  ADD COLUMN migration_version VARCHAR(50) AFTER field_type,
  ADD COLUMN applied_at TIMESTAMP NULL AFTER migration_version;

