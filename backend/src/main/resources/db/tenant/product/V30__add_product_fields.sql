-- =====================================================
-- V30: Global Product Fields
-- =====================================================
-- Creates tables for global product field definitions
-- and their values. These fields are visible across ALL
-- products regardless of ProductType.
-- =====================================================

-- Product Field Definitions (Global)
CREATE TABLE product_field_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    field_type VARCHAR(20) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    is_visible_in_list BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    default_value TEXT,
    validation_config JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_pfd_uuid UNIQUE (uuid),
    CONSTRAINT uk_pfd_uid UNIQUE (uid),
    CONSTRAINT uk_pfd_code UNIQUE (code),
    INDEX idx_pfd_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Product Field Values
CREATE TABLE product_field_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    field_definition_id BIGINT NOT NULL,
    value_text TEXT,
    value_number DECIMAL(19,4),
    value_boolean BOOLEAN,
    value_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_pfv_product FOREIGN KEY (product_id) 
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_pfv_definition FOREIGN KEY (field_definition_id) 
        REFERENCES product_field_definitions(id) ON DELETE CASCADE,
    CONSTRAINT uk_pfv_product_field UNIQUE (product_id, field_definition_id),
    INDEX idx_pfv_product (product_id),
    INDEX idx_pfv_definition (field_definition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
