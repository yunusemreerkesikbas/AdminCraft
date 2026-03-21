-- AdminCraft Tenant Core Baseline
-- Consolidated from V1 to V43
-- Created: 2026-03-20

-- 1. Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NULL,
    last_name VARCHAR(50) NULL,
    job_title VARCHAR(100) NULL,
    department VARCHAR(100) NULL,
    phone VARCHAR(50) NULL,
    avatar_url VARCHAR(255) NULL,
    role ENUM('TENANT_ADMIN', 'VIEWER') DEFAULT 'VIEWER',
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT TRUE,
    email_verified_at TIMESTAMP NULL,
    password_changed_at TIMESTAMP NULL,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    last_login_ip VARCHAR(50) NULL,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,

    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Site settings table
CREATE TABLE site_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT NULL,
    language VARCHAR(10) NULL COMMENT 'NULL for global settings',
    setting_type ENUM('TEXT', 'NUMBER', 'BOOLEAN', 'JSON', 'URL', 'I18N_TEXT') DEFAULT 'TEXT',
    category VARCHAR(50) DEFAULT 'general',
    display_name VARCHAR(100) NULL,
    description TEXT NULL,
    is_public BOOLEAN DEFAULT FALSE COMMENT 'Accessible via public API',
    sort_order INT DEFAULT 0,
    updated_by BIGINT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_setting_key_language (setting_key, language),
    INDEX idx_language (language),
    INDEX idx_category (category),
    INDEX idx_is_public (is_public),
    INDEX idx_setting_type (setting_type),

    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Sites Table
