-- Backfill only UUID/UID empty strings

-- Pages
UPDATE pages SET uuid = UUID() WHERE uuid IS NULL OR uuid = '';
UPDATE pages SET uid = LOWER(REPLACE(UUID(), '-', '')) WHERE uid IS NULL OR TRIM(uid) = '';

-- Page i18n
UPDATE page_i18n SET uuid = UUID() WHERE uuid IS NULL OR uuid = '';
UPDATE page_i18n SET uid = LOWER(REPLACE(UUID(), '-', '')) WHERE uid IS NULL OR TRIM(uid) = '';
