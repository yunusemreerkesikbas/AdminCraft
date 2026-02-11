-- V43: Remove unnecessary index on singleton table
--
-- The platform_settings table is a singleton (single row with ID=1).
-- Index on recaptcha_enabled provides no performance benefit.
--
-- Author: Code Review Fix
-- Date: 2026-02-10
-- Related: PR #209 Code Review - Warning #11

-- MySQL doesn't support DROP INDEX IF EXISTS
-- Use ALTER TABLE syntax instead
ALTER TABLE platform_settings 
DROP INDEX idx_platform_settings_recaptcha_enabled;
