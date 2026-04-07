-- Cookie consent text is now stored per-language in site_settings table
-- (key: i18n.cookie.consent.text, type: I18N_TEXT)
ALTER TABLE site_technical_settings DROP COLUMN cookie_consent_text;
