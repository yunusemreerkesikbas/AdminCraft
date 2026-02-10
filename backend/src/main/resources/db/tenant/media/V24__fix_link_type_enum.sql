-- =====================================================
-- V24: Fix link_type column type (NO-OP)
-- This migration is now redundant: component_media_links table
-- was moved to component_library module (V15) with correct VARCHAR type.
-- Kept as empty migration to preserve Flyway version history.
-- =====================================================
SELECT 1;
