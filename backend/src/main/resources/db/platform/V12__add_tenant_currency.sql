-- V12: Add currency column to tenants table
-- Existing tenants will default to TRY

ALTER TABLE tenants
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'TRY';

-- Remove default constraint after migration (new tenants must specify currency)
ALTER TABLE tenants
    ALTER COLUMN currency DROP DEFAULT;
