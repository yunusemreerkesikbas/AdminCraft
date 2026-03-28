-- #CRAFTIVE_IMPEX
-- Version-controlled ImpEx reference script.
-- Run via Admin UI /{lang}/impex when needed to seed platform mail marketing sample data.
-- Idempotent: safe to run multiple times.
-- Prerequisite: platform mail marketing tables must exist (Flyway V46..V49).

-- ============================================
-- 1. TEMPLATE TRANSLATIONS (TR + EN)
-- ============================================

INSERT INTO platform_email_templates (template_key, language, subject, content, is_active)
VALUES
('NEWSLETTER_DEFAULT', 'TR', 'Platform Duyurusu', 'Merhaba {{name}},\n\n{{content}}\n\nIptal: {{unsubscribeUrl}}', TRUE),
('NEWSLETTER_DEFAULT', 'EN', 'Platform Announcement', 'Hello {{name}},\n\n{{content}}\n\nUnsubscribe: {{unsubscribeUrl}}', TRUE),
('VERSION_UPGRADE', 'TR', 'Platform Versiyon Guncellemesi', 'Merhaba {{name}},\n\nYeni platform surumu: {{content}}\n\nIptal: {{unsubscribeUrl}}', TRUE),
('VERSION_UPGRADE', 'EN', 'Platform Version Upgrade', 'Hello {{name}},\n\nNew platform release: {{content}}\n\nUnsubscribe: {{unsubscribeUrl}}', TRUE)
ON DUPLICATE KEY UPDATE
subject = VALUES(subject),
content = VALUES(content),
is_active = VALUES(is_active),
updated_at = NOW();

-- ============================================
-- 2. SUBSCRIBERS
-- ============================================

INSERT INTO platform_newsletter_subscribers (
    email,
    status,
    source,
    confirm_token,
    unsubscribe_token,
    confirmed_at
)
VALUES
('platform-admin@example.com', 'ACTIVE', 'platform_landing', NULL, 'seed-unsub-platform-admin', NOW()),
('product-owner@example.com', 'ACTIVE', 'release_notes', NULL, 'seed-unsub-product-owner', NOW()),
('new-visitor@example.com', 'PENDING_CONFIRMATION', 'newsletter_widget', 'seed-confirm-new-visitor', 'seed-unsub-new-visitor', NULL)
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

INSERT INTO platform_newsletter_subscriber_subscriptions (subscriber_id, template_key, source, preferred_language, permission)
SELECT s.id, 'NEWSLETTER_DEFAULT', s.source, 'EN', TRUE
FROM platform_newsletter_subscribers s
WHERE s.email IN ('platform-admin@example.com', 'product-owner@example.com', 'new-visitor@example.com')
ON DUPLICATE KEY UPDATE
template_key = VALUES(template_key),
source = VALUES(source),
preferred_language = VALUES(preferred_language),
permission = VALUES(permission),
updated_at = NOW();

INSERT INTO platform_newsletter_subscriber_subscriptions (subscriber_id, template_key, source, preferred_language, permission)
SELECT s.id, 'VERSION_UPGRADE', s.source, 'EN', TRUE
FROM platform_newsletter_subscribers s
WHERE s.email IN ('platform-admin@example.com', 'product-owner@example.com')
ON DUPLICATE KEY UPDATE
template_key = VALUES(template_key),
source = VALUES(source),
preferred_language = VALUES(preferred_language),
permission = VALUES(permission),
updated_at = NOW();
