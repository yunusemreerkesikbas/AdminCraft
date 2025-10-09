-- Migration 003: Page Categories i18n Alignment
-- Purpose: Transform page_categories to use BaseEntity pattern and create page_category_i18n table
-- Date: 2025-01-08

-- Step 1: Add BaseEntity fields to page_categories (created_at, updated_at only if they don't exist)
-- uuid, uid, created_by, updated_by already exist, only add created_at and updated_at
ALTER TABLE page_categories
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER updated_by,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- Step 2: Remove language-specific fields from page_categories (name, slug, path, level, status)
-- First backup data to page_category_i18n
-- Note: We'll create the i18n table first before removing columns

-- Step 3: Create page_category_i18n table
CREATE TABLE IF NOT EXISTS page_category_i18n (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  uuid VARCHAR(36) NOT NULL,
  uid VARCHAR(50) NOT NULL,
  tenant_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  language VARCHAR(10) NOT NULL,
  url VARCHAR(150),
  title VARCHAR(200),
  meta_title VARCHAR(60),
  meta_description VARCHAR(160),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  CONSTRAINT uk_page_category_i18n_cat_lang UNIQUE (tenant_id, category_id, language),
  CONSTRAINT uk_page_category_i18n_uid_tenant UNIQUE (tenant_id, uid),
  CONSTRAINT uk_page_category_i18n_url UNIQUE (tenant_id, language, url),
  
  INDEX idx_cat_i18n_category (category_id),
  INDEX idx_cat_i18n_tenant_lang (tenant_id, language),
  INDEX idx_cat_i18n_url (tenant_id, language, url),
  
  CONSTRAINT fk_page_category_i18n_category FOREIGN KEY (category_id) REFERENCES page_categories(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 4: Migrate existing data to page_category_i18n
-- Assuming existing data is in Turkish (TR)
INSERT INTO page_category_i18n (uuid, uid, tenant_id, category_id, language, url, title, active, updated_at)
SELECT
    UUID() as uuid,
    CONCAT('catitem_', LPAD(pc.id, 8, '0')) as uid,
    pc.tenant_id,
    pc.id as category_id,
    'TR' as language,
    pc.slug as url,
    pc.name as title,
    CASE WHEN pc.status = 'ACTIVE' THEN TRUE ELSE FALSE END as active,
    NOW() as updated_at
FROM page_categories pc
WHERE EXISTS (SELECT 1 FROM page_categories WHERE id = pc.id)
ON DUPLICATE KEY UPDATE url = VALUES(url), title = VALUES(title);

-- Step 5: active and style_classes already exist, skip this step

-- Step 6: Backfill uuid and uid for existing categories if null
UPDATE page_categories 
SET uuid = UUID() 
WHERE uuid IS NULL OR uuid = '';

UPDATE page_categories
SET uid = CONCAT('catitem_', LPAD(id, 8, '0'))
WHERE uid IS NULL OR uid = '';

UPDATE page_categories 
SET created_at = NOW() 
WHERE created_at IS NULL;

UPDATE page_categories 
SET updated_at = NOW() 
WHERE updated_at IS NULL;

-- Step 7: Make uuid and uid NOT NULL and UNIQUE
ALTER TABLE page_categories
MODIFY COLUMN uuid VARCHAR(36) NOT NULL,
MODIFY COLUMN uid VARCHAR(50) NOT NULL;

ALTER TABLE page_categories
DROP INDEX IF EXISTS uk_page_category_uuid,
DROP INDEX IF EXISTS uk_page_category_uid;

ALTER TABLE page_categories
ADD CONSTRAINT IF NOT EXISTS uk_page_category_uuid UNIQUE (uuid),
ADD CONSTRAINT IF NOT EXISTS uk_page_category_uid UNIQUE (uid);

-- Step 8: Drop old language-specific columns
-- WARNING: Only run this after confirming data migration
ALTER TABLE page_categories 
DROP COLUMN IF EXISTS name,
DROP COLUMN IF EXISTS slug,
DROP COLUMN IF EXISTS path,
DROP COLUMN IF EXISTS level,
DROP COLUMN IF EXISTS status;

-- Step 9: Drop old unique constraint on slug
ALTER TABLE page_categories 
DROP INDEX IF EXISTS uk_page_category_slug_tenant;

-- Step 10: Drop old indexes
ALTER TABLE page_categories 
DROP INDEX IF EXISTS idx_page_category_path;

-- Migration complete
-- Categories now follow BaseEntity pattern with language-agnostic base table
-- and language-specific page_category_i18n table

