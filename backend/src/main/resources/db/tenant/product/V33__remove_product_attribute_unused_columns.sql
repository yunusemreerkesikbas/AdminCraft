-- =====================================================
-- V33: Remove unused columns from product_attribute_definitions
-- =====================================================
-- Removes columns that are no longer used:
-- - is_required
-- - is_searchable
-- - sort_order
-- - validation_config
-- Also removes the index on sort_order
-- =====================================================

-- Drop index on sort_order
ALTER TABLE product_attribute_definitions DROP INDEX idx_product_attr_def_sort;

-- Remove unused columns
ALTER TABLE product_attribute_definitions
    DROP COLUMN is_required,
    DROP COLUMN is_searchable,
    DROP COLUMN sort_order,
    DROP COLUMN validation_config;
