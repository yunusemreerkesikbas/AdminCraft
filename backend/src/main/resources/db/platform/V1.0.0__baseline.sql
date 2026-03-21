-- AdminCraft Platform Management Baseline
-- Consolidated from V1 to V52
-- Created: 2026-03-20

-- 1. Tenants table: Central registry of all tenants
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subdomain VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique subdomain identifier',
    company_name VARCHAR(255) NOT NULL,
    custom_domain VARCHAR(255) NULL COMMENT 'Optional custom domain',
    db_host VARCHAR(100) DEFAULT 'localhost' COMMENT 'Database host',
    db_port INT DEFAULT 3307 COMMENT 'Database port',
    database_name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Tenant database name (ac_tenant_{id})',
    status ENUM('PENDING', 'PROVISIONING', 'ACTIVE', 'SUSPENDED', 'DELETED', 'MAINTENANCE') DEFAULT 'PENDING',
    two_factor_policy ENUM('DISABLED', 'REQUIRED') DEFAULT 'DISABLED',
    has_admin_user BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether tenant admin user has been created',
    default_language ENUM('TR', 'EN', 'ZH', 'HI', 'ES', 'FR', 'AR', 'BN', 'RU', 'PT', 'UR') DEFAULT 'TR',
    supported_languages JSON DEFAULT ('["TR"]') COMMENT 'Array of supported language codes',
    admin_email VARCHAR(255) NULL,
    admin_name VARCHAR(100) NULL,
    phone VARCHAR(20) NULL COMMENT 'Contact phone number',
    ssl_enabled BOOLEAN DEFAULT FALSE COMMENT 'SSL certificate enabled for custom domain',
    timezone VARCHAR(50) DEFAULT 'Europe/Istanbul' COMMENT 'Tenant timezone',
    currency VARCHAR(3) DEFAULT 'TRY' COMMENT 'Default currency code',
    storage_used_mb BIGINT DEFAULT 0 COMMENT 'Storage usage in megabytes',
    activated_at TIMESTAMP NULL COMMENT 'When tenant was activated',
    last_backup_at TIMESTAMP NULL COMMENT 'Last backup timestamp',
    notes VARCHAR(1000) NULL COMMENT 'Administrative notes',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_subdomain (subdomain),
    INDEX idx_status (status),
    INDEX idx_database_name (database_name),
    INDEX idx_tenants_has_admin_user (has_admin_user),
    INDEX idx_tenants_two_factor_policy (two_factor_policy)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Platform Admin Users table
CREATE TABLE platform_admin_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    last_login_at DATETIME NULL,
    last_login_ip VARCHAR(45) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Modules catalog: Available modules for tenants
