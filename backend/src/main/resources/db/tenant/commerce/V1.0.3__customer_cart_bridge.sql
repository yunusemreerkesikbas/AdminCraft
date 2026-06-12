ALTER TABLE commerce_carts
    ADD COLUMN customer_id BIGINT NULL AFTER token_hash,
    ADD CONSTRAINT fk_commerce_cart_customer FOREIGN KEY (customer_id)
        REFERENCES commerce_customers(id) ON DELETE SET NULL,
    ADD INDEX idx_commerce_cart_customer_status_expires (customer_id, status, expires_at);
