-- ============================================
-- Media Module Baseline Migration
-- Version: V20
-- Description: Creates all media module tables
-- ============================================

-- ============================================
-- 1. MEDIA FORMAT DEFINITIONS (System + Custom)
-- ============================================
CREATE TABLE media_formats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    quality INT DEFAULT 80,
    crop_mode VARCHAR(20) DEFAULT 'FIT',
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT uk_media_format_uuid UNIQUE (uuid),
    CONSTRAINT uk_media_format_uid UNIQUE (uid),
    CONSTRAINT uk_media_format_code UNIQUE (code),
    INDEX idx_media_format_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. MEDIA FOLDERS (Hierarchical Organization)
-- ============================================
CREATE TABLE media_folders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT,
    path VARCHAR(768) NOT NULL,
    depth INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT uk_media_folder_uuid UNIQUE (uuid),
    CONSTRAINT uk_media_folder_uid UNIQUE (uid),
    CONSTRAINT uk_media_folder_path UNIQUE (path),
    CONSTRAINT fk_media_folder_parent FOREIGN KEY (parent_id) REFERENCES media_folders(id) ON DELETE CASCADE,
    INDEX idx_media_folder_parent (parent_id),
    INDEX idx_media_folder_depth (depth)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. MEDIA FILES (Master/Original)
-- ============================================
CREATE TABLE media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,

    -- File Properties
    original_name VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_extension VARCHAR(20),
    file_size BIGINT NOT NULL,

    -- Image/Video Properties
    width INT,
    height INT,
    duration INT,

    -- Organization
    folder_id BIGINT,
    tags JSON,

    -- Access Control
    is_public BOOLEAN DEFAULT TRUE,
    uploaded_by BIGINT,

    -- Storage Provider
    storage_provider VARCHAR(20) DEFAULT 'LOCAL',
    external_url VARCHAR(1000),
    external_id VARCHAR(255),

    -- Metadata
    metadata JSON,
    usage_count INT DEFAULT 0,

    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE',

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    last_accessed_at TIMESTAMP,

    CONSTRAINT uk_media_uuid UNIQUE (uuid),
    CONSTRAINT uk_media_uid UNIQUE (uid),
    CONSTRAINT uk_media_file_name UNIQUE (file_name),
    CONSTRAINT fk_media_folder FOREIGN KEY (folder_id) REFERENCES media_folders(id) ON DELETE SET NULL,
    INDEX idx_media_folder (folder_id),
    INDEX idx_media_mime_type (mime_type),
    INDEX idx_media_status (status),
    INDEX idx_media_is_public (is_public),
    INDEX idx_media_uploaded_by (uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. MEDIA I18N (Localized Metadata)
-- ============================================
CREATE TABLE media_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    media_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,

    alt_text VARCHAR(500),
    title VARCHAR(255),
    description TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT uk_media_i18n_uuid UNIQUE (uuid),
    CONSTRAINT uk_media_i18n_uid UNIQUE (uid),
    CONSTRAINT uk_media_i18n_lang UNIQUE (media_id, language),
    CONSTRAINT fk_media_i18n_media FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE,
    INDEX idx_media_i18n_media_id (media_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 5. MEDIA CONTAINERS (SAP Hybris Pattern)
-- Groups master + generated formats together
-- ============================================
CREATE TABLE media_containers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    code VARCHAR(100) NOT NULL,
    master_media_id BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT uk_media_container_uuid UNIQUE (uuid),
    CONSTRAINT uk_media_container_uid UNIQUE (uid),
    CONSTRAINT uk_media_container_code UNIQUE (code),
    CONSTRAINT fk_media_container_master FOREIGN KEY (master_media_id) REFERENCES media(id) ON DELETE CASCADE,
    INDEX idx_media_container_master (master_media_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 6. MEDIA CONTAINER ITEMS (Format Variants)
-- Links container to generated format versions
-- ============================================
CREATE TABLE media_container_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    container_id BIGINT NOT NULL,
    format_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,

    CONSTRAINT fk_container_item_container FOREIGN KEY (container_id) REFERENCES media_containers(id) ON DELETE CASCADE,
    CONSTRAINT fk_container_item_format FOREIGN KEY (format_id) REFERENCES media_formats(id) ON DELETE RESTRICT,
    CONSTRAINT fk_container_item_media FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE,
    CONSTRAINT uk_container_format UNIQUE (container_id, format_id),
    INDEX idx_container_item_container (container_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
