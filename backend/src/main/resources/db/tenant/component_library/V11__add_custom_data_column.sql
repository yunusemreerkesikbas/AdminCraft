-- Sprint 25: CMS Delivery API - Add custom_data JSON column
-- This column stores dynamic custom fields for component entries per language

ALTER TABLE component_entry_i18n
ADD COLUMN custom_data JSON DEFAULT NULL;

-- Add index for JSON queries if needed in the future
-- CREATE INDEX idx_entry_i18n_custom_data ON component_entry_i18n ((CAST(custom_data AS CHAR(512))));
