CREATE TABLE config_change_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT NOT NULL,
    actor_email VARCHAR(255) NOT NULL,
    actor_role VARCHAR(50) NOT NULL,
    target_tenant_id BIGINT NOT NULL,
    scope VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    before_json TEXT NULL,
    after_json TEXT NULL,
    reason VARCHAR(500) NOT NULL,
    correlation_id VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_config_audit_tenant_created (target_tenant_id, created_at),
    INDEX idx_config_audit_actor_created (actor_user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
