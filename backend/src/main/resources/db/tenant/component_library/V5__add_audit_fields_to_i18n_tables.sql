ALTER TABLE page_i18n 
  ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER updated_at,
  ADD COLUMN created_by BIGINT AFTER created_at,
  ADD COLUMN updated_by BIGINT AFTER created_by;

ALTER TABLE page_category_i18n 
  ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER updated_at,
  ADD COLUMN created_by BIGINT AFTER created_at,
  ADD COLUMN updated_by BIGINT AFTER created_by;

ALTER TABLE component_i18n 
  ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER updated_at,
  ADD COLUMN created_by BIGINT AFTER created_at,
  ADD COLUMN updated_by BIGINT AFTER created_by;

