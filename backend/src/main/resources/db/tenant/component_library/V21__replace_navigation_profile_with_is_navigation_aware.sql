-- V21: Replace navigation_profile enum column with simpler is_navigation_aware boolean.
-- NONE → false, all other profiles → true.

ALTER TABLE component_types
    ADD COLUMN is_navigation_aware BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE component_types
SET is_navigation_aware = (navigation_profile != 'NONE');

ALTER TABLE component_types DROP COLUMN navigation_profile;
