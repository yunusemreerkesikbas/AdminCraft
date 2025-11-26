-- V10: Add has_admin_user flag for tracking admin user existence
-- Sprint 20: Tenant Admin User Generator

-- Add has_admin_user flag to track whether tenant admin user has been generated
ALTER TABLE tenants
    ADD COLUMN has_admin_user BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT 'Whether tenant admin user has been created';

-- Make admin fields nullable (not required during tenant creation)
ALTER TABLE tenants
    MODIFY COLUMN admin_email VARCHAR(255) NULL,
    MODIFY COLUMN admin_name VARCHAR(100) NULL;

-- Add index for performance optimization when filtering by hasAdminUser
CREATE INDEX idx_tenants_has_admin_user ON tenants(has_admin_user);
