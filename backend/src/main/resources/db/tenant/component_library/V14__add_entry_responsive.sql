-- =====================================================
-- V14: Add Responsive Media to Component Entries
-- Sprint 37: Entry-Level Media Integration
-- =====================================================

ALTER TABLE component_entries ADD COLUMN responsive_id BIGINT NULL AFTER style_classes;
ALTER TABLE component_entries ADD CONSTRAINT fk_entry_responsive FOREIGN KEY (responsive_id) REFERENCES responsive_media_set(id) ON DELETE SET NULL;
CREATE INDEX idx_entry_responsive ON component_entries(responsive_id);
