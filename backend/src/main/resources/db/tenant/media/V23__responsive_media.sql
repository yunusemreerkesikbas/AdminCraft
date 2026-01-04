-- =====================================================
-- V23: Responsive Media Set & Component Media Links
-- Sprint 37: Component-Media Integration
-- =====================================================

-- ResponsiveMediaSet: Stores Desktop/Mobile image pairs
-- Used by both Components (component level) and Entries (entry level)
CREATE TABLE responsive_media_set (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    code VARCHAR(100) NOT NULL,
    
    -- Desktop image reference
    desktop_media_id BIGINT,
    
    -- Mobile image reference
    mobile_media_id BIGINT,
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    -- Unique constraints
    CONSTRAINT uk_responsive_media_uuid UNIQUE (uuid),
    CONSTRAINT uk_responsive_media_uid UNIQUE (uid),
    CONSTRAINT uk_responsive_media_code UNIQUE (code),
    
    -- Foreign keys to media table
    CONSTRAINT fk_responsive_desktop_media 
        FOREIGN KEY (desktop_media_id) REFERENCES media(id) ON DELETE SET NULL,
    CONSTRAINT fk_responsive_mobile_media 
        FOREIGN KEY (mobile_media_id) REFERENCES media(id) ON DELETE SET NULL,
    
    -- Indexes
    INDEX idx_responsive_desktop (desktop_media_id),
    INDEX idx_responsive_mobile (mobile_media_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ComponentMediaLinks: Track which components/entries use which media
-- Enables "Linked Components" feature in Media Detail dialog
CREATE TABLE component_media_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Component reference (always required)
    component_id BIGINT NOT NULL,
    
    -- Media reference
    media_id BIGINT NOT NULL,
    
    -- Link type for categorization (VARCHAR for JPA compatibility)
    link_type VARCHAR(20) NOT NULL DEFAULT 'ENTRY_MEDIA',
    
    -- Entry reference (NULL for component-level links)
    entry_id BIGINT NULL,
    
    -- Responsive media set reference (for component/entry responsive links)
    responsive_set_id BIGINT NULL,
    
    -- Timestamp
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_cml_component 
        FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE,
    CONSTRAINT fk_cml_media 
        FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE,
    CONSTRAINT fk_cml_entry 
        FOREIGN KEY (entry_id) REFERENCES component_entries(id) ON DELETE CASCADE,
    CONSTRAINT fk_cml_responsive_set
        FOREIGN KEY (responsive_set_id) REFERENCES responsive_media_set(id) ON DELETE CASCADE,
    
    -- Indexes for efficient lookups
    INDEX idx_cml_media (media_id),
    INDEX idx_cml_component (component_id),
    INDEX idx_cml_entry (entry_id),
    INDEX idx_cml_responsive_set (responsive_set_id),
    
    -- Prevent duplicate links
    -- NOTE: This constraint includes entry_id which can be NULL for component-level links.
    -- In MySQL, multiple rows with NULL values in a unique constraint are allowed.
    -- This means multiple component-level responsive links with the same component_id,
    -- media_id, and link_type can exist if entry_id is NULL. This is intentional to
    -- allow multiple component-level media assignments across different responsive sets.
    UNIQUE KEY uk_cml_component_media_type (component_id, media_id, link_type, entry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
