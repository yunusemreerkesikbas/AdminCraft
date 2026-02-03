-- Verification tokens table for OTP, password reset, and email verification
CREATE TABLE verification_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    token_type ENUM('EMAIL_VERIFY', 'PASSWORD_RESET', 'LOGIN_OTP', 'OPERATION_OTP') NOT NULL,
    status ENUM('ACTIVE', 'USED', 'EXPIRED', 'REVOKED') DEFAULT 'ACTIVE',
    target_value VARCHAR(255),
    expires_at DATETIME NOT NULL,
    attempt_count INT DEFAULT 0,
    max_attempts INT DEFAULT 5,
    used_at DATETIME,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_token_user (user_id),
    INDEX idx_token_hash (token_hash),
    INDEX idx_token_type_status (token_type, status),
    INDEX idx_token_expires (expires_at),

    CONSTRAINT fk_verification_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
