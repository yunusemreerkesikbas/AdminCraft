-- V18: Navigation i18n tables
-- Sprint 34A: Add multi-language support to NavigationNode and NavigationEntry
-- Language uses VARCHAR(10) to allow adding languages without schema migration

-- Navigation Node i18n
CREATE TABLE navigation_node_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(50) NOT NULL,
    uuid CHAR(36) NOT NULL,
    node_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,
    title VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    UNIQUE KEY uk_nav_node_i18n_lang (node_id, language),
    UNIQUE KEY uk_nav_node_i18n_uid (uid),
    KEY idx_nav_node_i18n_uuid (uuid),
    KEY idx_nav_node_i18n_node_id (node_id),
    KEY idx_nav_node_i18n_language (language),
    CONSTRAINT fk_nav_node_i18n_node FOREIGN KEY (node_id)
        REFERENCES navigation_nodes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Navigation Entry i18n
CREATE TABLE navigation_entry_i18n (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(50) NOT NULL,
    uuid CHAR(36) NOT NULL,
    entry_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,
    link_name VARCHAR(200) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    UNIQUE KEY uk_nav_entry_i18n_lang (entry_id, language),
    UNIQUE KEY uk_nav_entry_i18n_uid (uid),
    KEY idx_nav_entry_i18n_uuid (uuid),
    KEY idx_nav_entry_i18n_entry_id (entry_id),
    KEY idx_nav_entry_i18n_language (language),
    CONSTRAINT fk_nav_entry_i18n_entry FOREIGN KEY (entry_id)
        REFERENCES navigation_entries(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Migrate existing navigation_nodes.title data to TR (with audit fields)
INSERT INTO navigation_node_i18n (uid, uuid, node_id, language, title, created_at, updated_at, created_by, updated_by)
SELECT
    CONCAT('navnodei18n_', LPAD(id, 8, '0')),
    UUID(),
    id,
    'TR',
    title,
    COALESCE(created_at, NOW()),
    COALESCE(updated_at, NOW()),
    created_by,
    updated_by
FROM navigation_nodes
WHERE title IS NOT NULL AND title != '';

-- Migrate existing navigation_entries.link_name data to TR (with audit fields)
INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT
    CONCAT('naventryi18n_', LPAD(id, 8, '0')),
    UUID(),
    id,
    'TR',
    link_name,
    COALESCE(created_at, NOW()),
    COALESCE(updated_at, NOW()),
    created_by,
    updated_by
FROM navigation_entries
WHERE link_name IS NOT NULL AND link_name != '';

-- Drop old columns from navigation_nodes
ALTER TABLE navigation_nodes DROP COLUMN title;

-- Drop old columns from navigation_entries
ALTER TABLE navigation_entries DROP COLUMN link_name;
