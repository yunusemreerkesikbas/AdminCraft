ALTER TABLE commerce_orders
    ADD COLUMN shipping_carrier_name VARCHAR(100) NULL,
    ADD COLUMN shipping_tracking_number VARCHAR(100) NULL,
    ADD COLUMN shipping_tracking_url VARCHAR(500) NULL,
    ADD COLUMN shipped_at DATETIME NULL,
    ADD COLUMN delivered_at DATETIME NULL,
    ADD COLUMN status_changed_at DATETIME NULL;

CREATE TABLE commerce_order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid VARCHAR(50) NOT NULL,
    order_id BIGINT NOT NULL,
    from_status VARCHAR(30) NOT NULL,
    to_status VARCHAR(30) NOT NULL,
    shipping_carrier_name VARCHAR(100) NULL,
    shipping_tracking_number VARCHAR(100) NULL,
    shipping_tracking_url VARCHAR(500) NULL,
    internal_note VARCHAR(1000) NULL,
    changed_by_user_id BIGINT NULL,
    changed_by_email VARCHAR(191) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT uk_commerce_order_status_history_uuid UNIQUE (uuid),
    CONSTRAINT uk_commerce_order_status_history_uid UNIQUE (uid),
    CONSTRAINT fk_commerce_order_status_history_order FOREIGN KEY (order_id)
        REFERENCES commerce_orders(id) ON DELETE CASCADE,
    INDEX idx_commerce_order_status_history_order_created (order_id, created_at),
    INDEX idx_commerce_order_status_history_to_status_created (to_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
