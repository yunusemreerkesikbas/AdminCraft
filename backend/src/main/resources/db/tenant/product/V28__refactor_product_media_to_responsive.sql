-- =====================================================
-- V28: Refactor Product Gallery to Responsive Media Query
-- Sprint 40: Product Module Refactoring
-- =====================================================

-- 1. Add temporary linking column to responsive_media_set
ALTER TABLE responsive_media_set ADD COLUMN temp_pm_id BIGINT;
CREATE INDEX idx_temp_pm ON responsive_media_set(temp_pm_id);

-- 2. Add new column to product_media
ALTER TABLE product_media ADD COLUMN responsive_media_set_id BIGINT;

-- 3. Migrate existing data: Create ResponsiveMediaSet for each ProductMedia row
INSERT INTO responsive_media_set (uuid, uid, code, desktop_media_id, mobile_media_id, temp_pm_id)
SELECT 
    UUID(), 
    CONCAT('rms-', UUID()), 
    CONCAT('rms-', UUID()), 
    media_id, 
    media_id, 
    id 
FROM product_media;

-- 4. Update product_media with new foreign keys
UPDATE product_media pm
JOIN responsive_media_set rms ON pm.id = rms.temp_pm_id
SET pm.responsive_media_set_id = rms.id;

-- 5. Add constraints and cleanup product_media
ALTER TABLE product_media DROP FOREIGN KEY fk_pm_media;
ALTER TABLE product_media DROP INDEX uk_product_media;
ALTER TABLE product_media DROP COLUMN media_id;

-- Make nullable after update to ensure data integrity, then adding constraint
ALTER TABLE product_media MODIFY responsive_media_set_id BIGINT NOT NULL;

ALTER TABLE product_media
    ADD CONSTRAINT fk_pm_responsive_set
    FOREIGN KEY (responsive_media_set_id)
    REFERENCES responsive_media_set(id)
    ON DELETE CASCADE;

ALTER TABLE product_media ADD CONSTRAINT uk_product_media UNIQUE (product_id, responsive_media_set_id);
CREATE INDEX idx_pm_responsive_set ON product_media(responsive_media_set_id);

-- 6. Remove temporary column from responsive_media_set
ALTER TABLE responsive_media_set DROP COLUMN temp_pm_id;
