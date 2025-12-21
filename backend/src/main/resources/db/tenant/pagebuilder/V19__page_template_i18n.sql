-- V19: Page Template i18n table
-- Sprint 34A: Add multi-language support to PageTemplate
-- Language uses VARCHAR(10) to allow adding languages without schema migration

CREATE TABLE page_template_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(50) NOT NULL,
    uuid CHAR(36) NOT NULL,
    template_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    UNIQUE KEY uk_template_i18n_lang (template_id, language),
    UNIQUE KEY uk_template_i18n_uid (uid),
    KEY idx_template_i18n_uuid (uuid),
    KEY idx_template_i18n_template_id (template_id),
    KEY idx_template_i18n_language (language),
    CONSTRAINT fk_template_i18n_template FOREIGN KEY (template_id)
        REFERENCES page_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Migrate existing page_templates data to TR (with audit fields)
INSERT INTO page_template_i18n (uid, uuid, template_id, language, name, description, created_at, updated_at, created_by, updated_by)
SELECT
    CONCAT('templatei18n_', LPAD(id, 8, '0')),
    UUID(),
    id,
    'TR',
    name,
    description,
    COALESCE(created_at, NOW()),
    COALESCE(updated_at, NOW()),
    created_by,
    updated_by
FROM page_templates
WHERE name IS NOT NULL AND name != '';

-- Drop old columns from page_templates
ALTER TABLE page_templates DROP COLUMN name;
ALTER TABLE page_templates DROP COLUMN description;
