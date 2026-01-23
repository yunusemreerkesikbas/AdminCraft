-- =====================================================
-- V31: BaseEntity compliance for product fields
-- =====================================================
-- Adds missing columns required by BaseEntity:
-- - created_by/updated_by for product_field_definitions
-- - uuid/uid/created_by/updated_by for product_field_values
-- =====================================================

-- Add audit columns to product_field_definitions
ALTER TABLE product_field_definitions
    ADD COLUMN created_by BIGINT NULL,
    ADD COLUMN updated_by BIGINT NULL;

-- Add BaseEntity columns to product_field_values
ALTER TABLE product_field_values
    ADD COLUMN uuid VARCHAR(36) NULL,
    ADD COLUMN uid VARCHAR(50) NULL,
    ADD COLUMN created_by BIGINT NULL,
    ADD COLUMN updated_by BIGINT NULL;

-- Generate uuid/uid for existing records if any
UPDATE product_field_values 
SET uuid = UUID() 
WHERE uuid IS NULL;

UPDATE product_field_values 
SET uid = CONCAT('pfv_', id) 
WHERE uid IS NULL;

-- Now add constraints
ALTER TABLE product_field_values
    MODIFY COLUMN uuid VARCHAR(36) NOT NULL,
    MODIFY COLUMN uid VARCHAR(50) NOT NULL,
    ADD CONSTRAINT uk_pfv_uuid UNIQUE (uuid),
    ADD CONSTRAINT uk_pfv_uid UNIQUE (uid);
