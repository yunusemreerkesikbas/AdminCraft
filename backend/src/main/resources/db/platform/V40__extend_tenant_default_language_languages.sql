ALTER TABLE tenants
    MODIFY COLUMN default_language ENUM('TR', 'EN', 'ZH', 'HI', 'ES', 'FR', 'AR', 'BN', 'RU', 'PT', 'UR') DEFAULT 'TR';
