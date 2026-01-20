-- =====================================================
-- V13: Add Responsive Media to Components
-- Sprint 37: Component-Media Integration
-- =====================================================

-- Add responsive_id to components table
-- This links a component to its Desktop/Mobile image pair
ALTER TABLE components
    ADD COLUMN responsive_id BIGINT NULL AFTER style_classes,
    ADD CONSTRAINT fk_component_responsive
        FOREIGN KEY (responsive_id)
        REFERENCES responsive_media_set(id) ON DELETE SET NULL;

CREATE INDEX idx_component_responsive ON components(responsive_id);
