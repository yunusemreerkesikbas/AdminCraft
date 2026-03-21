-- AdminCraft Tenant Page Builder Baseline
-- Consolidated from V1 to V38
-- Created: 2026-03-20

-- 1. Page Templates table
CREATE TABLE page_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    is_system BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_page_template_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Page Template i18n
CREATE TABLE page_template_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(50) NOT NULL UNIQUE,
    uuid CHAR(36) NOT NULL,
    template_id BIGINT NOT NULL,
    language ENUM('TR', 'EN', 'ES', 'RU', 'AR', 'ZH', 'HI', 'FR', 'BN', 'PT', 'UR') NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    UNIQUE KEY uk_template_i18n_lang (template_id, language),
    KEY idx_template_i18n_uuid (uuid),
    KEY idx_template_i18n_template_id (template_id),
    KEY idx_template_i18n_language (language),
    CONSTRAINT fk_template_i18n_template FOREIGN KEY (template_id)
        REFERENCES page_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Template Slots
CREATE TABLE template_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    template_id BIGINT NOT NULL,
    slot_name VARCHAR(50) NOT NULL,
    position VARCHAR(20) NOT NULL,
    sort_order INT DEFAULT 0,
    is_required BOOLEAN DEFAULT FALSE,
    max_components INT NULL,
    allowed_types JSON NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_template_slot (template_id, slot_name),
    INDEX idx_template_slot_template (template_id),
    INDEX idx_template_slot_sort_order (sort_order),
    CONSTRAINT fk_template_slot_template FOREIGN KEY (template_id) REFERENCES page_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Pages table
CREATE TABLE pages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    template_id BIGINT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    page_type VARCHAR(20) DEFAULT 'CONTENT',
    is_home BOOLEAN DEFAULT FALSE,
    robot_tag ENUM('INDEX_FOLLOW', 'NOINDEX_FOLLOW', 'INDEX_NOFOLLOW', 'NOINDEX_NOFOLLOW') DEFAULT 'INDEX_FOLLOW',
    published_at datetime(6),
    scheduled_at datetime(6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_page_uid (uid),
    INDEX idx_page_status (status),
    INDEX idx_page_home (is_home),
    INDEX idx_page_template (template_id),
    INDEX idx_page_type_status (page_type, status),
    INDEX idx_page_is_home_status (is_home, status),
    CONSTRAINT fk_page_template FOREIGN KEY (template_id) REFERENCES page_templates(id) ON DELETE SET NULL,
    CONSTRAINT fk_page_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_page_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 10000;

-- 5. Page i18n table
CREATE TABLE page_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    page_id BIGINT NOT NULL,
    language ENUM('TR', 'EN', 'ES', 'RU', 'AR', 'ZH', 'HI', 'FR', 'BN', 'PT', 'UR') NOT NULL,
    canonical_url VARCHAR(255) NULL,
    title VARCHAR(200) NULL,
    name VARCHAR(200) NULL,
    description LONGTEXT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_page_i18n_page_lang (page_id, language),
    UNIQUE KEY uk_page_i18n_uid (uid),
    UNIQUE KEY uk_page_i18n_canonical_url (language, canonical_url),
    INDEX idx_page_i18n_page (page_id),
    INDEX idx_page_i18n_language (language),
    INDEX idx_page_i18n_status (language, status),
    CONSTRAINT fk_page_i18n_page FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 10000;

-- 6. Page Slots table
CREATE TABLE page_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    page_id BIGINT NULL COMMENT 'NULL = shared/global slot',
    slot_name VARCHAR(50) NOT NULL COMMENT 'Descriptive name: Header, MainContent, Footer',
    position VARCHAR(20) NOT NULL COMMENT 'Layout position: TOP, CENTER, BOTTOM, LEFT, RIGHT',
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_shared BOOLEAN DEFAULT FALSE COMMENT 'Explicit shared flag for global slots',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_page_slots_page (page_id),
    INDEX idx_page_slots_shared (is_shared),
    INDEX idx_page_slots_active (is_active),
    UNIQUE KEY uk_page_slot (page_id, slot_name),
    CONSTRAINT fk_page_slot_page FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Slot Components junction table
CREATE TABLE slot_components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_id BIGINT NOT NULL,
    component_id BIGINT NOT NULL,
    sort_order INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_slot_components_slot (slot_id),
    INDEX idx_slot_components_component (component_id),
    INDEX idx_slot_components_order (slot_id, sort_order),
    UNIQUE KEY uk_slot_component (slot_id, component_id),
    CONSTRAINT fk_slot_comp_slot FOREIGN KEY (slot_id) REFERENCES page_slots(id) ON DELETE CASCADE,
    CONSTRAINT fk_slot_comp_comp FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
