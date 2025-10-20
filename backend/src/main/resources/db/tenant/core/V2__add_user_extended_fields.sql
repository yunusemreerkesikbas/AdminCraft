-- Add extended user fields for Sprint 17 enhancements
-- These fields support enhanced user profiles, security features, and audit trail

-- Add user profile fields
ALTER TABLE users
    ADD COLUMN first_name VARCHAR(50) NULL AFTER full_name,
    ADD COLUMN last_name VARCHAR(50) NULL AFTER first_name,
    ADD COLUMN avatar_url VARCHAR(255) NULL AFTER phone,
    ADD COLUMN department VARCHAR(100) NULL AFTER job_title;

-- Add tenant association (critical for multi-tenant filtering)
ALTER TABLE users
    ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1 AFTER preferred_language,
    ADD INDEX idx_user_tenant (tenant_id);

-- Add security and audit fields
ALTER TABLE users
    ADD COLUMN two_factor_enabled BOOLEAN DEFAULT FALSE AFTER email_verified_at,
    ADD COLUMN password_changed_at TIMESTAMP NULL AFTER two_factor_enabled,
    ADD COLUMN last_login_ip VARCHAR(45) NULL AFTER last_login_at COMMENT 'IPv6-compatible',
    ADD COLUMN failed_login_attempts INT DEFAULT 0 AFTER last_login_ip,
    ADD COLUMN locked_until TIMESTAMP NULL AFTER failed_login_attempts;

-- Add audit trail fields
ALTER TABLE users
    ADD COLUMN created_by BIGINT NULL AFTER created_at,
    ADD COLUMN updated_by BIGINT NULL AFTER updated_at,
    ADD COLUMN notes VARCHAR(500) NULL AFTER updated_by;

-- Update indexes for new columns
ALTER TABLE users
    ADD INDEX idx_user_created_at (created_at);

-- Note: tenant_id default is 1 for backward compatibility
-- Existing records will inherit tenant_id from their database context
