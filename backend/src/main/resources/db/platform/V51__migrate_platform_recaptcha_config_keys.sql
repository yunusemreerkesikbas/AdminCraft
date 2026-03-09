-- Move platform reCAPTCHA config keys into dedicated platform namespace.
-- Old (conflicting with tenant): security.recaptcha.*
-- New (platform-only):        platform.security.recaptcha.*

INSERT IGNORE INTO platform_management.platform_config_properties
    (config_key, config_value, is_secret, created_at, updated_at, updated_by)
SELECT
    'platform.security.recaptcha.enabled',
    p.config_value,
    p.is_secret,
    p.created_at,
    p.updated_at,
    p.updated_by
FROM platform_management.platform_config_properties p
WHERE p.config_key = 'security.recaptcha.enabled';

INSERT IGNORE INTO platform_management.platform_config_properties
    (config_key, config_value, is_secret, created_at, updated_at, updated_by)
SELECT
    'platform.security.recaptcha.site_key',
    p.config_value,
    p.is_secret,
    p.created_at,
    p.updated_at,
    p.updated_by
FROM platform_management.platform_config_properties p
WHERE p.config_key = 'security.recaptcha.site_key';

INSERT IGNORE INTO platform_management.platform_config_properties
    (config_key, config_value, is_secret, created_at, updated_at, updated_by)
SELECT
    'platform.security.recaptcha.secret_key',
    p.config_value,
    p.is_secret,
    p.created_at,
    p.updated_at,
    p.updated_by
FROM platform_management.platform_config_properties p
WHERE p.config_key = 'security.recaptcha.secret_key';

DELETE FROM platform_management.platform_config_properties
WHERE config_key IN (
    'security.recaptcha.enabled',
    'security.recaptcha.site_key',
    'security.recaptcha.secret_key'
);
