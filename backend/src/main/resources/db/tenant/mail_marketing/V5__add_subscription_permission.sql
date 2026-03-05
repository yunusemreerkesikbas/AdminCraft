ALTER TABLE newsletter_subscriber_subscriptions
    ADD COLUMN permission BOOLEAN NOT NULL DEFAULT TRUE AFTER preferred_language;

CREATE INDEX idx_newsletter_subscriber_template_permission_lang
    ON newsletter_subscriber_subscriptions (template_key, permission, preferred_language);
