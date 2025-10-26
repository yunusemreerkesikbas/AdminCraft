-- Migrate tenant supported languages from normalized table to JSON field
-- This migration removes the redundant tenant_supported_languages table
-- and consolidates all language data into the tenants.supported_languages JSON column

-- Step 1: Migrate existing data from tenant_supported_languages to tenants.supported_languages JSON
-- Build JSON array from normalized table for each tenant
UPDATE tenants t
SET t.supported_languages = (
    SELECT CONCAT(
        '[',
        GROUP_CONCAT(
            CONCAT('"', tsl.language, '"')
            ORDER BY tsl.language
            SEPARATOR ','
        ),
        ']'
    )
    FROM tenant_supported_languages tsl
    WHERE tsl.tenant_id = t.id
)
WHERE EXISTS (
    SELECT 1 FROM tenant_supported_languages tsl WHERE tsl.tenant_id = t.id
);

-- Step 2: Set default value for tenants without any supported languages
-- Ensures no tenant has NULL or empty supported_languages
UPDATE tenants
SET supported_languages = '["TR"]'
WHERE supported_languages IS NULL
   OR supported_languages = ''
   OR supported_languages = '[]';

-- Step 3: Drop the redundant tenant_supported_languages table
-- This removes the @ElementCollection junction table
DROP TABLE IF EXISTS tenant_supported_languages;
