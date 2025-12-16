-- Navigation Node System
-- Hierarchical navigation nodes for mega menus

CREATE TABLE navigation_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(200) NULL,
    parent_id BIGINT NULL,
    position VARCHAR(20) DEFAULT 'LEFT',
    sort_order INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    is_tab BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,

    INDEX idx_nav_parent (parent_id),
    INDEX idx_nav_sort (sort_order),
    CONSTRAINT fk_nav_parent FOREIGN KEY (parent_id)
        REFERENCES navigation_nodes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 10000;

CREATE TABLE navigation_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(100) NOT NULL UNIQUE,
    node_id BIGINT NOT NULL,
    item_type VARCHAR(30) NOT NULL,
    item_id VARCHAR(100) NULL,
    url VARCHAR(500) NULL,
    link_name VARCHAR(200) NOT NULL,
    link_color VARCHAR(10) NULL,
    target VARCHAR(20) DEFAULT '_self',
    is_external BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,

    INDEX idx_entry_node (node_id),
    INDEX idx_entry_sort (sort_order),
    CONSTRAINT fk_entry_node FOREIGN KEY (node_id)
        REFERENCES navigation_nodes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT = 10000;
