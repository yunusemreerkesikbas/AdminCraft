-- Sprint 7: Media Management Database Schema
-- Clean Architecture + Multi-Language Support
-- Date: 2025-01-14

-- Drop existing tables if they exist (development only)
-- DROP TABLE IF EXISTS media_usages;
-- DROP TABLE IF EXISTS media_files;

-- Create media_files table with Sprint 7 JSON structure
CREATE TABLE IF NOT EXISTS media_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Basic file information
    original_name VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL UNIQUE,
    file_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_extension VARCHAR(50),
    
    -- Image properties
    width INT NULL,
    height INT NULL,
    
    -- Sprint 7: JSON-based i18n structure for localized metadata
    -- JSON: {"tr": {"title":"","subtitle":"","altText":"","seo":{"title":"","description":"","keywords":[]}}, "en": {...}}
    i18n JSON NULL,
    
    -- Sprint 7: JSON variants structure for desktop/mobile variants  
    -- JSON: {"desktop":{"url":"","width":1200,"height":800}, "mobile":{"url":"","width":768,"height":512}}
    variants JSON NULL,
    
    -- Sprint 7: Content hash for de-duplication
    content_hash VARCHAR(64) NULL, -- SHA-256 hash
    
    -- Thumbnail support
    has_thumbnails BOOLEAN DEFAULT FALSE,
    thumbnail_path VARCHAR(500) NULL,
    
    -- Organization
    folder VARCHAR(100) DEFAULT 'uploads',
    category VARCHAR(100) DEFAULT 'general',
    tags JSON NULL, -- JSON array of tags
    
    -- Sprint 7: Tenant isolation (CRITICAL)
    tenant_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    
    -- Access and usage
    is_public BOOLEAN DEFAULT FALSE,
    is_optimized BOOLEAN DEFAULT FALSE,
    usage_count INT DEFAULT 0,
    
    -- Sprint 7: Staged upload system
    status ENUM('STAGED', 'ACTIVE', 'ARCHIVED') DEFAULT 'STAGED',
    
    -- External storage support
    storage_provider VARCHAR(20) DEFAULT 'local',
    external_url VARCHAR(500) NULL,
    external_id VARCHAR(100) NULL,
    
    -- Metadata
    metadata JSON NULL, -- EXIF, duration, etc.
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Sprint 7: Critical tenant isolation constraints
    CONSTRAINT fk_media_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create media_usages table for Sprint 7 attachment system
CREATE TABLE IF NOT EXISTS media_usages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Media reference
    media_id BIGINT NOT NULL,
    
    -- Owner information
    owner_type VARCHAR(50) NOT NULL, -- PAGE, BLOCK, FORM_FIELD, etc.
    owner_id BIGINT NOT NULL,
    
    -- Sprint 7: Usage purpose enum
    purpose ENUM('BREADCRUMB', 'THUMBNAIL', 'SLIDER', 'GALLERY', 'ICON', 'BACKGROUND', 'LOGO', 'HERO', 'CARD') NOT NULL,
    
    -- Sprint 7: Single cover per owner enforcement
    is_cover BOOLEAN DEFAULT FALSE,
    
    -- Sprint 7: Drag-drop reordering
    sort_order INT DEFAULT 0,
    
    -- Tenant isolation
    tenant_id BIGINT NOT NULL,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Sprint 7: Critical constraints
    CONSTRAINT fk_usage_media_id FOREIGN KEY (media_id) REFERENCES media_files(id) ON DELETE CASCADE,
    CONSTRAINT fk_usage_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    
    -- Sprint 7: Single cover per owner constraint
    CONSTRAINT uk_single_cover_per_owner UNIQUE (owner_type, owner_id, is_cover),
    
    -- Sprint 7: Tenant isolation constraint
    CONSTRAINT chk_tenant_consistency CHECK (tenant_id = (SELECT tenant_id FROM media_files WHERE id = media_id))
);

-- Sprint 7: Critical tenant isolation indices
CREATE INDEX idx_media_tenant_id ON media_files(tenant_id);
CREATE INDEX idx_media_content_hash ON media_files(content_hash, tenant_id);
CREATE INDEX idx_media_status_tenant ON media_files(status, tenant_id);
CREATE INDEX idx_media_created_tenant ON media_files(created_at DESC, tenant_id);

-- Standard performance indices
CREATE INDEX idx_media_filename ON media_files(file_name);
CREATE INDEX idx_media_mimetype ON media_files(mime_type);
CREATE INDEX idx_media_folder ON media_files(folder);
CREATE INDEX idx_media_category ON media_files(category);
CREATE INDEX idx_media_uploader ON media_files(uploaded_by);
CREATE INDEX idx_media_created_at ON media_files(created_at DESC);

-- Media usage indices
CREATE INDEX idx_usage_media_id ON media_usages(media_id);
CREATE INDEX idx_usage_owner ON media_usages(owner_type, owner_id);
CREATE INDEX idx_usage_tenant_id ON media_usages(tenant_id);
CREATE INDEX idx_usage_purpose ON media_usages(purpose);
CREATE INDEX idx_usage_sort_order ON media_usages(sort_order);
CREATE INDEX idx_usage_cover ON media_usages(is_cover, owner_type, owner_id);

-- Sprint 7: Tenant-scoped performance indices (composite)
CREATE INDEX idx_media_tenant_status_created ON media_files(tenant_id, status, created_at DESC);
CREATE INDEX idx_usage_tenant_owner ON media_usages(tenant_id, owner_type, owner_id, sort_order);