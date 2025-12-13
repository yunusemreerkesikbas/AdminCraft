-- Sprint 27: Page Template System - Seed Data
-- Pre-seeded system templates that cannot be deleted

-- Insert system templates
INSERT INTO page_templates (uuid, uid, name, description, is_system, is_active)
VALUES
    (UUID(), 'homepage-template', 'Homepage Template', 'Standard homepage layout with hero, main content and footer', TRUE, TRUE),
    (UUID(), 'content-page-template', 'Content Page Template', 'Content page with sidebar for articles and blog posts', TRUE, TRUE),
    (UUID(), 'product-list-page-template', 'Product List Page Template', 'Product listing page with filters and pagination', TRUE, TRUE),
    (UUID(), 'product-detail-page-template', 'Product Detail Page Template', 'Product detail page with gallery and related products', TRUE, TRUE),
    (UUID(), 'static-page-template', 'Static Page Template', 'Simple static page for About, Contact, FAQ etc.', TRUE, TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- Insert template slots for homepage-template
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Header', 'TOP', 0, TRUE FROM page_templates WHERE uid = 'homepage-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'HeroSection', 'TOP', 1, FALSE FROM page_templates WHERE uid = 'homepage-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'MainContent', 'CENTER', 2, TRUE FROM page_templates WHERE uid = 'homepage-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Footer', 'BOTTOM', 3, TRUE FROM page_templates WHERE uid = 'homepage-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Insert template slots for content-page-template
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Header', 'TOP', 0, TRUE FROM page_templates WHERE uid = 'content-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'MainContent', 'CENTER', 1, TRUE FROM page_templates WHERE uid = 'content-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Sidebar', 'RIGHT', 2, FALSE FROM page_templates WHERE uid = 'content-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Footer', 'BOTTOM', 3, TRUE FROM page_templates WHERE uid = 'content-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Insert template slots for product-list-page-template
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Header', 'TOP', 0, TRUE FROM page_templates WHERE uid = 'product-list-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Filters', 'LEFT', 1, FALSE FROM page_templates WHERE uid = 'product-list-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'ProductGrid', 'CENTER', 2, TRUE FROM page_templates WHERE uid = 'product-list-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Pagination', 'BOTTOM', 3, FALSE FROM page_templates WHERE uid = 'product-list-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Footer', 'BOTTOM', 4, TRUE FROM page_templates WHERE uid = 'product-list-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Insert template slots for product-detail-page-template
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Header', 'TOP', 0, TRUE FROM page_templates WHERE uid = 'product-detail-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'ProductGallery', 'LEFT', 1, TRUE FROM page_templates WHERE uid = 'product-detail-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'ProductInfo', 'RIGHT', 2, TRUE FROM page_templates WHERE uid = 'product-detail-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'RelatedProducts', 'BOTTOM', 3, FALSE FROM page_templates WHERE uid = 'product-detail-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Footer', 'BOTTOM', 4, TRUE FROM page_templates WHERE uid = 'product-detail-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Insert template slots for static-page-template
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Header', 'TOP', 0, TRUE FROM page_templates WHERE uid = 'static-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'MainContent', 'CENTER', 1, TRUE FROM page_templates WHERE uid = 'static-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), CONCAT('slot_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)), id, 'Footer', 'BOTTOM', 2, TRUE FROM page_templates WHERE uid = 'static-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);