CREATE TABLE modules_catalog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique module identifier (e.g., core, pagebuilder)',
    name VARCHAR(100) NOT NULL COMMENT 'Display name',
    type ENUM('core', 'b2b', 'b2c') NOT NULL COMMENT 'Module type/category',
    version VARCHAR(20) DEFAULT '1.0.0',
    deps JSON NULL COMMENT 'Array of module codes this module depends on',
    enabled_by_default BOOLEAN DEFAULT FALSE,
    description TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_type (type),
    INDEX idx_enabled_by_default (enabled_by_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tenant modules: Modules enabled for each tenant
CREATE TABLE tenant_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    module_code VARCHAR(50) NOT NULL,
    status ENUM('enabled', 'disabled', 'pending') DEFAULT 'pending',
    target_version VARCHAR(20) NULL,
    installed_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_tenant_module (tenant_id, module_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_module_code (module_code),
    INDEX idx_status (status),

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (module_code) REFERENCES modules_catalog(code) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Provisioning jobs: Track tenant database provisioning
CREATE TABLE provisioning_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    type ENUM('create-db', 'add-modules', 'migrate', 'full-provision', 'sync-migrations') DEFAULT 'full-provision',
    payload JSON NULL COMMENT 'Job configuration (modules to install, etc.)',
    status ENUM('pending', 'running', 'succeeded', 'failed') DEFAULT 'pending',
    progress INT DEFAULT 0 COMMENT 'Progress percentage (0-100)',
    error TEXT NULL COMMENT 'Error message if failed',
    correlation_id VARCHAR(36) NULL COMMENT 'Correlation ID for log tracking',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_correlation_id (correlation_id),

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Platform Settings table (Singleton)
CREATE TABLE platform_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform_name VARCHAR(100) NOT NULL DEFAULT 'AdminCraft',
    default_language VARCHAR(2) NOT NULL DEFAULT 'TR',
    default_currency VARCHAR(3) NOT NULL DEFAULT 'TRY',
    email_from_address VARCHAR(255) NOT NULL DEFAULT 'noreply@admincraft.com',
    email_from_name VARCHAR(100) NOT NULL DEFAULT 'AdminCraft',
    two_factor_policy ENUM('DISABLED', 'REQUIRED') NOT NULL DEFAULT 'DISABLED',
    recaptcha_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    recaptcha_site_key VARCHAR(255) NULL,
    recaptcha_secret_key_encrypted TEXT NULL,
    recaptcha_threshold DECIMAL(3,2) NOT NULL DEFAULT 0.50,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO platform_settings (id) VALUES (1);

-- 7. Platform Verification Tokens
CREATE TABLE platform_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform_admin_user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    token_type ENUM('LOGIN_OTP') NOT NULL,
    status ENUM('ACTIVE', 'USED', 'EXPIRED', 'REVOKED') NOT NULL DEFAULT 'ACTIVE',
    target_value VARCHAR(255) NULL,
    expires_at DATETIME NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 5,
    used_at DATETIME NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_platform_verification_token_admin_user
        FOREIGN KEY (platform_admin_user_id) REFERENCES platform_admin_users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_platform_verification_token_hash (token_hash),
    INDEX idx_platform_token_admin_user (platform_admin_user_id),
    INDEX idx_platform_token_type_status (token_type, status),
    INDEX idx_platform_token_expires_at (expires_at),
    INDEX idx_platform_token_hash_status (token_hash, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Config Change Audit
CREATE TABLE config_change_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT NOT NULL,
    actor_email VARCHAR(255) NOT NULL,
    actor_role VARCHAR(50) NOT NULL,
    target_tenant_id BIGINT NOT NULL,
    scope VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    before_json TEXT NULL,
    after_json TEXT NULL,
    reason VARCHAR(500) NOT NULL,
    correlation_id VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_config_audit_tenant_created (target_tenant_id, created_at),
    INDEX idx_config_audit_actor_created (actor_user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Platform Mail Marketing
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

CREATE TABLE platform_newsletter_subscriber_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL DEFAULT (UUID()),
    uid VARCHAR(50) NOT NULL DEFAULT (CONCAT('plt_ss_', LPAD(FLOOR(RAND() * 100000000), 8, '0'))),
    subscriber_id BIGINT NOT NULL,
    template_key VARCHAR(100) NOT NULL,
    source VARCHAR(120) NULL,
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'EN',
    permission BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    UNIQUE KEY uk_platform_newsletter_subscriber_template_type (subscriber_id, template_key),
    UNIQUE KEY uk_platform_newsletter_subscription_uuid (uuid),
    UNIQUE KEY uk_platform_newsletter_subscription_uid (uid),
    INDEX idx_platform_newsletter_subscriber_template_key (template_key),
    INDEX idx_platform_newsletter_subscriber_subscription_subscriber (subscriber_id),
    INDEX idx_platform_newsletter_subscriber_template_lang (template_key, preferred_language),
    INDEX idx_platform_newsletter_subscriber_template_permission_lang (template_key, permission, preferred_language),
    CONSTRAINT fk_platform_newsletter_subscriber_subscription_subscriber
        FOREIGN KEY (subscriber_id) REFERENCES platform_newsletter_subscribers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Default platform email templates
INSERT INTO platform_email_templates (template_key, language, subject, content, is_active)
VALUES
('NEWSLETTER_DEFAULT', 'TR', 'Bülten Bilgilendirmesi', 'Merhaba {{name}},

{{content}}

İptal: {{unsubscribeUrl}}', TRUE),
('NEWSLETTER_DEFAULT', 'EN', 'Newsletter Update', 'Hello {{name}},

{{content}}

Unsubscribe: {{unsubscribeUrl}}', TRUE),
('VERSION_UPGRADE', 'TR', 'Versiyon Güncellemesi', 'Merhaba {{name}},

Yeni versiyon yayınlandı: {{content}}

İptal: {{unsubscribeUrl}}', TRUE),
('VERSION_UPGRADE', 'EN', 'Version Upgrade', 'Hello {{name}},

A new version is available: {{content}}

Unsubscribe: {{unsubscribeUrl}}', TRUE);

-- 10. Platform Config Properties
CREATE TABLE platform_config_properties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    uid  VARCHAR(50) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT NULL,
    is_secret BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    UNIQUE KEY uq_platform_config_properties_config_key (config_key),
    UNIQUE KEY uq_platform_config_properties_uuid (uuid),
    UNIQUE KEY uq_platform_config_properties_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
