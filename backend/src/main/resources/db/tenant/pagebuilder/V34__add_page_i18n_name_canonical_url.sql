-- Add missing columns to page_i18n for canonical_url and name
ALTER TABLE page_i18n ADD COLUMN name VARCHAR(200) NULL;
ALTER TABLE page_i18n ADD COLUMN canonical_url VARCHAR(255) NULL;

ALTER TABLE page_i18n ADD UNIQUE KEY idx_page_i18n_language_canonical_url (language, canonical_url);
