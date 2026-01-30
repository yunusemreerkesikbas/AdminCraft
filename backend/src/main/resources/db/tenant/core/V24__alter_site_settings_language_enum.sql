-- V24: Alter language column to VARCHAR to support all languages
-- Previously defined as ENUM('TR', 'EN'), which causes truncation for other languages like 'ES'

ALTER TABLE site_settings MODIFY COLUMN language VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'NULL for global settings';
