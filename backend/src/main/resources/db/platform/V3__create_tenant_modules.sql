-- Create platform_management.tenant_modules table
-- Note: Flyway controls idempotency via versioning; do not use IF NOT EXISTS

CREATE TABLE platform_management.tenant_modules (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  module_code VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL,
  target_version VARCHAR(20) NULL,
  installed_at DATETIME NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_module (tenant_id, module_code),
  KEY idx_tenant_modules_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


