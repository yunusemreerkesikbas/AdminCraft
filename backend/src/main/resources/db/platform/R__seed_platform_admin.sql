-- Seed initial platform admin user
-- Runs automatically on every startup/checksum change
-- Password: Craftive.2026!

INSERT INTO platform_admin_users
(email, password_hash, full_name, is_active, failed_login_attempts, locked_until, created_at, updated_at)
VALUES
('admin@craftive.io',
 '$2b$10$g9r4wUJ13JLcmAY/irgS8eYqYXD/I8.CMoux29p1yMgvM.3sx0ojW',
 'Craftive Master Admin',
 1, 0, NULL,
 NOW(),
 NOW())
ON DUPLICATE KEY UPDATE
  password_hash         = VALUES(password_hash),
  is_active             = 1,
  failed_login_attempts = 0,
  locked_until          = NULL,
  updated_at            = NOW();
