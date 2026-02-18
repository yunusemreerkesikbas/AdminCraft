-- V35: Restore description column to page_i18n
-- V34 dropped description alongside legacy columns (subtitle, meta_title, meta_description, description_html)
-- but the PageI18n entity and application layer still reference this column.
ALTER TABLE page_i18n ADD COLUMN description LONGTEXT NULL AFTER title;
