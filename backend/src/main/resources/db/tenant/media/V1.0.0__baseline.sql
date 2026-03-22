-- Craftive Tenant Media Baseline
-- Consolidated from V20 to V24
-- Created: 2026-03-20

-- 1. MEDIA FORMAT DEFINITIONS (System + Custom)
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
    output_format VARCHAR(20) DEFAULT 'ORIGINAL',
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

-- 2. MEDIA FILES (Master/Original)
CREATE TABLE media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_extension VARCHAR(20),
    file_size BIGINT NOT NULL,
    width INT,
    height INT,
    duration INT,
    focal_point_x DOUBLE DEFAULT 0.5,
    focal_point_y DOUBLE DEFAULT 0.5,
    tags JSON,
    is_public BOOLEAN DEFAULT TRUE,
    uploaded_by BIGINT,
    storage_provider VARCHAR(20) DEFAULT 'LOCAL',
    external_url VARCHAR(1000),
    external_id VARCHAR(255),
    metadata JSON,
    usage_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    last_accessed_at TIMESTAMP,

    CONSTRAINT uk_media_uuid UNIQUE (uuid),
    CONSTRAINT uk_media_uid UNIQUE (uid),
    CONSTRAINT uk_media_file_name UNIQUE (file_name),
    INDEX idx_media_mime_type (mime_type),
    INDEX idx_media_status (status),
    INDEX idx_media_is_public (is_public),
    INDEX idx_media_uploaded_by (uploaded_by),
    INDEX idx_media_focal_point (focal_point_x, focal_point_y)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. MEDIA I18N (Localized Metadata)
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

-- 4. MEDIA CONTAINERS (SAP Hybris Pattern)
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

-- 5. MEDIA CONTAINER ITEMS (Format Variants)
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

-- 6. ResponsiveMediaSet: Stores Desktop/Mobile image pairs
CREATE TABLE responsive_media_set (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    code VARCHAR(100) NOT NULL,
    desktop_media_id BIGINT,
    mobile_media_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT uk_responsive_media_uuid UNIQUE (uuid),
    CONSTRAINT uk_responsive_media_uid UNIQUE (uid),
    CONSTRAINT uk_responsive_media_code UNIQUE (code),
    CONSTRAINT fk_responsive_desktop_media
        FOREIGN KEY (desktop_media_id) REFERENCES media(id) ON DELETE SET NULL,
    CONSTRAINT fk_responsive_mobile_media
        FOREIGN KEY (mobile_media_id) REFERENCES media(id) ON DELETE SET NULL,
    INDEX idx_responsive_desktop (desktop_media_id),
    INDEX idx_responsive_mobile (mobile_media_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
