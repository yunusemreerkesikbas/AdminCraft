-- Remove Extended Fields from Component Library
-- This migration removes the extended_fields_schema and extended_data columns
-- as we're moving to a different approach with component entries

ALTER TABLE component_types
DROP COLUMN IF EXISTS extended_fields_schema;

ALTER TABLE components
DROP COLUMN IF EXISTS extended_data;

ALTER TABLE component_i18n
DROP COLUMN IF EXISTS extended_localized_data;



