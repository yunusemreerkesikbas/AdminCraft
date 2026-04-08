-- Add TENANT_USER_WELCOME transactional email template
-- Used when a tenant admin creates a new user account.
-- Tenant admins can customize the subject and content via the mail marketing admin panel.
INSERT INTO email_templates (template_key, language, subject, content, is_active)
VALUES
(
    'TENANT_USER_WELCOME',
    'TR',
    'Hesabınız Oluşturuldu',
    'Merhaba {{name}},\n\nHesabınız oluşturuldu. Hesabınızı aktifleştirmek için aşağıdaki bağlantıya tıklayın:\n\n{{verificationLink}}\n\nBağlantı {{expiryHours}} saat geçerlidir.',
    TRUE
),
(
    'TENANT_USER_WELCOME',
    'EN',
    'Your Account Has Been Created',
    'Hello {{name}},\n\nYour account has been created. Click the link below to activate your account:\n\n{{verificationLink}}\n\nThis link is valid for {{expiryHours}} hours.',
    TRUE
);
