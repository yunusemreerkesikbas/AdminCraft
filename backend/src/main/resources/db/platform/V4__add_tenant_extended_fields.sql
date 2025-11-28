-- Add extended fields to tenants table for Sprint 16
-- These fields support additional tenant management features

ALTER TABLE tenants
    ADD COLUMN phone VARCHAR(20) NULL COMMENT 'Contact phone number',
    ADD COLUMN ssl_enabled BOOLEAN DEFAULT FALSE COMMENT 'SSL certificate enabled for custom domain',
    ADD COLUMN timezone VARCHAR(50) DEFAULT 'Europe/Istanbul' COMMENT 'Tenant timezone',
    ADD COLUMN currency VARCHAR(3) DEFAULT 'TRY' COMMENT 'Default currency code',
    ADD COLUMN storage_used_mb BIGINT DEFAULT 0 COMMENT 'Storage usage in megabytes',
    ADD COLUMN activated_at TIMESTAMP NULL COMMENT 'When tenant was activated',
    ADD COLUMN last_backup_at TIMESTAMP NULL COMMENT 'Last backup timestamp',
    ADD COLUMN notes VARCHAR(1000) NULL COMMENT 'Administrative notes';

-- Rename db_name to database_name for consistency with entity
ALTER TABLE tenants
    CHANGE COLUMN db_name database_name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Tenant database name (ac_tenant_{id})';

-- Update status enum to include MAINTENANCE
ALTER TABLE tenants
    MODIFY COLUMN status ENUM('PENDING', 'PROVISIONING', 'ACTIVE', 'SUSPENDED', 'DELETED', 'MAINTENANCE') DEFAULT 'PENDING';

-- Create tenant_supported_languages table for @ElementCollection
CREATE TABLE IF NOT EXISTS tenant_supported_languages (
    tenant_id BIGINT NOT NULL,
    language VARCHAR(255) NOT NULL,
    PRIMARY KEY (tenant_id, language),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
