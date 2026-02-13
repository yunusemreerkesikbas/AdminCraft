-- Add reCAPTCHA columns to sites table

ALTER TABLE sites ADD COLUMN recaptcha_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE sites ADD COLUMN recaptcha_site_key VARCHAR(255);
ALTER TABLE sites ADD COLUMN recaptcha_secret_key_encrypted TEXT;
ALTER TABLE sites ADD COLUMN recaptcha_threshold DECIMAL(3,2) DEFAULT 0.5;
CREATE INDEX idx_sites_recaptcha_enabled ON sites(recaptcha_enabled);
