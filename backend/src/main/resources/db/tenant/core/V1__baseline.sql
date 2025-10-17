-- Tenant Core Module Baseline
-- Essential tables for every tenant database

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role ENUM('TENANT_ADMIN', 'EDITOR', 'VIEWER') DEFAULT 'VIEWER',
    preferred_language ENUM('TR', 'EN') DEFAULT 'TR',
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verified_at TIMESTAMP NULL,
    job_title VARCHAR(100) NULL,
    phone VARCHAR(50) NULL,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active),
    INDEX idx_email_verified (email_verified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Site settings table
CREATE TABLE site_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT NULL,
    language ENUM('TR', 'EN') NULL COMMENT 'NULL for global settings',
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


