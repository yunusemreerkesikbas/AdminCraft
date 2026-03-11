CREATE TABLE newsletter_subscriber_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscriber_id BIGINT NOT NULL,
    template_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_newsletter_subscriber_subscription_subscriber
        FOREIGN KEY (subscriber_id) REFERENCES newsletter_subscribers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_newsletter_subscriber_template_type (subscriber_id, template_key),
    INDEX idx_newsletter_subscriber_template_key (template_key),
    INDEX idx_newsletter_subscriber_subscription_subscriber (subscriber_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO newsletter_subscriber_subscriptions (subscriber_id, template_key)
SELECT s.id, 'NEWSLETTER_DEFAULT'
FROM newsletter_subscribers s
LEFT JOIN newsletter_subscriber_subscriptions r
    ON r.subscriber_id = s.id AND r.template_key = 'NEWSLETTER_DEFAULT'
WHERE r.id IS NULL;

INSERT INTO email_templates (template_key, language, subject, content, is_active)
SELECT 'VERSION_UPGRADE', 'TR', 'Versiyon Guncellemesi',
       'Merhaba {{name}},\n\nYeni versiyon yayinlandi: {{content}}\n\nIptal: {{unsubscribeUrl}}', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM email_templates WHERE template_key = 'VERSION_UPGRADE' AND language = 'TR'
);

INSERT INTO email_templates (template_key, language, subject, content, is_active)
SELECT 'VERSION_UPGRADE', 'EN', 'Version Upgrade',
       'Hello {{name}},\n\nA new version is available: {{content}}\n\nUnsubscribe: {{unsubscribeUrl}}', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM email_templates WHERE template_key = 'VERSION_UPGRADE' AND language = 'EN'
);

