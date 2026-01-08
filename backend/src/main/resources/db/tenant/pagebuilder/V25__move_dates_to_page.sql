-- Move published_at and scheduled_at from page_i18n to page table

ALTER TABLE pages ADD COLUMN published_at datetime(6);
ALTER TABLE pages ADD COLUMN scheduled_at datetime(6);

-- Optional: Migrate data from first available translation (Strategy decision needed: taking MAX or First?)
-- For now, we leave them NULL to avoid incorrect assumptions, or the user can run a manual script.
-- UPDATE pages p JOIN page_i18n pi ON p.id = pi.page_id SET p.published_at = pi.published_at WHERE pi.published_at IS NOT NULL;

ALTER TABLE page_i18n DROP COLUMN published_at;
ALTER TABLE page_i18n DROP COLUMN scheduled_at;
