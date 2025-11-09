-- Component Library Module Baseline
-- Dynamic component management system for reusable UI components

-- Component Types table (type definitions: header, footer, cta, etc.)
CREATE TABLE component_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'Server-generated UUID for external references',
    uid VARCHAR(50) NOT NULL UNIQUE COMMENT 'Human-readable stable identifier',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique code for component type (e.g., header, footer)',
    name VARCHAR(100) NOT NULL COMMENT 'Display name of the component type',
    category VARCHAR(50) NULL COMMENT 'Category grouping (e.g., navigation, marketing)',
    icon VARCHAR(50) NULL COMMENT 'Icon identifier for UI',
    is_system BOOLEAN DEFAULT FALSE COMMENT 'System-defined types cannot be deleted',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,

    UNIQUE KEY uk_component_type_uid (uid),
    UNIQUE KEY uk_component_type_code (code),
    INDEX idx_component_type_category (category),
    INDEX idx_component_type_system (is_system),

    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Components table (component items)
CREATE TABLE components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'Server-generated UUID for external references',
    uid VARCHAR(50) NOT NULL UNIQUE COMMENT 'Human-readable stable identifier',
    component_type_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique code for component instance',
    name VARCHAR(100) NOT NULL COMMENT 'Display name of the component',
    base_data JSON NULL COMMENT 'Standard fields: order, isVisible, styleClasses',
    status ENUM('ACTIVE', 'INACTIVE', 'DRAFT') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,

    UNIQUE KEY uk_component_uid (uid),
    UNIQUE KEY uk_component_code (code),
    INDEX idx_component_type (component_type_id),
    INDEX idx_component_status (status),

    FOREIGN KEY (component_type_id) REFERENCES component_types(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Component i18n table (language-specific content)
CREATE TABLE component_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'Server-generated UUID',
    uid VARCHAR(50) NOT NULL UNIQUE COMMENT 'Human-readable identifier for this i18n entry',
    component_id BIGINT NOT NULL,
    language ENUM('TR', 'EN') NOT NULL,
    base_localized_data JSON NULL COMMENT 'Standard i18n fields: title, subtitle, description, imageUrl, imageAlt, buttonText, buttonUrl, buttonStyle, links',
    status ENUM('ACTIVE', 'INACTIVE', 'DRAFT') DEFAULT 'DRAFT',
    published_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_component_i18n_component_lang (component_id, language),
    UNIQUE KEY uk_component_i18n_uid (uid),
    INDEX idx_component_i18n_component (component_id),
    INDEX idx_component_i18n_language (language),
    INDEX idx_component_i18n_status (language, status),
    INDEX idx_component_i18n_published (language, published_at),

    FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
