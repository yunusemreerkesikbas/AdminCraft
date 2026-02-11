ALTER TABLE platform_settings
    ADD COLUMN two_factor_policy ENUM('DISABLED', 'REQUIRED') NOT NULL DEFAULT 'DISABLED' AFTER email_from_name,
    ADD COLUMN recaptcha_enabled BOOLEAN NOT NULL DEFAULT FALSE AFTER two_factor_policy,
    ADD COLUMN recaptcha_site_key VARCHAR(255) NULL AFTER recaptcha_enabled,
    ADD COLUMN recaptcha_secret_key_encrypted TEXT NULL AFTER recaptcha_site_key,
    ADD COLUMN recaptcha_threshold DECIMAL(3,2) NOT NULL DEFAULT 0.50 AFTER recaptcha_secret_key_encrypted;

CREATE INDEX idx_platform_settings_recaptcha_enabled ON platform_settings(recaptcha_enabled);
