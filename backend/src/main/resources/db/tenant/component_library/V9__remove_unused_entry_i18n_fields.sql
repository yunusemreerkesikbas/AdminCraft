-- Remove unused button/image fields from component_entry_i18n
-- These fields exist in DB but are not used by frontend

ALTER TABLE component_entry_i18n
    DROP COLUMN image_url,
    DROP COLUMN button_text,
    DROP COLUMN button_url;
