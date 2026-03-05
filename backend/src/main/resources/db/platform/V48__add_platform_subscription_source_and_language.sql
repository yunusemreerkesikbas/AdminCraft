ALTER TABLE platform_newsletter_subscriber_subscriptions
    ADD COLUMN source VARCHAR(120) NULL AFTER template_key,
    ADD COLUMN preferred_language VARCHAR(10) NOT NULL DEFAULT 'EN' AFTER source;

UPDATE platform_newsletter_subscriber_subscriptions rs
JOIN platform_newsletter_subscribers s ON s.id = rs.subscriber_id
SET rs.source = s.source
WHERE rs.source IS NULL;

CREATE INDEX idx_platform_newsletter_subscriber_template_lang
    ON platform_newsletter_subscriber_subscriptions (template_key, preferred_language);
