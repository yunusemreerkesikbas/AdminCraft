-- Add login tracking columns to platform_admin_users table
ALTER TABLE platform_admin_users
ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0 AFTER is_active,
ADD COLUMN locked_until DATETIME NULL AFTER failed_login_attempts,
ADD COLUMN last_login_at DATETIME NULL AFTER locked_until,
ADD COLUMN last_login_ip VARCHAR(45) NULL AFTER last_login_at;
