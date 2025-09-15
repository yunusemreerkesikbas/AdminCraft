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
  tenant_id BIGINT NOT NULL,
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

  -- TENANT ISOLATION CONSTRAINT
  CONSTRAINT uk_site_setting_key_lang_tenant UNIQUE (tenant_id, setting_key, language),
  CONSTRAINT chk_site_setting_key_format CHECK (setting_key REGEXP '^[a-z0-9._-]+$'),
  
  -- PERFORMANCE INDICES
  INDEX idx_site_setting_tenant (tenant_id),
  INDEX idx_site_setting_tenant_lang (tenant_id, language),
  INDEX idx_site_setting_tenant_category (tenant_id, category),
  INDEX idx_site_setting_tenant_public (tenant_id, is_public),
  INDEX idx_site_setting_type (setting_type),
  
  -- FOREIGN KEYS
  FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
  FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- UI Components (Sprint 10) - Type-Based Routing Optimized
CREATE TABLE IF NOT EXISTS ui_components (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  type VARCHAR(30) NOT NULL,
  component_key VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  visible BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by BIGINT NOT NULL,
  updated_by BIGINT NULL,

  -- CONSTRAINTS
  UNIQUE KEY uk_ui_component_tenant_type_key (tenant_id, type, component_key),
  CONSTRAINT chk_ui_component_type CHECK (type IN ('NAVBAR', 'LOGO', 'CTA', 'BRANDS', 'FAQ', 'BREADCRUMB')),
  CONSTRAINT chk_ui_component_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
  CONSTRAINT chk_ui_component_key_format CHECK (component_key REGEXP '^[a-z0-9._-]+$'),
  CONSTRAINT chk_ui_component_sort_order CHECK (sort_order >= 0),

  -- PERFORMANCE INDEXES - Type-Based Routing Optimized
  KEY idx_ui_component_tenant (tenant_id),
  KEY idx_ui_component_type (type),
  KEY idx_ui_component_status (status),
  KEY idx_ui_component_sort (sort_order),
  KEY idx_ui_component_tenant_type (tenant_id, type), -- Main type-based query
  KEY idx_ui_component_tenant_type_status (tenant_id, type, status), -- Filtered type queries
  KEY idx_ui_component_tenant_type_sort (tenant_id, type, sort_order, status), -- Ordered type queries
  KEY idx_ui_component_tenant_type_visible (tenant_id, type, visible, status), -- Visible-only queries
  KEY idx_ui_component_tenant_status_sort (tenant_id, status, sort_order), -- Status-based queries

  -- FOREIGN KEYS
  CONSTRAINT fk_ui_component_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ui_component_translations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  component_id BIGINT NOT NULL,
  language VARCHAR(5) NOT NULL,
  title VARCHAR(200) NULL,
  subtitle VARCHAR(300) NULL,
  data LONGTEXT NULL,

  -- CONSTRAINTS
  UNIQUE KEY uk_ui_component_translation_lang (component_id, language),
  CONSTRAINT chk_ui_comp_tr_language CHECK (language IN ('tr', 'en')),
  CONSTRAINT chk_ui_comp_tr_title_length CHECK (title IS NULL OR CHAR_LENGTH(TRIM(title)) > 0),
  CONSTRAINT chk_ui_comp_tr_subtitle_length CHECK (subtitle IS NULL OR CHAR_LENGTH(TRIM(subtitle)) > 0),

  -- PERFORMANCE INDEXES - Translation Loading Optimized
  KEY idx_ui_comp_tr_component (component_id),
  KEY idx_ui_comp_tr_language (language),
  KEY idx_ui_comp_tr_component_lang (component_id, language), -- Specific translation lookup
  KEY idx_ui_comp_tr_batch_load (component_id, language, title), -- Batch loading with fallback

  -- FOREIGN KEYS
  CONSTRAINT fk_ui_comp_tr_component FOREIGN KEY (component_id)
    REFERENCES ui_components(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
