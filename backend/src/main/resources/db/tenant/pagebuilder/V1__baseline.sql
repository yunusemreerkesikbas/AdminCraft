-- Page Builder Module Baseline
-- Multi-language i18n architecture for page management

-- Pages table (language-agnostic data)
CREATE TABLE pages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'Server-generated UUID for external references',
    uid VARCHAR(50) NOT NULL UNIQUE COMMENT 'Human-readable stable identifier',
    category_id BIGINT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED', 'SCHEDULED') DEFAULT 'DRAFT',
    featured_image VARCHAR(500) NULL,
    style_classes VARCHAR(255) NULL,
    is_home BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    
    UNIQUE KEY uk_page_uid (uid),
    INDEX idx_page_category (category_id),
    INDEX idx_page_status (status),
    INDEX idx_page_home (is_home),
    INDEX idx_page_sort (sort_order),
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Page i18n table (language-specific content)
CREATE TABLE page_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'Server-generated UUID',
    uid VARCHAR(50) NOT NULL UNIQUE COMMENT 'Human-readable identifier for this i18n entry',
    page_id BIGINT NOT NULL,
    language ENUM('TR', 'EN') NOT NULL,
    url_path VARCHAR(255) NULL,
    title VARCHAR(200) NULL,
    subtitle VARCHAR(200) NULL,
    meta_title VARCHAR(60) NULL,
    meta_description VARCHAR(160) NULL,
    description LONGTEXT NULL,
    description_html LONGTEXT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED', 'SCHEDULED') DEFAULT 'DRAFT',
    published_at TIMESTAMP NULL,
    scheduled_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_page_i18n_page_lang (page_id, language),
    UNIQUE KEY uk_page_i18n_uid (uid),
    UNIQUE KEY uk_page_i18n_url_path (language, url_path),
    INDEX idx_page_i18n_page (page_id),
    INDEX idx_page_i18n_language (language),
    INDEX idx_page_i18n_status (language, status),
    INDEX idx_page_i18n_published (language, published_at),
    
    FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Page categories table (language-agnostic data)
CREATE TABLE page_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'Server-generated UUID',
    uid VARCHAR(50) NOT NULL UNIQUE COMMENT 'Human-readable identifier',
    parent_id BIGINT NULL,
    active BOOLEAN DEFAULT TRUE,
    style_classes VARCHAR(255) NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    
    UNIQUE KEY uk_page_category_uid (uid),
    INDEX idx_page_category_parent (parent_id),
    INDEX idx_page_category_active (active),
    INDEX idx_page_category_sort (parent_id, sort_order),
    
    FOREIGN KEY (parent_id) REFERENCES page_categories(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Page category i18n table (language-specific content)
CREATE TABLE page_category_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'Server-generated UUID',
    uid VARCHAR(50) NOT NULL UNIQUE COMMENT 'Human-readable identifier',
    category_id BIGINT NOT NULL,
    language ENUM('TR', 'EN') NOT NULL,
    url VARCHAR(150) NULL COMMENT 'URL slug for this language',
    title VARCHAR(200) NULL COMMENT 'Category title in this language',
    meta_title VARCHAR(60) NULL,
    meta_description VARCHAR(160) NULL,
    active BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_page_category_i18n_cat_lang (category_id, language),
    UNIQUE KEY uk_page_category_i18n_uid (uid),
    UNIQUE KEY uk_page_category_i18n_url_lang (language, url),
    INDEX idx_cat_i18n_category (category_id),
    INDEX idx_cat_i18n_lang (language),
    INDEX idx_cat_i18n_active (language, active),
    
    FOREIGN KEY (category_id) REFERENCES page_categories(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add FK from pages to page_categories
ALTER TABLE pages 
ADD CONSTRAINT fk_pages_category 
FOREIGN KEY (category_id) REFERENCES page_categories(id) ON DELETE SET NULL;



