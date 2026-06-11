-- Product Catalog Variant Foundation

CREATE TABLE product_variant_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT uk_product_variant_option_uuid UNIQUE (uuid),
    CONSTRAINT uk_product_variant_option_uid UNIQUE (uid),
    CONSTRAINT uk_product_variant_option_code UNIQUE (code),
    INDEX idx_product_variant_option_active (active),
    INDEX idx_product_variant_option_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_variant_option_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    option_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    label VARCHAR(100) NOT NULL,
    swatch_value VARCHAR(50) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT uk_product_variant_option_value_uuid UNIQUE (uuid),
    CONSTRAINT uk_product_variant_option_value_uid UNIQUE (uid),
    CONSTRAINT uk_product_variant_option_value_code UNIQUE (option_id, code),
    CONSTRAINT fk_product_variant_option_value_option FOREIGN KEY (option_id)
        REFERENCES product_variant_options(id) ON DELETE CASCADE,
    INDEX idx_product_variant_option_value_option (option_id),
    INDEX idx_product_variant_option_value_active (active),
    INDEX idx_product_variant_option_value_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_variants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL,
    price DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    first_price DECIMAL(15,2) NULL,
    vat_rate DECIMAL(5,2) NOT NULL DEFAULT 20.00,
    stock_quantity INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    responsive_id BIGINT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT uk_product_variant_uuid UNIQUE (uuid),
    CONSTRAINT uk_product_variant_uid UNIQUE (uid),
    CONSTRAINT uk_product_variant_sku UNIQUE (sku),
    CONSTRAINT fk_product_variant_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_variant_responsive FOREIGN KEY (responsive_id)
        REFERENCES responsive_media_set(id) ON DELETE SET NULL,
    CONSTRAINT chk_product_variant_price_non_negative CHECK (price >= 0),
    CONSTRAINT chk_product_variant_first_price_non_negative CHECK (first_price IS NULL OR first_price >= 0),
    CONSTRAINT chk_product_variant_vat_rate_non_negative CHECK (vat_rate >= 0),
    CONSTRAINT chk_product_variant_stock_non_negative CHECK (stock_quantity >= 0),
    INDEX idx_product_variant_product (product_id),
    INDEX idx_product_variant_active (active),
    INDEX idx_product_variant_responsive (responsive_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_variant_value_links (
    variant_id BIGINT NOT NULL,
    option_value_id BIGINT NOT NULL,
    PRIMARY KEY (variant_id, option_value_id),
    CONSTRAINT fk_product_variant_value_link_variant FOREIGN KEY (variant_id)
        REFERENCES product_variants(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_variant_value_link_value FOREIGN KEY (option_value_id)
        REFERENCES product_variant_option_values(id) ON DELETE RESTRICT,
    INDEX idx_product_variant_value_link_value (option_value_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO product_variants (
    uuid,
    uid,
    product_id,
    sku,
    price,
    first_price,
    vat_rate,
    stock_quantity,
    active,
    responsive_id,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT
    UUID(),
    CONCAT('variant_', p.id),
    p.id,
    p.sku,
    COALESCE(p.base_price, 0.00),
    NULL,
    20.00,
    0,
    TRUE,
    p.responsive_id,
    NOW(),
    NOW(),
    p.created_by,
    p.updated_by
FROM products p;
