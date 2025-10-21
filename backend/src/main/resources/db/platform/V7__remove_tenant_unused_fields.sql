-- Remove unused tenant fields
-- These fields were part of the tenant form but are no longer needed

ALTER TABLE tenants
    DROP COLUMN admin_name,
    DROP COLUMN admin_email,
    DROP COLUMN phone,
    DROP COLUMN ssl_enabled,
    DROP COLUMN timezone,
    DROP COLUMN currency;
