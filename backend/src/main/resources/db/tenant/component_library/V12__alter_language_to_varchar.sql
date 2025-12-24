-- V9: Alter language column to VARCHAR to support all languages
-- Previously defined as ENUM('TR', 'EN'), which causes truncation for other languages like 'ES'

ALTER TABLE component_i18n MODIFY COLUMN language VARCHAR(10) NOT NULL;
ALTER TABLE component_entry_i18n MODIFY COLUMN language VARCHAR(10) NOT NULL;
