-- =====================================================
-- V15: Component Media Links
-- Sprint 37: Component-Media Integration
-- =====================================================

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

    -- Generated column to make UNIQUE constraint work with NULL entry_id
    -- MySQL treats NULLs as distinct in unique keys, so we coalesce to 0
    entry_id_key BIGINT GENERATED ALWAYS AS (COALESCE(entry_id, 0)) STORED,

    -- Prevent duplicate links (uses entry_id_key to handle NULL correctly)
    UNIQUE KEY uk_cml_component_media_type (component_id, media_id, link_type, entry_id_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
