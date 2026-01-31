-- V25: Remove preferred_language column
-- Admin panel language is system-wide, not user-specific

ALTER TABLE users DROP COLUMN preferred_language;
