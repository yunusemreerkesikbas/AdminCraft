-- Seed default site settings for new tenants
-- Repeatable migration: runs on every checksum change

-- Insert default site settings if not exists
INSERT IGNORE INTO site_settings (setting_key, setting_value, language, setting_type, category, display_name, is_public, sort_order)
VALUES 
    ('site.name', 'My Company', NULL, 'TEXT', 'general', 'Site Name', TRUE, 1),
    ('site.description', 'Welcome to our website', NULL, 'TEXT', 'general', 'Site Description', TRUE, 2),
    ('site.timezone', 'Europe/Istanbul', NULL, 'TEXT', 'general', 'Timezone', FALSE, 3),
    ('site.currency', 'TRY', NULL, 'TEXT', 'general', 'Currency', FALSE, 4),
    ('site.maintenance_mode', 'false', NULL, 'BOOLEAN', 'general', 'Maintenance Mode', FALSE, 5);

-- Turkish translations
INSERT IGNORE INTO site_settings (setting_key, setting_value, language, setting_type, category, display_name, is_public, sort_order)
VALUES 
    ('site.welcome_message', 'Hoş Geldiniz', 'TR', 'I18N_TEXT', 'content', 'Welcome Message', TRUE, 10),
    ('site.footer_text', '© 2024 Tüm hakları saklıdır', 'TR', 'I18N_TEXT', 'content', 'Footer Text', TRUE, 11);

-- English translations
INSERT IGNORE INTO site_settings (setting_key, setting_value, language, setting_type, category, display_name, is_public, sort_order)
VALUES 
    ('site.welcome_message', 'Welcome', 'EN', 'I18N_TEXT', 'content', 'Welcome Message', TRUE, 10),
    ('site.footer_text', '© 2024 All rights reserved', 'EN', 'I18N_TEXT', 'content', 'Footer Text', TRUE, 11);


