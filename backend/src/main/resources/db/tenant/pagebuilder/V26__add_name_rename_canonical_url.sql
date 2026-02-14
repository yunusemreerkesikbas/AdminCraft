-- V26: Page Builder i18n refactoring - Sprint 38
-- All changes were applied manually during development:
-- 1. Added 'name' column to page_i18n
-- 2. Renamed 'url_path' to 'canonical_url' in page_i18n
-- 3. Dropped subtitle, meta_title, meta_description, description_html from page_i18n
-- 4. Dropped featured_image, sort_order from pages
-- 5. Updated indexes for canonical_url
-- This migration is now a no-op placeholder to sync Flyway history.
-- Forward-only repair: see V34__repair_page_i18n_name_canonical_url.sql
SELECT 1;
