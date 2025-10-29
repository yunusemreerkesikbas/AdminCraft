-- Add admin user fields to tenants table for provisioning
-- These fields are used during tenant provisioning to create the initial admin user

ALTER TABLE platform_management.tenants
    ADD COLUMN admin_email VARCHAR(255) NULL COMMENT 'Admin user email for initial provisioning',
    ADD COLUMN admin_name VARCHAR(100) NULL COMMENT 'Admin user full name for initial provisioning';
