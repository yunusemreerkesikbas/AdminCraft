CREATE TABLE platform_email_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL DEFAULT (UUID()),
    uid VARCHAR(50) NOT NULL DEFAULT (CONCAT('plt_et_', LPAD(FLOOR(RAND() * 100000000), 8, '0'))),
    template_key VARCHAR(100) NOT NULL,
    language VARCHAR(10) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_platform_email_template_key_lang (template_key, language),
    UNIQUE KEY uk_platform_email_template_uuid (uuid),
    UNIQUE KEY uk_platform_email_template_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE platform_newsletter_subscribers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL DEFAULT (UUID()),
    uid VARCHAR(50) NOT NULL DEFAULT (CONCAT('plt_ns_', LPAD(FLOOR(RAND() * 100000000), 8, '0'))),
    email VARCHAR(255) NOT NULL,
    status VARCHAR(40) NOT NULL,
    source VARCHAR(120) NULL,
    confirm_token VARCHAR(255) NULL,
    unsubscribe_token VARCHAR(255) NULL,
    confirmed_at TIMESTAMP NULL,
    unsubscribed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_platform_newsletter_email (email),
    UNIQUE KEY uk_platform_newsletter_subscriber_uuid (uuid),
    UNIQUE KEY uk_platform_newsletter_subscriber_uid (uid),
    INDEX idx_platform_newsletter_status (status),
    INDEX idx_platform_newsletter_confirm_token (confirm_token),
    INDEX idx_platform_newsletter_unsubscribe_token (unsubscribe_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE platform_mail_campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL DEFAULT (UUID()),
    uid VARCHAR(50) NOT NULL DEFAULT (CONCAT('plt_mc_', LPAD(FLOOR(RAND() * 100000000), 8, '0'))),
    template_id BIGINT NULL,
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    total_count INT NOT NULL DEFAULT 0,
    sent_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    created_by_email VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_platform_mail_campaign_uuid (uuid),
    UNIQUE KEY uk_platform_mail_campaign_uid (uid),
    CONSTRAINT fk_platform_campaign_template FOREIGN KEY (template_id) REFERENCES platform_email_templates(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE platform_mail_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL DEFAULT (UUID()),
    uid VARCHAR(50) NOT NULL DEFAULT (CONCAT('plt_mo_', LPAD(FLOOR(RAND() * 100000000), 8, '0'))),
    campaign_id BIGINT NOT NULL,
    to_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    provider_message_id VARCHAR(255) NULL,
    error_message TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_platform_mail_outbox_uuid (uuid),
    UNIQUE KEY uk_platform_mail_outbox_uid (uid),
    CONSTRAINT fk_platform_outbox_campaign FOREIGN KEY (campaign_id) REFERENCES platform_mail_campaigns(id) ON DELETE CASCADE,
    INDEX idx_platform_outbox_campaign (campaign_id),
    INDEX idx_platform_outbox_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO platform_email_templates (template_key, language, subject, content, is_active)
VALUES
('NEWSLETTER_DEFAULT', 'TR', 'Bulten Bilgilendirmesi', 'Merhaba {{name}},\n\n{{content}}\n\nIptal: {{unsubscribeUrl}}', TRUE),
('NEWSLETTER_DEFAULT', 'EN', 'Newsletter Update', 'Hello {{name}},\n\n{{content}}\n\nUnsubscribe: {{unsubscribeUrl}}', TRUE);
