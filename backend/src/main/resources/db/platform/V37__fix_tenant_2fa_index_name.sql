-- Fix index naming convention for two_factor_policy
-- Rename idx_tenants_2fa_policy to idx_tenants_two_factor_policy to match column name

DROP INDEX idx_tenants_2fa_policy ON tenants;
CREATE INDEX idx_tenants_two_factor_policy ON tenants(two_factor_policy);
