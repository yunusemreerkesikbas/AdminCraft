CREATE TABLE platform_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform_name VARCHAR(100) NOT NULL DEFAULT 'AdminCraft',
    default_language VARCHAR(2) NOT NULL DEFAULT 'TR',
    default_currency VARCHAR(3) NOT NULL DEFAULT 'TRY',
    email_from_address VARCHAR(255) NOT NULL DEFAULT 'noreply@admincraft.com',
    email_from_name VARCHAR(100) NOT NULL DEFAULT 'AdminCraft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO platform_settings (id) VALUES (1);
