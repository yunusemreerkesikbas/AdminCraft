ALTER TABLE platform_config_properties
    ADD COLUMN uuid VARCHAR(36) NULL AFTER id,
    ADD COLUMN uid  VARCHAR(50) NULL AFTER uuid;

UPDATE platform_config_properties SET
    uuid = UUID(),
    uid  = CONCAT('cfg-', SUBSTRING(MD5(RAND()), 1, 8))
WHERE uuid IS NULL;

ALTER TABLE platform_config_properties
    MODIFY COLUMN uuid VARCHAR(36) NOT NULL,
    MODIFY COLUMN uid  VARCHAR(50) NOT NULL,
    ADD UNIQUE KEY uq_platform_config_properties_uuid (uuid),
    ADD UNIQUE KEY uq_platform_config_properties_uid  (uid);
