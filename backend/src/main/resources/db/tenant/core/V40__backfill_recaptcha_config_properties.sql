INSERT INTO config_properties (config_key, config_value, is_secret, updated_by)
SELECT
  'security.recaptcha.enabled',
  CASE
    WHEN recaptcha_enabled IS NULL OR recaptcha_enabled = 0 THEN 'false'
    ELSE 'true'
  END,
  FALSE,
  updated_by
FROM sites
ORDER BY id ASC
LIMIT 1;

INSERT INTO config_properties (config_key, config_value, is_secret, updated_by)
SELECT
  'security.recaptcha.site_key',
  recaptcha_site_key,
  FALSE,
  updated_by
FROM sites
ORDER BY id ASC
LIMIT 1;

INSERT INTO config_properties (config_key, config_value, is_secret, updated_by)
SELECT
  'security.recaptcha.secret_key',
  recaptcha_secret_key_encrypted,
  TRUE,
  updated_by
FROM sites
ORDER BY id ASC
LIMIT 1;

INSERT INTO config_properties (config_key, config_value, is_secret, updated_by)
SELECT
  'security.recaptcha.threshold',
  IFNULL(CAST(recaptcha_threshold AS CHAR), '0.5'),
  FALSE,
  updated_by
FROM sites
ORDER BY id ASC
LIMIT 1;

