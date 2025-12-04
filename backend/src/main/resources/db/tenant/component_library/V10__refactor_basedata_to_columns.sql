-- Refactor baseData JSON column to separate columns
-- Part 1: Add new columns
ALTER TABLE components
    ADD COLUMN display_order INT DEFAULT 0,
    ADD COLUMN is_visible BOOLEAN DEFAULT TRUE,
    ADD COLUMN style_classes VARCHAR(500);

-- Part 2: Migrate data from JSON to new columns
UPDATE components 
SET display_order = COALESCE(JSON_EXTRACT(base_data, '$.order'), 0),
    is_visible = CASE 
        WHEN JSON_EXTRACT(base_data, '$.isVisible') = true THEN 1
        WHEN JSON_EXTRACT(base_data, '$.isVisible') = false THEN 0
        ELSE 1
    END,
    style_classes = JSON_UNQUOTE(JSON_EXTRACT(base_data, '$.styleClasses'))
WHERE base_data IS NOT NULL;

-- Part 3: Drop JSON column
ALTER TABLE components DROP COLUMN base_data;

