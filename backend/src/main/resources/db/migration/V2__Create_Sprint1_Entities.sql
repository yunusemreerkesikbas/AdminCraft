bu ref-- Sprint 1 Backend Entities Migration
-- AdminCraft SaaS CMS Platform - Clean Architecture + Multi-Language

-- ================================================================
-- Users table with security and multi-language features
-- ================================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role ENUM('SUPER_ADMIN', 'TENANT_ADMIN', 'EDITOR', 'VIEWER') NOT NULL DEFAULT 'VIEWER',
    preferred_language ENUM('TR', 'EN') NOT NULL DEFAULT 'TR',
    tenant_id BIGINT NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    job_title VARCHAR(100),
    department VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    password_changed_at TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    notes VARCHAR(500),
    
    UNIQUE KEY uk_user_email (email),
    UNIQUE KEY uk_user_email_tenant (email, tenant_id),
    INDEX idx_user_tenant (tenant_id),
    INDEX idx_user_email (email),
    INDEX idx_user_role (role),
    INDEX idx_user_active (is_active),
    INDEX idx_user_created_at (created_at),
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Content types for flexible CMS structure
-- ================================================================
CREATE TABLE content_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    display_name_tr VARCHAR(100),
    display_name_en VARCHAR(100),
    description VARCHAR(500),
    description_tr VARCHAR(500),
    description_en VARCHAR(500),
    fields TEXT, -- JSON schema for custom fields
    tenant_id BIGINT NOT NULL,
    supports_multi_language BOOLEAN DEFAULT TRUE,
    supports_seo BOOLEAN DEFAULT TRUE,
    supports_scheduling BOOLEAN DEFAULT TRUE,
    supports_comments BOOLEAN DEFAULT FALSE,
    requires_approval BOOLEAN DEFAULT FALSE,
    is_system_type BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    icon VARCHAR(50) DEFAULT 'document',
    color VARCHAR(20) DEFAULT '#3B82F6',
    max_items INT, -- NULL = unlimited
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    UNIQUE KEY uk_content_type_name_tenant (name, tenant_id),
    INDEX idx_content_type_tenant (tenant_id),
    INDEX idx_content_type_name (name),
    INDEX idx_content_type_active (is_active),
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Contents table with multi-language and SEO support
-- ================================================================
CREATE TABLE contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    excerpt VARCHAR(500),
    data TEXT, -- JSON content data
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED', 'SCHEDULED', 'REVIEWING') NOT NULL DEFAULT 'DRAFT',
    language ENUM('TR', 'EN') NOT NULL DEFAULT 'TR',
    parent_content_id BIGINT, -- For translations
    content_type_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    
    -- SEO fields
    meta_title VARCHAR(60),
    meta_description VARCHAR(160),
    meta_keywords VARCHAR(100),
    canonical_url VARCHAR(255),
    og_image VARCHAR(255),
    no_index BOOLEAN DEFAULT FALSE,
    no_follow BOOLEAN DEFAULT FALSE,
    
    -- Publishing
    published_at TIMESTAMP,
    scheduled_at TIMESTAMP,
    expires_at TIMESTAMP,
    
    -- Organization
    sort_order INT DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
    is_sticky BOOLEAN DEFAULT FALSE,
    view_count BIGINT DEFAULT 0,
    comment_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    
    -- Template and layout
    template VARCHAR(50) DEFAULT 'default',
    layout VARCHAR(50) DEFAULT 'default',
    
    -- Access control
    is_password_protected BOOLEAN DEFAULT FALSE,
    content_password VARCHAR(255),
    requires_login BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    published_by BIGINT,
    notes VARCHAR(1000),
    
    UNIQUE KEY uk_content_slug_tenant_lang (tenant_id, slug, language),
    INDEX idx_content_tenant (tenant_id),
    INDEX idx_content_slug (slug),
    INDEX idx_content_status (status),
    INDEX idx_content_language (language),
    INDEX idx_content_type (content_type_id),
    INDEX idx_content_parent (parent_content_id),
    INDEX idx_content_published_at (published_at),
    INDEX idx_content_created_at (created_at),
    INDEX idx_content_view_count (view_count),
    INDEX idx_content_featured (is_featured),
    INDEX idx_content_sticky (is_sticky),
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (content_type_id) REFERENCES content_types(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_content_id) REFERENCES contents(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON SET NULL,
    FOREIGN KEY (published_by) REFERENCES users(id) ON SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Media files with multi-language metadata
-- ================================================================
CREATE TABLE media_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL UNIQUE,
    file_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_extension VARCHAR(50),
    
    -- Image properties
    width INT,
    height INT,
    has_thumbnails BOOLEAN DEFAULT FALSE,
    thumbnail_path VARCHAR(500),
    
    -- Multi-language metadata
    alt_text_tr VARCHAR(255),
    alt_text_en VARCHAR(255),
    description_tr VARCHAR(500),
    description_en VARCHAR(500),
    title_tr VARCHAR(200),
    title_en VARCHAR(200),
    
    -- Organization
    folder VARCHAR(100) DEFAULT 'uploads',
    category VARCHAR(100) DEFAULT 'general',
    tags TEXT, -- JSON array of tags
    
    -- Access and usage
    tenant_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    is_public BOOLEAN DEFAULT FALSE,
    is_optimized BOOLEAN DEFAULT FALSE,
    usage_count INT DEFAULT 0,
    
    -- External storage
    storage_provider VARCHAR(20) DEFAULT 'local',
    external_url VARCHAR(500),
    external_id VARCHAR(100),
    
    -- Metadata
    metadata TEXT, -- JSON metadata (EXIF, duration, etc.)
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP,
    
    UNIQUE KEY uk_media_filename (file_name),
    INDEX idx_media_tenant (tenant_id),
    INDEX idx_media_filename (file_name),
    INDEX idx_media_mimetype (mime_type),
    INDEX idx_media_folder (folder),
    INDEX idx_media_category (category),
    INDEX idx_media_uploader (uploaded_by),
    INDEX idx_media_created_at (created_at),
    INDEX idx_media_size (file_size),
    INDEX idx_media_usage (usage_count),
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Insert default content types for each tenant
-- ================================================================
INSERT INTO content_types (name, display_name, display_name_tr, display_name_en, description_tr, description_en, tenant_id, is_system_type, sort_order, icon, created_by)
SELECT 
    'page' as name,
    'Sayfa' as display_name,
    'Sayfa' as display_name_tr,
    'Page' as display_name_en,
    'Statik sayfalar için kullanılır' as description_tr,
    'Used for static pages' as description_en,
    t.id as tenant_id,
    TRUE as is_system_type,
    1 as sort_order,
    'file-text' as icon,
    1 as created_by
FROM tenants t
WHERE t.status = 'ACTIVE';

INSERT INTO content_types (name, display_name, display_name_tr, display_name_en, description_tr, description_en, tenant_id, is_system_type, sort_order, icon, created_by)
SELECT 
    'post' as name,
    'Blog Yazısı' as display_name,
    'Blog Yazısı' as display_name_tr,
    'Blog Post' as display_name_en,
    'Blog yazıları için kullanılır' as description_tr,
    'Used for blog posts' as description_en,
    t.id as tenant_id,
    TRUE as is_system_type,
    2 as sort_order,
    'edit' as icon,
    1 as created_by
FROM tenants t
WHERE t.status = 'ACTIVE';

-- ================================================================
-- Create indexes for better query performance
-- ================================================================

-- Multi-language content queries
CREATE INDEX idx_content_lang_status ON contents(language, status);
CREATE INDEX idx_content_tenant_lang_status ON contents(tenant_id, language, status);

-- Publishing workflow indexes
CREATE INDEX idx_content_scheduled ON contents(status, scheduled_at) WHERE status = 'SCHEDULED';
CREATE INDEX idx_content_expires ON contents(expires_at) WHERE expires_at IS NOT NULL;

-- Media file type queries
CREATE INDEX idx_media_type_tenant ON media_files(tenant_id, mime_type);
CREATE INDEX idx_media_folder_tenant ON media_files(tenant_id, folder);

-- User security queries
CREATE INDEX idx_user_login_attempts ON users(failed_login_attempts) WHERE failed_login_attempts > 0;
CREATE INDEX idx_user_locked ON users(locked_until) WHERE locked_until IS NOT NULL;

-- ================================================================
-- Create full-text search indexes for content
-- ================================================================
ALTER TABLE contents ADD FULLTEXT INDEX ft_content_search (title, excerpt, data);
ALTER TABLE media_files ADD FULLTEXT INDEX ft_media_search (original_name, alt_text_tr, alt_text_en, description_tr, description_en);