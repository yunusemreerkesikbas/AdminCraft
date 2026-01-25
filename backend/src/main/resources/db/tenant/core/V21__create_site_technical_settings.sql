-- V21: Create site_technical_settings table
-- Stores technical configuration for sites (robots.txt, scripts, verification codes)

CREATE TABLE site_technical_settings (
    site_id BIGINT PRIMARY KEY COMMENT 'FK to sites table, 1-1 relationship',

    -- Search Engine Settings
    robots_txt TEXT NULL COMMENT 'Custom robots.txt content',
    sitemap_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether sitemap generation is enabled',
    indexing_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether search engine indexing is allowed',

    -- Verification Codes
    google_verification VARCHAR(100) NULL COMMENT 'Google Search Console verification code',
    bing_verification VARCHAR(100) NULL COMMENT 'Bing Webmaster Tools verification code',
    yandex_verification VARCHAR(100) NULL COMMENT 'Yandex Webmaster verification code',

    -- Custom Scripts
    head_scripts TEXT NULL COMMENT 'Scripts to inject in <head> section',
    body_start_scripts TEXT NULL COMMENT 'Scripts to inject at start of <body>',
    body_end_scripts TEXT NULL COMMENT 'Scripts to inject at end of <body>',

    -- Cookie Consent
    cookie_consent_enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether cookie consent banner is enabled',
    cookie_consent_text TEXT NULL COMMENT 'Custom cookie consent message',

    -- Timestamps & Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,

    CONSTRAINT fk_site_technical_settings_site
        FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Technical settings for sites (robots.txt, scripts, verification)';

-- Insert default settings for existing sites
INSERT INTO site_technical_settings (site_id, sitemap_enabled, indexing_enabled, cookie_consent_enabled)
SELECT id, TRUE, TRUE, FALSE FROM sites
ON DUPLICATE KEY UPDATE site_id = site_id;
