CREATE INDEX idx_commerce_order_created_at ON commerce_orders (created_at);
CREATE INDEX idx_commerce_order_status_created ON commerce_orders (status, created_at);
CREATE INDEX idx_commerce_order_attention_created ON commerce_orders (requires_attention, created_at);

CREATE INDEX idx_commerce_payment_attempt_status_created ON commerce_payment_attempts (status, created_at);
