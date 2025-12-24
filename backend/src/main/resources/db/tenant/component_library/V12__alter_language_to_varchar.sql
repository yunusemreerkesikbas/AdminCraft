-- V12: Alter language column to VARCHAR to support all languages
-- Previously defined as ENUM('TR', 'EN'), which causes truncation for other languages like 'ES'

ALTER TABLE component_i18n MODIFY COLUMN language VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE component_entry_i18n MODIFY COLUMN language VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
