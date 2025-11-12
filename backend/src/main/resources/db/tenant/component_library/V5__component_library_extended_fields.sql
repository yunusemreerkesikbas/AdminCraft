-- Component Library Module - Extended Fields Support
-- Sprint 2: Add dynamic extended fields for custom component type schemas
-- Enables component types to define additional fields beyond the standard base fields

-- Add extended_fields_schema to component_types
-- Stores JSON schema defining additional fields that can be configured for this component type
-- Example: { "i18n": [{ "key": "author", "type": "text", "label": "Author Name", "required": true, "maxLength": 100 }] }
ALTER TABLE component_types
ADD COLUMN extended_fields_schema JSON NULL COMMENT 'JSON schema defining additional custom fields for this component type (i18n and non-i18n)';

-- Add extended_data to components
-- Stores actual values for non-i18n extended fields defined in component_type.extended_fields_schema
-- Example: { "rating": 5, "featured": true }
ALTER TABLE components
ADD COLUMN extended_data JSON NULL COMMENT 'Values for non-i18n extended fields as defined in component_type.extended_fields_schema';

-- Add extended_localized_data to component_i18n
-- Stores language-specific values for i18n extended fields defined in component_type.extended_fields_schema
-- Example: { "author": "John Doe", "customTitle": "Custom localized title" }
ALTER TABLE component_i18n
ADD COLUMN extended_localized_data JSON NULL COMMENT 'Language-specific values for i18n extended fields as defined in component_type.extended_fields_schema';
