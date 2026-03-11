ALTER TABLE newsletter_subscriber_subscriptions
    ADD COLUMN source VARCHAR(120) NULL AFTER template_key,
    ADD COLUMN preferred_language VARCHAR(10) NOT NULL DEFAULT 'EN' AFTER source;

UPDATE newsletter_subscriber_subscriptions rs
JOIN newsletter_subscribers s ON s.id = rs.subscriber_id
SET rs.source = s.source
WHERE rs.source IS NULL;

-- preferred_language defaults to 'EN' via column default; language is injected at provisioning time by the application layer

CREATE INDEX idx_newsletter_subscriber_template_lang
    ON newsletter_subscriber_subscriptions (template_key, preferred_language);
