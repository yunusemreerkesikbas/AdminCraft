-- AdminCraft Multi-Language Multi-Tenant Database Initialization
-- Supports Turkish (TR) and English (EN) languages

-- Create main database if not exists
CREATE DATABASE IF NOT EXISTS admincraft-db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS admincraft_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use main database
USE admincraft-db;

-- Create tenant table first (needed for foreign keys)
CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subdomain VARCHAR(100) NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    database_name VARCHAR(100) NOT NULL,
    status ENUM('PENDING', 'ACTIVE', 'SUSPENDED', 'DELETED') DEFAULT 'PENDING',
    default_language ENUM('TR', 'EN') DEFAULT 'TR',
    supported_languages JSON DEFAULT ('["TR"]'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_subdomain (subdomain),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role ENUM('SUPER_ADMIN', 'TENANT_ADMIN', 'EDITOR', 'VIEWER') DEFAULT 'VIEWER',
    preferred_language ENUM('TR', 'EN') DEFAULT 'TR',
    tenant_id BIGINT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verified_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active),
    INDEX idx_tenant_role (tenant_id, role),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create content_types table
CREATE TABLE IF NOT EXISTS content_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    fields JSON NOT NULL,
    supports_multi_language BOOLEAN DEFAULT TRUE,
    tenant_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY unique_name_tenant (name, tenant_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_is_active (is_active),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create contents table with multi-language support
CREATE TABLE IF NOT EXISTS contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL,
    data JSON NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    language ENUM('TR', 'EN') DEFAULT 'TR',
    parent_content_id BIGINT NULL COMMENT 'For translations - links to original content',
    content_type_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    
    -- SEO fields
    meta_title VARCHAR(255) NULL,
    meta_description TEXT NULL,
    meta_keywords VARCHAR(500) NULL,
    
    -- Timestamps
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    
    UNIQUE KEY unique_slug_tenant_language (slug, tenant_id, language),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_content_type_id (content_type_id),
    INDEX idx_status (status),
    INDEX idx_language (language),
    INDEX idx_parent_content_id (parent_content_id),
    INDEX idx_published_at (published_at),
    INDEX idx_created_at (created_at),
    FULLTEXT(title, meta_description),
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (content_type_id) REFERENCES content_types(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_content_id) REFERENCES contents(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create media_files table with multi-language alt text
CREATE TABLE IF NOT EXISTS media_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(500) NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    
    -- Image properties
    width INT NULL,
    height INT NULL,
    
    -- Multi-language alt text
    alt_text_tr TEXT NULL,
    alt_text_en TEXT NULL,
    
    tenant_id BIGINT NOT NULL,
    uploaded_by BIGINT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_mime_type (mime_type),
    INDEX idx_file_size (file_size),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create sites table
CREATE TABLE IF NOT EXISTS sites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    domain VARCHAR(255) NULL,
    enabled_languages JSON DEFAULT ('["TR"]'),
    default_language ENUM('TR', 'EN') DEFAULT 'TR',
    tenant_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- SEO and settings
    favicon_url VARCHAR(500) NULL,
    logo_url VARCHAR(500) NULL,
    theme_config JSON NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_domain (domain),
    INDEX idx_is_active (is_active),
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create menus table (language-specific)
CREATE TABLE IF NOT EXISTS menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    language ENUM('TR', 'EN') DEFAULT 'TR',
    menu_data JSON NOT NULL,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    
    UNIQUE KEY unique_name_tenant_language (name, tenant_id, language),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_site_id (site_id),
    INDEX idx_language (language),
    INDEX idx_is_active (is_active),
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SECURITY NOTE: Initial Super Admin Setup
-- ==========================================
-- For security reasons, no default admin user is created in the database initialization.
-- The initial super admin user must be created through one of these secure methods:
--
-- 1. Application Setup Endpoint: /api/setup (recommended for production)
--    - Requires environment variable ADMIN_SETUP_TOKEN for authorization
--    - Allows setting secure email and password
--    - Automatically disabled after first admin user creation
--
-- 2. Application Startup Process (for development)
--    - Use environment variables: INITIAL_ADMIN_EMAIL and INITIAL_ADMIN_PASSWORD
--    - Password will be properly hashed using BCrypt
--
-- 3. Manual Creation via Application API (after deployment)
--    - Use the standard user creation endpoint with appropriate authentication
--
-- This approach ensures:
-- - No hardcoded credentials in source code
-- - Proper password hashing and validation
-- - Secure setup process aligned with Clean Architecture principles

-- Note: Default content types should be created when tenants are created through the application
-- This ensures proper tenant isolation and follows Clean Architecture principles

-- Create indexes for better performance
CREATE INDEX idx_contents_tenant_language_status ON contents(tenant_id, language, status);
CREATE INDEX idx_media_files_tenant_type ON media_files(tenant_id, mime_type);
CREATE INDEX idx_users_tenant_role ON users(tenant_id, role);

-- Enable events for audit trail
SET GLOBAL event_scheduler = ON;