-- Extend language ENUM to support all languages in the system
-- This fixes the "Data truncated for column 'language'" error when using ES, RU, AR

-- page_i18n table
ALTER TABLE page_i18n 
MODIFY COLUMN language ENUM('TR', 'EN', 'ES', 'RU', 'AR') NOT NULL;

-- page_template_i18n table (if exists)
ALTER TABLE page_template_i18n 
MODIFY COLUMN language ENUM('TR', 'EN', 'ES', 'RU', 'AR') NOT NULL;
