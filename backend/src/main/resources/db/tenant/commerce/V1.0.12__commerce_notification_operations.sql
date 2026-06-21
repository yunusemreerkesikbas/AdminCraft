ALTER TABLE commerce_notification_outbox
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0 AFTER status,
    ADD COLUMN last_attempted_at TIMESTAMP NULL AFTER attempt_count,
    ADD COLUMN next_retry_at TIMESTAMP NULL AFTER last_attempted_at;

CREATE INDEX idx_commerce_notification_outbox_retry
    ON commerce_notification_outbox (status, next_retry_at, created_at);
