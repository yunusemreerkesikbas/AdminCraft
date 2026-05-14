CREATE TABLE cms_draft_overrides (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    uid VARCHAR(50) NOT NULL UNIQUE,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    language_key VARCHAR(10) NOT NULL DEFAULT '_',
    payload LONGTEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_cms_draft_target (target_type, target_id, language_key),
    INDEX idx_cms_draft_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
