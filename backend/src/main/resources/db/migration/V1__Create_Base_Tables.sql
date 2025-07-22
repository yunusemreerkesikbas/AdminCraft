-- Base Tables Migration
-- AdminCraft SaaS CMS Platform - Create fundamental tables

-- ================================================================
-- Tenants table - Multi-tenant architecture foundation
-- ================================================================
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subdomain VARCHAR(50) UNIQUE NOT NULL COMMENT 'customer1.platform.com',
    company_name VARCHAR(100) NOT NULL,
    database_name VARCHAR(50) UNIQUE NOT NULL COMMENT 'tenant_1_db',
    
    -- Status Management
    status ENUM('PENDING', 'ACTIVE', 'SUSPENDED', 'MAINTENANCE') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    activated_at TIMESTAMP NULL,
    
    -- Language Configuration
    default_language ENUM('TR', 'EN') NOT NULL DEFAULT 'TR',
    supported_languages JSON NOT NULL DEFAULT '["TR"]',
    
    -- Contact Information
    admin_email VARCHAR(100) NOT NULL,
    admin_name VARCHAR(100) NOT NULL,
    admin_language ENUM('TR', 'EN') DEFAULT 'TR',
    phone VARCHAR(20) NULL,
    
    -- Domain Configuration
    custom_domain VARCHAR(100) NULL COMMENT 'customer.com',
    ssl_enabled BOOLEAN DEFAULT FALSE,
    ssl_certificate_path VARCHAR(255) NULL,
    
    -- Technical Configuration
    database_version VARCHAR(10) DEFAULT '1.0',
    last_backup_at TIMESTAMP NULL,
    storage_used_mb BIGINT DEFAULT 0,
    
    -- Localization Settings
    timezone VARCHAR(50) DEFAULT 'Europe/Istanbul',
    date_format VARCHAR(20) DEFAULT 'DD/MM/YYYY',
    time_format VARCHAR(10) DEFAULT '24h',
    currency VARCHAR(3) DEFAULT 'TRY',
    
    -- Metadata
    notes TEXT NULL,
    created_by VARCHAR(100) DEFAULT 'system',
    
    INDEX idx_subdomain (subdomain),
    INDEX idx_status (status),
    INDEX idx_default_language (default_language),
    INDEX idx_custom_domain (custom_domain),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Multi-language tenant registry';

-- ================================================================
-- Insert default tenant for development
-- ================================================================
INSERT INTO tenants (
    subdomain, company_name, database_name, status,
    admin_email, admin_name, admin_language,
    default_language, supported_languages,
    timezone, currency
) VALUES (
    'demo', 'Demo Şirketi A.Ş.', 'admincraft_demo_db', 'ACTIVE',
    'admin@demo.com', 'Demo Admin', 'TR',
    'TR', '["TR", "EN"]',
    'Europe/Istanbul', 'TRY'
); 