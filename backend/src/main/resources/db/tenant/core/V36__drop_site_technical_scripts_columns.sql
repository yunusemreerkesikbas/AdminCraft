-- V36: Drop custom script columns from site_technical_settings
-- Scripts feature removed from Site Dashboard Technical tab

ALTER TABLE site_technical_settings
    DROP COLUMN head_scripts,
    DROP COLUMN body_start_scripts,
    DROP COLUMN body_end_scripts;
