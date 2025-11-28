-- Platform Management Schema Baseline
-- Control-plane database for multi-tenant SaaS platform

-- Tenants table: Central registry of all tenants
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subdomain VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique subdomain identifier',
    company_name VARCHAR(255) NOT NULL,
    custom_domain VARCHAR(255) NULL COMMENT 'Optional custom domain',
    db_host VARCHAR(100) DEFAULT 'localhost' COMMENT 'Database host',
    db_port INT DEFAULT 3307 COMMENT 'Database port',
    database_name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Tenant database name (ac_tenant_{id})',
    status ENUM('PENDING', 'PROVISIONING', 'ACTIVE', 'SUSPENDED', 'DELETED', 'MAINTENANCE') DEFAULT 'PENDING',
    default_language ENUM('TR', 'EN') DEFAULT 'TR',
    supported_languages JSON DEFAULT ('["TR"]') COMMENT 'Array of supported language codes',
    admin_email VARCHAR(255) NOT NULL,
    admin_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL COMMENT 'Contact phone number',
    ssl_enabled BOOLEAN DEFAULT FALSE COMMENT 'SSL certificate enabled for custom domain',
    timezone VARCHAR(50) DEFAULT 'Europe/Istanbul' COMMENT 'Tenant timezone',
    currency VARCHAR(3) DEFAULT 'TRY' COMMENT 'Default currency code',
    storage_used_mb BIGINT DEFAULT 0 COMMENT 'Storage usage in megabytes',
    activated_at TIMESTAMP NULL COMMENT 'When tenant was activated',
    last_backup_at TIMESTAMP NULL COMMENT 'Last backup timestamp',
    notes VARCHAR(1000) NULL COMMENT 'Administrative notes',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_subdomain (subdomain),
    INDEX idx_status (status),
    INDEX idx_database_name (database_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Modules catalog: Available modules for tenants
CREATE TABLE modules_catalog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique module identifier (e.g., core, pagebuilder)',
    name VARCHAR(100) NOT NULL COMMENT 'Display name',
    type ENUM('core', 'b2b', 'b2c') NOT NULL COMMENT 'Module type/category',
    version VARCHAR(20) DEFAULT '1.0.0',
    deps JSON NULL COMMENT 'Array of module codes this module depends on',
    enabled_by_default BOOLEAN DEFAULT FALSE,
    description TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_type (type),
    INDEX idx_enabled_by_default (enabled_by_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tenant modules: Modules enabled for each tenant
CREATE TABLE tenant_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    module_code VARCHAR(50) NOT NULL,
    status ENUM('enabled', 'disabled', 'pending') DEFAULT 'pending',
    target_version VARCHAR(20) NULL,
    installed_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_tenant_module (tenant_id, module_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_module_code (module_code),
    INDEX idx_status (status),
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (module_code) REFERENCES modules_catalog(code) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Provisioning jobs: Track tenant database provisioning
CREATE TABLE provisioning_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    type ENUM('create-db', 'add-modules', 'migrate', 'full-provision') DEFAULT 'full-provision',
    payload JSON NULL COMMENT 'Job configuration (modules to install, etc.)',
    status ENUM('pending', 'running', 'succeeded', 'failed') DEFAULT 'pending',
    progress INT DEFAULT 0 COMMENT 'Progress percentage (0-100)',
    error TEXT NULL COMMENT 'Error message if failed',
    correlation_id VARCHAR(36) NULL COMMENT 'Correlation ID for log tracking',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_correlation_id (correlation_id),
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


