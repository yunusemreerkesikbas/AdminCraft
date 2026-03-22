-- Craftive Tenant Component Library Baseline
-- Consolidated from V1 to V26
-- Created: 2026-03-20

-- 1. Component Types table
CREATE TABLE component_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NULL,
    is_navigation_aware BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_component_type_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Components table
CREATE TABLE components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    component_type_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    display_order INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    style_classes VARCHAR(500),
    custom_data JSON NULL,
    responsive_id BIGINT NULL,
    navigation_node_id BIGINT NULL,
    navigation_type VARCHAR(50) NULL,
    search_box BOOLEAN NULL,
    wrap_after INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_component_type (component_type_id),
    INDEX idx_component_status (status),
    INDEX idx_component_responsive (responsive_id),
    INDEX idx_component_navigation_node (navigation_node_id),
    CONSTRAINT fk_component_type FOREIGN KEY (component_type_id) REFERENCES component_types(id) ON DELETE RESTRICT,
    CONSTRAINT fk_component_responsive FOREIGN KEY (responsive_id) REFERENCES responsive_media_set(id) ON DELETE SET NULL,
    CONSTRAINT fk_component_navigation_node FOREIGN KEY (navigation_node_id) REFERENCES navigation_nodes(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Component i18n table
CREATE TABLE component_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    component_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,
    title VARCHAR(200),
    subtitle VARCHAR(200),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_component_i18n_component_lang (component_id, language),
    UNIQUE KEY uk_component_i18n_uid (uid),
    INDEX idx_component_i18n_component (component_id),
    INDEX idx_component_i18n_language (language),
    INDEX idx_component_i18n_title (title),
    INDEX idx_component_i18n_language_status (language, status),
    CONSTRAINT fk_component_i18n_component FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Component Entries table
CREATE TABLE component_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    component_id BIGINT NOT NULL,
    sort_order INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    style_classes VARCHAR(500),
    responsive_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    INDEX idx_component_order (component_id, sort_order),
    INDEX idx_entry_status (status),
    INDEX idx_entry_responsive (responsive_id),
    CONSTRAINT fk_entry_component FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE,
    CONSTRAINT fk_entry_responsive FOREIGN KEY (responsive_id) REFERENCES responsive_media_set(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Component Entry i18n table
CREATE TABLE component_entry_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    entry_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,
    title VARCHAR(255),
    description TEXT,
    custom_data JSON DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    UNIQUE KEY uk_entry_language (entry_id, language),
    INDEX idx_entry_i18n_entry (entry_id),
    INDEX idx_entry_i18n_language (language),
    INDEX idx_entry_i18n_status (status),
    CONSTRAINT fk_entry_i18n_entry FOREIGN KEY (entry_id) REFERENCES component_entries(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Entry Field Definitions
CREATE TABLE entry_field_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    component_type_id BIGINT NOT NULL,
    field_key VARCHAR(50) NOT NULL,
    field_type ENUM('TEXT', 'TEXTAREA', 'NUMBER', 'BOOLEAN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type_field (component_type_id, field_key),
    INDEX idx_field_type (component_type_id),
    CONSTRAINT fk_field_def_type FOREIGN KEY (component_type_id) REFERENCES component_types(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Component Media Links
CREATE TABLE component_media_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    component_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    link_type VARCHAR(20) NOT NULL DEFAULT 'ENTRY_MEDIA',
    entry_id BIGINT NULL,
    responsive_set_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cml_component_media_type (component_id, media_id, link_type, entry_id),
    INDEX idx_cml_media (media_id),
    INDEX idx_cml_component (component_id),
    INDEX idx_cml_entry (entry_id),
    INDEX idx_cml_responsive_set (responsive_set_id),
    CONSTRAINT fk_cml_component FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE,
    CONSTRAINT fk_cml_media FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE,
    CONSTRAINT fk_cml_entry FOREIGN KEY (entry_id) REFERENCES component_entries(id) ON DELETE CASCADE,
    CONSTRAINT fk_cml_responsive_set FOREIGN KEY (responsive_set_id) REFERENCES responsive_media_set(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
