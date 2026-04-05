-- #CRAFTIVE_IMPEX
-- Version-controlled ImpEx reference script.
-- Run via Admin UI /{lang}/impex when needed to seed tenant mail marketing sample data.
-- Idempotent: safe to run multiple times.
-- Prerequisite: mail_marketing module tables must exist (Flyway V1..V5).

-- ============================================
-- 1. TEMPLATE TRANSLATIONS (TR + EN)
-- ============================================

INSERT INTO email_templates (template_key, language, subject, content, is_active)
VALUES
('NEWSLETTER_DEFAULT', 'TR', 'Haftalik Bulten', 'Merhaba {{name}},\n\n{{content}}\n\nIptal: {{unsubscribeUrl}}', TRUE),
('NEWSLETTER_DEFAULT', 'EN', 'Weekly Newsletter', 'Hello {{name}},\n\n{{content}}\n\nUnsubscribe: {{unsubscribeUrl}}', TRUE),
('VERSION_UPGRADE', 'TR', 'Versiyon Guncellemesi', 'Merhaba {{name}},\n\nYeni surum yayinda: {{content}}\n\nIptal: {{unsubscribeUrl}}', TRUE),
('VERSION_UPGRADE', 'EN', 'Version Upgrade', 'Hello {{name}},\n\nNew release is live: {{content}}\n\nUnsubscribe: {{unsubscribeUrl}}', TRUE)
ON DUPLICATE KEY UPDATE
subject = VALUES(subject),
content = VALUES(content),
is_active = VALUES(is_active),
updated_at = NOW();

-- ============================================
-- 2. SUBSCRIBERS
-- ============================================

INSERT INTO newsletter_subscribers (
    email,
    status,
    source,
    confirm_token,
    unsubscribe_token,
    confirmed_at
)
VALUES
('alice@example.com', 'ACTIVE', 'landing_newsletter', NULL, 'seed-unsub-alice', NOW()),
('bob@example.com', 'ACTIVE', 'site_footer', NULL, 'seed-unsub-bob', NOW()),
('carol@example.com', 'PENDING_CONFIRMATION', 'waitlist_modal', 'seed-confirm-carol', 'seed-unsub-carol', NULL)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
source = VALUES(source),
confirm_token = VALUES(confirm_token),
unsubscribe_token = VALUES(unsubscribe_token),
confirmed_at = VALUES(confirmed_at),
updated_at = NOW();

-- ============================================
-- 3. SUBSCRIBER <-> TEMPLATE TYPE RELATIONS
-- ============================================

INSERT INTO newsletter_subscriber_subscriptions (subscriber_id, template_key, source, preferred_language, permission)
SELECT
    s.id,
    'NEWSLETTER_DEFAULT',
    s.source,
    'EN',
    TRUE
FROM newsletter_subscribers s
WHERE s.email IN ('alice@example.com', 'bob@example.com', 'carol@example.com')
ON DUPLICATE KEY UPDATE
template_key = VALUES(template_key),
source = VALUES(source),
preferred_language = VALUES(preferred_language),
permission = VALUES(permission),
updated_at = NOW();

INSERT INTO newsletter_subscriber_subscriptions (subscriber_id, template_key, source, preferred_language, permission)
SELECT
    s.id,
    'VERSION_UPGRADE',
    s.source,
    'EN',
    TRUE
FROM newsletter_subscribers s
WHERE s.email IN ('alice@example.com', 'bob@example.com')
ON DUPLICATE KEY UPDATE
template_key = VALUES(template_key),
source = VALUES(source),
preferred_language = VALUES(preferred_language),
permission = VALUES(permission),
updated_at = NOW();
