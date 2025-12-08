-- Content Slot System for Page-Component Integration
-- Sprint 26: SAP Commerce Cloud patterns

-- Page Slots table
-- Shared slots have page_id = NULL (global slots like Header, Footer)
CREATE TABLE page_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'Server-generated UUID',
    uid VARCHAR(50) NOT NULL UNIQUE COMMENT 'Human-readable stable identifier',
    page_id BIGINT NULL COMMENT 'NULL = shared/global slot',
    slot_name VARCHAR(50) NOT NULL COMMENT 'Descriptive name: Header, MainContent, Footer',
    position VARCHAR(20) NOT NULL COMMENT 'Layout position: top, center, bottom, left, right',
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_shared BOOLEAN DEFAULT FALSE COMMENT 'Explicit shared flag for global slots',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_page_slots_page (page_id),
    INDEX idx_page_slots_shared (is_shared),
    INDEX idx_page_slots_active (is_active),
    UNIQUE KEY uk_page_slot (page_id, slot_name),
    FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Slot Components junction table
-- Links components to slots with ordering
CREATE TABLE slot_components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_id BIGINT NOT NULL,
    component_id BIGINT NOT NULL,
    sort_order INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_slot_components_slot (slot_id),
    INDEX idx_slot_components_component (component_id),
    INDEX idx_slot_components_order (slot_id, sort_order),
    UNIQUE KEY uk_slot_component (slot_id, component_id),
    FOREIGN KEY (slot_id) REFERENCES page_slots(id) ON DELETE CASCADE,
    FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SEO fields for Page entity (Sprint 27 preparation)
ALTER TABLE pages ADD COLUMN robot_tag VARCHAR(50) DEFAULT 'INDEX_FOLLOW' COMMENT 'Meta robots directive';
ALTER TABLE pages ADD COLUMN template_uid VARCHAR(50) NULL COMMENT 'Future: PageTemplate reference';
