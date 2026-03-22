-- Seed initial platform admin user
-- Runs automatically on every startup/checksum change

INSERT IGNORE INTO platform_admin_users 
(email, password_hash, full_name, is_active, created_at, updated_at) 
VALUES 
('admin@craftive.io', 
 '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 
 'Craftive Master Admin', 
 1, 
 NOW(), 
 NOW());
