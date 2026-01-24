-- =====================================================
-- V32: Remove unused columns from product_field_definitions
-- =====================================================
-- Removes columns that are no longer used:
-- - is_required
-- - is_visible_in_list
-- - sort_order
-- - default_value
-- - validation_config
-- Also removes the index on sort_order
-- =====================================================

-- Drop index on sort_order
ALTER TABLE product_field_definitions DROP INDEX idx_pfd_sort;

-- Remove unused columns
ALTER TABLE product_field_definitions
    DROP COLUMN is_required,
    DROP COLUMN is_visible_in_list,
    DROP COLUMN sort_order,
    DROP COLUMN default_value,
    DROP COLUMN validation_config;
