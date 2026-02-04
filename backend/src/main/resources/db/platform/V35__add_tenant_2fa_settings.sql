-- Add two-factor authentication policy to tenants
ALTER TABLE tenants
ADD COLUMN two_factor_policy ENUM('DISABLED', 'OPTIONAL', 'REQUIRED') DEFAULT 'DISABLED' AFTER status;

CREATE INDEX idx_tenants_2fa_policy ON tenants(two_factor_policy);
