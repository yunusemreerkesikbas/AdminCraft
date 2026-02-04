-- Remove OPTIONAL policy from two_factor_policy enum
UPDATE tenants
SET two_factor_policy = 'DISABLED'
WHERE two_factor_policy = 'OPTIONAL';

ALTER TABLE tenants
MODIFY COLUMN two_factor_policy ENUM('DISABLED', 'REQUIRED') DEFAULT 'DISABLED';
