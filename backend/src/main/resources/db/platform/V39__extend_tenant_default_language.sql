-- Extend tenant default_language enum to support all site languages
ALTER TABLE tenants
    MODIFY COLUMN default_language ENUM('TR', 'EN', 'ES', 'RU', 'AR') DEFAULT 'TR';
