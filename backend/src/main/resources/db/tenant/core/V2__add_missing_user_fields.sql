-- V2: Add missing user fields to align with User entity
-- Sprint 20: Fix provisioning crash caused by missing columns

-- Add missing user fields that exist in User entity but were not in V1 migration
ALTER TABLE users
    ADD COLUMN avatar_url VARCHAR(255) NULL
        COMMENT 'User profile picture URL',
    ADD COLUMN first_name VARCHAR(50) NULL
        COMMENT 'User first name',
    ADD COLUMN last_name VARCHAR(50) NULL
        COMMENT 'User last name',
    ADD COLUMN department VARCHAR(100) NULL
        COMMENT 'User department',
    ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT 'Two-factor authentication enabled',
    ADD COLUMN password_changed_at TIMESTAMP NULL
        COMMENT 'Last password change timestamp',
    ADD COLUMN last_login_ip VARCHAR(50) NULL
        COMMENT 'IP address of last login',
    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0
        COMMENT 'Number of consecutive failed login attempts',
    ADD COLUMN locked_until TIMESTAMP NULL
        COMMENT 'Account locked until this timestamp',
    ADD COLUMN created_by BIGINT NULL
        COMMENT 'User ID who created this record',
    ADD COLUMN updated_by BIGINT NULL
        COMMENT 'User ID who last updated this record',
    ADD COLUMN notes VARCHAR(500) NULL
        COMMENT 'Additional notes about the user';

-- Add indexes for performance
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);
