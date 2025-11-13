CREATE TABLE component_entries (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  uuid CHAR(36) NOT NULL UNIQUE,
  uid VARCHAR(50) NOT NULL UNIQUE,
  component_id BIGINT NOT NULL,
  sort_order INT DEFAULT 0,
  is_visible BOOLEAN DEFAULT TRUE,
  style_classes VARCHAR(500),
  status ENUM('DRAFT', 'ACTIVE', 'INACTIVE') DEFAULT 'DRAFT',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE,
  INDEX idx_component_order (component_id, sort_order),
  INDEX idx_entry_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE component_entry_i18n (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  uuid CHAR(36) NOT NULL UNIQUE,
  uid VARCHAR(50) NOT NULL UNIQUE,
  entry_id BIGINT NOT NULL,
  language ENUM('TR', 'EN') NOT NULL,
  title VARCHAR(255),
  description TEXT,
  image_url VARCHAR(500),
  button_text VARCHAR(100),
  button_url VARCHAR(500),
  status ENUM('DRAFT', 'ACTIVE', 'INACTIVE') DEFAULT 'DRAFT',
  published_at TIMESTAMP NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  FOREIGN KEY (entry_id) REFERENCES component_entries(id) ON DELETE CASCADE,
  UNIQUE KEY uk_entry_language (entry_id, language),
  INDEX idx_entry_i18n_entry (entry_id),
  INDEX idx_entry_i18n_language (language),
  INDEX idx_entry_i18n_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE entry_field_definitions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  component_type_id BIGINT NOT NULL,
  field_key VARCHAR(50) NOT NULL,
  field_type ENUM('TEXT', 'TEXTAREA', 'NUMBER', 'BOOLEAN') NOT NULL,
  label_tr VARCHAR(100) NOT NULL,
  label_en VARCHAR(100) NOT NULL,
  is_required BOOLEAN DEFAULT FALSE,
  max_length INT NULL,
  min_value DECIMAL(10,2) NULL,
  max_value DECIMAL(10,2) NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (component_type_id) REFERENCES component_types(id) ON DELETE CASCADE,
  UNIQUE KEY uk_type_field (component_type_id, field_key),
  INDEX idx_field_type (component_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