CREATE TABLE sites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    theme_name VARCHAR(255) DEFAULT 'default',
    domain VARCHAR(255),
    custom_domain VARCHAR(255),
    is_ssl_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    default_language VARCHAR(20) NOT NULL DEFAULT 'TR',
    logo_media_uid VARCHAR(255) NULL,
    logo_dark_media_uid VARCHAR(255) NULL,
    site_title VARCHAR(60),
    site_description VARCHAR(160),
    site_keywords VARCHAR(200),
    og_image_url VARCHAR(255),
    twitter_handle VARCHAR(50),
    facebook_page_url VARCHAR(255),
    google_analytics_id VARCHAR(50),
    google_tag_manager_id VARCHAR(50),
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP NULL,
    maintenance_mode BOOLEAN NOT NULL DEFAULT FALSE,
    maintenance_message VARCHAR(500),
    recaptcha_enabled BOOLEAN DEFAULT FALSE,
    recaptcha_site_key VARCHAR(255),
    recaptcha_secret_key_encrypted TEXT,
    recaptcha_threshold DECIMAL(3,2) DEFAULT 0.5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_sites_created_by (created_by),
    INDEX idx_sites_updated_by (updated_by),
    INDEX idx_sites_recaptcha_enabled (recaptcha_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Site Languages Collection Table
CREATE TABLE site_languages (
    site_id BIGINT NOT NULL,
    language VARCHAR(20) NOT NULL,
    PRIMARY KEY (site_id, language),
    CONSTRAINT fk_site_languages_site FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Site Technical Settings
CREATE TABLE site_technical_settings (
    site_id BIGINT PRIMARY KEY COMMENT 'FK to sites table, 1-1 relationship',
    robots_txt TEXT NULL COMMENT 'Custom robots.txt content',
    sitemap_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether sitemap generation is enabled',
    indexing_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether search engine indexing is allowed',
    cookie_consent_enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether cookie consent banner is enabled',
    cookie_consent_text TEXT NULL COMMENT 'Custom cookie consent message',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_site_technical_settings_site
        FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Site Activity table
CREATE TABLE site_activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    action VARCHAR(20) NOT NULL,
    entity_type VARCHAR(20) NOT NULL,
    entity_id BIGINT NULL,
    entity_name VARCHAR(255) NULL,
    user_id BIGINT NULL,
    description VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_site_activity_uuid (uuid),
    INDEX idx_site_activity_uid (uid),
    INDEX idx_site_activity_created_at (created_at DESC),
    INDEX idx_site_activity_entity_type (entity_type),
    INDEX idx_site_activity_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Navigation Node System
CREATE TABLE navigation_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(100) NOT NULL UNIQUE,
    parent_id BIGINT NULL,
    position VARCHAR(20) DEFAULT 'LEFT',
    sort_order INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    is_tab BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_nav_parent (parent_id),
    INDEX idx_nav_sort (sort_order),
    CONSTRAINT fk_nav_parent FOREIGN KEY (parent_id)
        REFERENCES navigation_nodes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 10000;

CREATE TABLE navigation_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(100) NOT NULL UNIQUE,
    node_id BIGINT NOT NULL,
    item_type VARCHAR(30) NOT NULL,
    item_id VARCHAR(100) NULL,
    url VARCHAR(500) NULL,
    link_color VARCHAR(10) NULL,
    target VARCHAR(20) DEFAULT '_self',
    is_external BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_entry_node (node_id),
    INDEX idx_entry_sort (sort_order),
    CONSTRAINT fk_entry_node FOREIGN KEY (node_id)
        REFERENCES navigation_entries(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 10000;

-- 8. Navigation i18n
CREATE TABLE navigation_node_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(50) NOT NULL,
    uuid CHAR(36) NOT NULL,
    node_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,
    title VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    UNIQUE KEY uk_nav_node_i18n_lang (node_id, language),
    UNIQUE KEY uk_nav_node_i18n_uid (uid),
    KEY idx_nav_node_i18n_uuid (uuid),
    KEY idx_nav_node_i18n_node_id (node_id),
    KEY idx_nav_node_i18n_language (language),
    CONSTRAINT fk_nav_node_i18n_node FOREIGN KEY (node_id)
        REFERENCES navigation_nodes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE navigation_entry_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(50) NOT NULL,
    uuid CHAR(36) NOT NULL,
    entry_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,
    link_name VARCHAR(200) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    UNIQUE KEY uk_nav_entry_i18n_lang (entry_id, language),
    UNIQUE KEY uk_nav_entry_i18n_uid (uid),
    KEY idx_nav_entry_i18n_uuid (uuid),
    KEY idx_nav_entry_i18n_entry_id (entry_id),
    KEY idx_nav_entry_i18n_language (language),
    CONSTRAINT fk_nav_entry_i18n_entry FOREIGN KEY (entry_id)
        REFERENCES navigation_entries(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Verification tokens table
CREATE TABLE verification_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    token_type ENUM('EMAIL_VERIFY', 'PASSWORD_RESET', 'LOGIN_OTP', 'OPERATION_OTP') NOT NULL,
    status ENUM('ACTIVE', 'USED', 'EXPIRED', 'REVOKED') DEFAULT 'ACTIVE',
    target_value VARCHAR(255),
    expires_at DATETIME NOT NULL,
    attempt_count INT DEFAULT 0,
    max_attempts INT DEFAULT 5,
    used_at DATETIME,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_token_user (user_id),
    INDEX idx_token_hash (token_hash),
    INDEX idx_token_type_status (token_type, status),
    INDEX idx_token_expires (expires_at),
    CONSTRAINT fk_verification_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Trusted devices table
CREATE TABLE trusted_devices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    device_fingerprint VARCHAR(64) NOT NULL,
    device_name VARCHAR(100),
    browser VARCHAR(50),
    os VARCHAR(50),
    ip_address VARCHAR(45),
    trusted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_used_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_device (user_id, device_fingerprint),
    INDEX idx_device_user (user_id),
    INDEX idx_device_expires (expires_at),
    CONSTRAINT fk_trusted_device_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Config Properties table
CREATE TABLE config_properties (
  id BIGINT NOT NULL AUTO_INCREMENT,
  config_key VARCHAR(255) NOT NULL,
  config_value TEXT NULL,
  is_secret BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by BIGINT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_config_properties_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
