INSERT INTO config_properties (config_key, config_value, is_secret, updated_by)
SELECT
    'security.recaptcha.enabled',
    CASE WHEN recaptcha_enabled THEN 'true' ELSE 'false' END,
    FALSE,
    updated_by
FROM sites
ORDER BY id
LIMIT 1
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    is_secret = VALUES(is_secret),
    updated_by = VALUES(updated_by);

INSERT INTO config_properties (config_key, config_value, is_secret, updated_by)
SELECT
    'security.recaptcha.site_key',
    recaptcha_site_key,
    FALSE,
    updated_by
FROM sites
WHERE recaptcha_site_key IS NOT NULL AND TRIM(recaptcha_site_key) <> ''
ORDER BY id
LIMIT 1
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    is_secret = VALUES(is_secret),
    updated_by = VALUES(updated_by);

INSERT INTO config_properties (config_key, config_value, is_secret, updated_by)
SELECT
    'security.recaptcha.secret_key',
    recaptcha_secret_key_encrypted,
    TRUE,
    updated_by
FROM sites
WHERE recaptcha_secret_key_encrypted IS NOT NULL AND TRIM(recaptcha_secret_key_encrypted) <> ''
ORDER BY id
LIMIT 1
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    is_secret = VALUES(is_secret),
    updated_by = VALUES(updated_by);

ALTER TABLE sites
    DROP INDEX idx_sites_recaptcha_enabled;

ALTER TABLE sites
    DROP COLUMN recaptcha_enabled,
    DROP COLUMN recaptcha_site_key,
    DROP COLUMN recaptcha_secret_key_encrypted,
    DROP COLUMN recaptcha_threshold;
