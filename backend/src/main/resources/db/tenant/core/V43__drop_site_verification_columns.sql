-- V43: Drop search engine webmaster verification codes
ALTER TABLE site_technical_settings
    DROP COLUMN google_verification,
    DROP COLUMN bing_verification,
    DROP COLUMN yandex_verification;

