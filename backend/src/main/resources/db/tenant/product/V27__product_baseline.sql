-- =====================================================
-- V27: Product Catalog Module Baseline
-- Sprint 40: Product Module Implementation
-- =====================================================

-- Product Types (Electronics, Clothing, General, etc.)
CREATE TABLE product_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    
    CONSTRAINT uk_product_type_uuid UNIQUE (uuid),
    CONSTRAINT uk_product_type_uid UNIQUE (uid),
    CONSTRAINT uk_product_type_code UNIQUE (code),
    INDEX idx_product_type_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=10000;

-- Product Attribute Definitions (dynamic fields per product type)
CREATE TABLE product_attribute_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    product_type_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    field_type VARCHAR(30) NOT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    is_searchable BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    validation_config JSON NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_product_attr_def_uuid UNIQUE (uuid),
    CONSTRAINT uk_product_attr_def_uid UNIQUE (uid),
    CONSTRAINT uk_product_attr_type_code UNIQUE (product_type_id, code),
    CONSTRAINT fk_product_attr_def_type FOREIGN KEY (product_type_id) 
        REFERENCES product_types(id) ON DELETE CASCADE,
    INDEX idx_product_attr_def_type (product_type_id),
    INDEX idx_product_attr_def_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=10000;

-- Categories (hierarchical product categories)
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    parent_id BIGINT NULL,
    sort_order INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    
    CONSTRAINT uk_category_uuid UNIQUE (uuid),
    CONSTRAINT uk_category_uid UNIQUE (uid),
    CONSTRAINT uk_category_code UNIQUE (code),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) 
        REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_category_parent (parent_id),
    INDEX idx_category_sort (sort_order),
    INDEX idx_category_visible (is_visible)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=10000;

-- Category i18n (multilingual category content)
CREATE TABLE category_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    category_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    
    CONSTRAINT uk_category_i18n_uuid UNIQUE (uuid),
    CONSTRAINT uk_category_i18n_uid UNIQUE (uid),
    CONSTRAINT uk_category_i18n_lang UNIQUE (category_id, language),
    CONSTRAINT fk_category_i18n_category FOREIGN KEY (category_id) 
        REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_category_i18n_category (category_id),
    INDEX idx_category_i18n_language (language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=10000;

-- Products (main product entity)
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    product_type_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL,
    base_price DECIMAL(15,2) NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    is_visible BOOLEAN DEFAULT TRUE,
    responsive_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    
    CONSTRAINT uk_product_uuid UNIQUE (uuid),
    CONSTRAINT uk_product_uid UNIQUE (uid),
    CONSTRAINT uk_product_sku UNIQUE (sku),
    CONSTRAINT fk_product_type FOREIGN KEY (product_type_id) 
        REFERENCES product_types(id) ON DELETE RESTRICT,
    CONSTRAINT fk_product_responsive FOREIGN KEY (responsive_id) 
        REFERENCES responsive_media_set(id) ON DELETE SET NULL,
    INDEX idx_product_type (product_type_id),
    INDEX idx_product_status (status),
    INDEX idx_product_visible (is_visible),
    INDEX idx_product_responsive (responsive_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=10000;

-- Product i18n (multilingual product content)
CREATE TABLE product_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,
    name VARCHAR(200) NOT NULL,
    short_description VARCHAR(500) NULL,
    description TEXT NULL,
    seo_title VARCHAR(200) NULL,
    seo_description VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    
    CONSTRAINT uk_product_i18n_uuid UNIQUE (uuid),
    CONSTRAINT uk_product_i18n_uid UNIQUE (uid),
    CONSTRAINT uk_product_i18n_lang UNIQUE (product_id, language),
    CONSTRAINT fk_product_i18n_product FOREIGN KEY (product_id) 
        REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_i18n_product (product_id),
    INDEX idx_product_i18n_language (language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=10000;

-- Product Attributes (EAV - dynamic attribute values)
CREATE TABLE product_attributes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    attribute_definition_id BIGINT NOT NULL,
    value_text TEXT NULL,
    value_number DECIMAL(15,4) NULL,
    value_boolean BOOLEAN NULL,
    value_date DATE NULL,
    value_media_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_product_attr UNIQUE (product_id, attribute_definition_id),
    CONSTRAINT fk_product_attr_product FOREIGN KEY (product_id) 
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_attr_def FOREIGN KEY (attribute_definition_id) 
        REFERENCES product_attribute_definitions(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_attr_media FOREIGN KEY (value_media_id) 
        REFERENCES media(id) ON DELETE SET NULL,
    INDEX idx_product_attr_product (product_id),
    INDEX idx_product_attr_def (attribute_definition_id),
    INDEX idx_product_attr_media (value_media_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=10000;

-- Product-Category Links (N:N relationship with primary flag)
CREATE TABLE product_category_links (
    product_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_pcl_product FOREIGN KEY (product_id) 
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_pcl_category FOREIGN KEY (category_id) 
        REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_pcl_category (category_id),
    INDEX idx_pcl_primary (is_primary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Product Media (gallery images)
CREATE TABLE product_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    media_type VARCHAR(30) DEFAULT 'GALLERY',
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_product_media UNIQUE (product_id, media_id),
    CONSTRAINT fk_pm_product FOREIGN KEY (product_id) 
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_media FOREIGN KEY (media_id) 
        REFERENCES media(id) ON DELETE CASCADE,
    INDEX idx_pm_product (product_id),
    INDEX idx_pm_media (media_id),
    INDEX idx_pm_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=10000;
