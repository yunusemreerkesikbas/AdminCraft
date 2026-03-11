CREATE TABLE platform_newsletter_subscriber_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL DEFAULT (UUID()),
    uid VARCHAR(50) NOT NULL DEFAULT (CONCAT('plt_ss_', LPAD(FLOOR(RAND() * 100000000), 8, '0'))),
    subscriber_id BIGINT NOT NULL,
    template_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_platform_newsletter_subscriber_template_type (subscriber_id, template_key),
    UNIQUE KEY uk_platform_newsletter_subscription_uuid (uuid),
    UNIQUE KEY uk_platform_newsletter_subscription_uid (uid),
    INDEX idx_platform_newsletter_subscriber_template_key (template_key),
    INDEX idx_platform_newsletter_subscriber_subscription_subscriber (subscriber_id),
    CONSTRAINT fk_platform_newsletter_subscriber_subscription_subscriber
        FOREIGN KEY (subscriber_id) REFERENCES platform_newsletter_subscribers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO platform_newsletter_subscriber_subscriptions (subscriber_id, template_key)
SELECT s.id, 'NEWSLETTER_DEFAULT'
FROM platform_newsletter_subscribers s
LEFT JOIN platform_newsletter_subscriber_subscriptions r
    ON r.subscriber_id = s.id AND r.template_key = 'NEWSLETTER_DEFAULT'
WHERE r.id IS NULL;

INSERT INTO platform_email_templates (template_key, language, subject, content, is_active)
SELECT 'VERSION_UPGRADE', 'TR', 'Versiyon Guncellemesi',
       'Merhaba {{name}},\n\nYeni versiyon yayinlandi: {{content}}\n\nIptal: {{unsubscribeUrl}}', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM platform_email_templates WHERE template_key = 'VERSION_UPGRADE' AND language = 'TR'
);

INSERT INTO platform_email_templates (template_key, language, subject, content, is_active)
SELECT 'VERSION_UPGRADE', 'EN', 'Version Upgrade',
       'Hello {{name}},\n\nA new version is available: {{content}}\n\nUnsubscribe: {{unsubscribeUrl}}', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM platform_email_templates WHERE template_key = 'VERSION_UPGRADE' AND language = 'EN'
);
