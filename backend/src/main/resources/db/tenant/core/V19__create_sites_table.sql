-- Tenant Core Module: Create Sites Table
-- Fixes missing sites table error (V19)

CREATE TABLE sites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Basic Info
    site_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    theme_name VARCHAR(255) DEFAULT 'default',
    
    -- Domain & SSL
    domain VARCHAR(255),
    custom_domain VARCHAR(255),
    is_ssl_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Language
    default_language VARCHAR(20) NOT NULL DEFAULT 'TR',
    
    -- SEO
    site_title VARCHAR(60),
    site_description VARCHAR(160),
    site_keywords VARCHAR(200),
    og_image_url VARCHAR(255),
    
    -- Social
    twitter_handle VARCHAR(50),
    facebook_page_url VARCHAR(255),
    
    -- Analytics
    google_analytics_id VARCHAR(50),
    google_tag_manager_id VARCHAR(50),
    
    -- Status & Maintenance
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP NULL,
    maintenance_mode BOOLEAN NOT NULL DEFAULT FALSE,
    maintenance_message VARCHAR(500),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Site Languages Collection Table
CREATE TABLE site_languages (
    site_id BIGINT NOT NULL,
    language VARCHAR(20) NOT NULL,
    PRIMARY KEY (site_id, language),
    CONSTRAINT fk_site_languages_site FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default site if not exists (optional, but good for empty state)
-- INSERT INTO sites (site_name, default_language, is_ssl_enabled, created_at, updated_at)
-- VALUES ('My Awesome Site', 'TR', true, NOW(), NOW());
