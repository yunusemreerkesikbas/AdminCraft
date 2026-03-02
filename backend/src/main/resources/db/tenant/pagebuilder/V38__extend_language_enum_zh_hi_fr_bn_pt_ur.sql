-- Extend language ENUM to include ZH, HI, FR, BN, PT, UR (matches domain Language enum)
-- Fixes "Data truncated for column 'language'" when saving page i18n for Chinese etc.

ALTER TABLE page_i18n
MODIFY COLUMN language ENUM('TR', 'EN', 'ES', 'RU', 'AR', 'ZH', 'HI', 'FR', 'BN', 'PT', 'UR') NOT NULL;

ALTER TABLE page_template_i18n
MODIFY COLUMN language ENUM('TR', 'EN', 'ES', 'RU', 'AR', 'ZH', 'HI', 'FR', 'BN', 'PT', 'UR') NOT NULL;
