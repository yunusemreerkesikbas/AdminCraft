-- Sprint 27: Page Template System
-- Creates page_templates and template_slots tables for reusable page layouts

CREATE TABLE IF NOT EXISTS page_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    is_system BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_page_template_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS template_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    template_id BIGINT NOT NULL,
    slot_name VARCHAR(50) NOT NULL,
    position VARCHAR(20) NOT NULL,
    sort_order INT DEFAULT 0,
    is_required BOOLEAN DEFAULT FALSE,
    max_components INT NULL,
    allowed_types JSON NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_template_slot (template_id, slot_name),
    INDEX idx_template_slot_template (template_id),
    INDEX idx_template_slot_sort_order (sort_order),
    FOREIGN KEY (template_id) REFERENCES page_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add template_id reference to pages table
ALTER TABLE pages ADD COLUMN template_id BIGINT NULL;
CREATE INDEX idx_page_template ON pages (template_id);
ALTER TABLE pages ADD CONSTRAINT fk_page_template
    FOREIGN KEY (template_id) REFERENCES page_templates(id) ON DELETE SET NULL;
