-- Add reCAPTCHA columns to sites table
ALTER TABLE sites 
  ADD COLUMN recaptcha_enabled BOOLEAN DEFAULT FALSE,
  ADD COLUMN recaptcha_site_key VARCHAR(255),
  ADD COLUMN recaptcha_secret_key_encrypted TEXT,
  ADD COLUMN recaptcha_threshold DECIMAL(3,2) DEFAULT 0.5;

-- Add index for performance
CREATE INDEX idx_sites_recaptcha_enabled ON sites(recaptcha_enabled);
