-- Page Builder Schema (idempotent)

-- pages
CREATE TABLE IF NOT EXISTS pages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  slug VARCHAR(200) NOT NULL,
  status ENUM('DRAFT','PUBLISHED','ARCHIVED','SCHEDULED') NOT NULL DEFAULT 'DRAFT',
  language ENUM('TR','EN') NOT NULL,
  category_id BIGINT NULL,
  meta_title VARCHAR(60) NULL,
  meta_description VARCHAR(160) NULL,
  canonical_url VARCHAR(255) NULL,
  subtitle VARCHAR(200) NULL,
  style_classes VARCHAR(255) NULL,
  description LONGTEXT NULL,
  description_html LONGTEXT NULL,
  featured_image VARCHAR(500) NULL,
  published_at TIMESTAMP NULL,
  scheduled_at TIMESTAMP NULL,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by BIGINT NOT NULL,
  updated_by BIGINT NULL,
  CONSTRAINT uk_page_slug_tenant_lang UNIQUE (tenant_id, slug, language),
  INDEX idx_page_tenant (tenant_id),
  INDEX idx_page_slug (slug),
  INDEX idx_page_status (status),
  INDEX idx_page_language (language),
  INDEX idx_page_published_at (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- FK from pages to page_categories (idempotent)
SET @fk_exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pages'
    AND CONSTRAINT_NAME = 'fk_pages_category'
);
SET @sql_stmt = IF(@fk_exists = 0,
  'ALTER TABLE pages ADD CONSTRAINT fk_pages_category FOREIGN KEY (category_id) REFERENCES page_categories(id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- page_categories
CREATE TABLE IF NOT EXISTS page_categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(150) NOT NULL,
  parent_id BIGINT NULL,
  path VARCHAR(500) NULL,
  level INT NULL,
  sort_order INT DEFAULT 0,
  status ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  -- GÜVENLIK VE VERİ BÜTÜNLÜĞÜ CONSTRAINT'LERİ
  CONSTRAINT uk_page_category_slug_tenant UNIQUE (tenant_id, slug),
  CONSTRAINT chk_category_no_self_parent CHECK (id != parent_id),
  CONSTRAINT chk_category_level_positive CHECK (level > 0 AND level <= 50),
  CONSTRAINT chk_category_path_format CHECK (path REGEXP '^(/[a-z0-9-]+)+$' OR path = '/' OR path IS NULL),
  CONSTRAINT chk_category_slug_format CHECK (slug REGEXP '^[a-z0-9-]+$'),
  CONSTRAINT chk_category_name_length CHECK (CHAR_LENGTH(TRIM(name)) >= 1),
  
  -- PERFORMANS İNDEXLERİ
  INDEX idx_page_category_tenant (tenant_id),
  INDEX idx_page_category_parent (parent_id),
  INDEX idx_page_category_path_prefix (tenant_id, path(100)), -- Path prefix queries için optimize
  INDEX idx_page_category_level (tenant_id, level),
  INDEX idx_page_category_parent_sort (tenant_id, parent_id, sort_order), -- Sibling queries için
  INDEX idx_page_category_status (tenant_id, status),
  INDEX idx_page_category_composite (tenant_id, parent_id, status, sort_order) -- Tree navigation için
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- page_sections
CREATE TABLE IF NOT EXISTS page_sections (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  page_id BIGINT NOT NULL,
  type VARCHAR(50) NULL,
  display_order INT DEFAULT 0,
  data TEXT NULL,
  INDEX idx_page_section_page (page_id),
  INDEX idx_page_section_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- page_category_translations
CREATE TABLE IF NOT EXISTS page_category_translations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  language ENUM('TR','EN') NOT NULL,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(150) NOT NULL,
  description TEXT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  -- GÜVENLIK VE VERİ BÜTÜNLÜĞÜ CONSTRAINT'LERİ
  CONSTRAINT uk_page_category_i18n_unique UNIQUE (tenant_id, category_id, language),
  CONSTRAINT uk_page_category_i18n_slug_per_lang UNIQUE (tenant_id, language, slug), -- Her dilde slug unique
  CONSTRAINT chk_translation_slug_format CHECK (slug REGEXP '^[a-z0-9-]+$'),
  CONSTRAINT chk_translation_name_length CHECK (CHAR_LENGTH(TRIM(name)) >= 1),
  
  -- PERFORMANS İNDEXLERİ
  INDEX idx_cat_tr_tenant (tenant_id),
  INDEX idx_cat_tr_category (category_id),
  INDEX idx_cat_tr_lang (language),
  INDEX idx_cat_tr_batch_lookup (tenant_id, language) -- Batch translation loading için
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- page_blocks
CREATE TABLE IF NOT EXISTS page_blocks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  section_id BIGINT NOT NULL,
  type VARCHAR(50) NULL,
  display_order INT DEFAULT 0,
  data TEXT NULL,
  INDEX idx_page_block_section (section_id),
  INDEX idx_page_block_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- FOREIGN KEY CONSTRAINT'LERİ (İdempotent)

-- Self-referencing FK for page_categories parent_id
SET @fk_parent_exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'page_categories'
    AND CONSTRAINT_NAME = 'fk_page_category_parent'
);
SET @sql_parent = IF(@fk_parent_exists = 0,
  'ALTER TABLE page_categories ADD CONSTRAINT fk_page_category_parent FOREIGN KEY (parent_id) REFERENCES page_categories(id) ON DELETE CASCADE',
  'SELECT 1'
);
PREPARE stmt FROM @sql_parent;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- FK from page_category_translations to page_categories
SET @fk_trans_cat_exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'page_category_translations'
    AND CONSTRAINT_NAME = 'fk_category_translation_category'
);
SET @sql_trans_cat = IF(@fk_trans_cat_exists = 0,
  'ALTER TABLE page_category_translations ADD CONSTRAINT fk_category_translation_category FOREIGN KEY (category_id) REFERENCES page_categories(id) ON DELETE CASCADE',
  'SELECT 1'
);
PREPARE stmt FROM @sql_trans_cat;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- FK from page_sections to pages
SET @fk_section_page_exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'page_sections'
    AND CONSTRAINT_NAME = 'fk_page_section_page'
);
SET @sql_section_page = IF(@fk_section_page_exists = 0,
  'ALTER TABLE page_sections ADD CONSTRAINT fk_page_section_page FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE',
  'SELECT 1'
);
PREPARE stmt FROM @sql_section_page;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- FK from page_blocks to page_sections
SET @fk_block_section_exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'page_blocks'
    AND CONSTRAINT_NAME = 'fk_page_block_section'
);
SET @sql_block_section = IF(@fk_block_section_exists = 0,
  'ALTER TABLE page_blocks ADD CONSTRAINT fk_page_block_section FOREIGN KEY (section_id) REFERENCES page_sections(id) ON DELETE CASCADE',
  'SELECT 1'
);
PREPARE stmt FROM @sql_block_section;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- site_settings (Sprint 9)
CREATE TABLE IF NOT EXISTS site_settings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  setting_key VARCHAR(100) NOT NULL,
  setting_value TEXT NULL,
  language ENUM('TR','EN') NULL COMMENT 'NULL for global',
  setting_type ENUM('TEXT','NUMBER','BOOLEAN','JSON','URL','I18N_TEXT') DEFAULT 'TEXT',
  category VARCHAR(50) DEFAULT 'general',
  display_name VARCHAR(100) NULL,
  description TEXT NULL,
  is_public BOOLEAN DEFAULT FALSE,
  sort_order INT DEFAULT 0,
  updated_by BIGINT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uk_site_setting_key_language (setting_key, language),
  INDEX idx_site_setting_language (language),
  INDEX idx_site_setting_category (category),
  INDEX idx_site_setting_public (is_public),
  INDEX idx_site_setting_type (setting_type),
  FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

