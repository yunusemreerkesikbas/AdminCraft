-- Backfill UUID/UID and enforce NOT NULL/UNIQUE constraints

-- Pages
UPDATE pages SET uuid = UUID() WHERE uuid IS NULL OR uuid = '';
UPDATE pages SET uid = CONCAT('cmsitem_', LPAD(FLOOR(RAND() * 100000000), 8, '0')) WHERE uid IS NULL OR TRIM(uid) = '';

ALTER TABLE pages MODIFY uuid VARCHAR(36) NOT NULL;
ALTER TABLE pages MODIFY uid VARCHAR(50) NOT NULL;
ALTER TABLE pages ADD UNIQUE KEY uk_pages_uuid (uuid);
ALTER TABLE pages ADD UNIQUE KEY uk_pages_uid_tenant (tenant_id, uid);

-- Page i18n
UPDATE page_i18n SET uuid = UUID() WHERE uuid IS NULL OR uuid = '';
UPDATE page_i18n SET uid = CONCAT('cmsitem_', LPAD(FLOOR(RAND() * 100000000), 8, '0')) WHERE uid IS NULL OR TRIM(uid) = '';

ALTER TABLE page_i18n MODIFY uuid VARCHAR(36) NOT NULL;
ALTER TABLE page_i18n MODIFY uid VARCHAR(50) NOT NULL;
ALTER TABLE page_i18n ADD UNIQUE KEY uk_page_i18n_uuid (uuid);
ALTER TABLE page_i18n ADD UNIQUE KEY uk_page_i18n_uid_tenant (tenant_id, uid);
