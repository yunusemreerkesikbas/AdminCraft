INSERT INTO platform_config_properties (uuid, uid, config_key, config_value, is_secret, updated_by)
SELECT
    UUID(),
    'platform-recaptcha-enabled',
    'platform.security.recaptcha.enabled',
    CASE WHEN recaptcha_enabled THEN 'true' ELSE 'false' END,
    FALSE,
    NULL
FROM platform_settings
WHERE id = 1
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    is_secret = VALUES(is_secret),
    updated_by = VALUES(updated_by);

INSERT INTO platform_config_properties (uuid, uid, config_key, config_value, is_secret, updated_by)
SELECT
    UUID(),
    'platform-recaptcha-site-key',
    'platform.security.recaptcha.site_key',
    recaptcha_site_key,
    FALSE,
    NULL
FROM platform_settings
WHERE id = 1
  AND recaptcha_site_key IS NOT NULL
  AND TRIM(recaptcha_site_key) <> ''
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    is_secret = VALUES(is_secret),
    updated_by = VALUES(updated_by);

INSERT INTO platform_config_properties (uuid, uid, config_key, config_value, is_secret, updated_by)
SELECT
    UUID(),
    'platform-recaptcha-secret-key',
    'platform.security.recaptcha.secret_key',
    recaptcha_secret_key_encrypted,
    TRUE,
    NULL
FROM platform_settings
WHERE id = 1
  AND recaptcha_secret_key_encrypted IS NOT NULL
  AND TRIM(recaptcha_secret_key_encrypted) <> ''
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    is_secret = VALUES(is_secret),
    updated_by = VALUES(updated_by);

ALTER TABLE platform_settings
    DROP COLUMN recaptcha_enabled,
    DROP COLUMN recaptcha_site_key,
    DROP COLUMN recaptcha_secret_key_encrypted,
    DROP COLUMN recaptcha_threshold;
