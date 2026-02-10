CREATE TABLE platform_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform_admin_user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    token_type ENUM('LOGIN_OTP') NOT NULL,
    status ENUM('ACTIVE', 'USED', 'EXPIRED', 'REVOKED') NOT NULL DEFAULT 'ACTIVE',
    target_value VARCHAR(255) NULL,
    expires_at DATETIME NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 5,
    used_at DATETIME NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_platform_verification_token_admin_user
        FOREIGN KEY (platform_admin_user_id) REFERENCES platform_admin_users(id) ON DELETE CASCADE,
    CONSTRAINT uk_platform_verification_token_hash UNIQUE (token_hash),
    INDEX idx_platform_token_admin_user (platform_admin_user_id),
    INDEX idx_platform_token_type_status (token_type, status),
    INDEX idx_platform_token_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
