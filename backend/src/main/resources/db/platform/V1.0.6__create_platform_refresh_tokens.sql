CREATE TABLE platform_refresh_tokens (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    token_hash VARCHAR(64)  NOT NULL,
    expires_at DATETIME     NOT NULL,
    revoked_at DATETIME     NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_platform_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_platform_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_platform_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES platform_admin_users (id) ON DELETE CASCADE,
    INDEX idx_platform_refresh_tokens_hash (token_hash),
    INDEX idx_platform_refresh_tokens_user (user_id),
    INDEX idx_platform_refresh_tokens_expires (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
