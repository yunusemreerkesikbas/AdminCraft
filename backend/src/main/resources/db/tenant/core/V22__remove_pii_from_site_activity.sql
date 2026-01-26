-- Remove PII fields from site_activity
-- Data retention: Historical user email/name in activity log will be lost.
-- This is intentional for GDPR/Privacy compliance.
-- User details should be resolved from user table using user_id.

ALTER TABLE site_activity DROP COLUMN user_email;
ALTER TABLE site_activity DROP COLUMN user_full_name;

-- Add audit fields to sites table
ALTER TABLE sites ADD COLUMN created_by BIGINT;
ALTER TABLE sites ADD COLUMN updated_by BIGINT;

CREATE INDEX idx_sites_created_by ON sites(created_by);
CREATE INDEX idx_sites_updated_by ON sites(updated_by);
