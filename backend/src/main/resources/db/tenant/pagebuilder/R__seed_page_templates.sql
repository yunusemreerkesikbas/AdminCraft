-- Sprint 27: Page Template System - Seed Data
-- Pre-seeded system templates that cannot be deleted
-- Uses deterministic UUIDs (36 chars) for idempotent migrations

-- Insert system templates with deterministic UUIDs
INSERT INTO page_templates (uuid, uid, name, description, is_system, is_active)
VALUES
    ('00000001-0000-0000-0000-000000000001', 'homepage-template', 'Homepage Template', 'Standard homepage layout with hero, main content and footer', TRUE, TRUE),
    ('00000001-0000-0000-0000-000000000002', 'content-page-template', 'Content Page Template', 'Content page with sidebar for articles and blog posts', TRUE, TRUE),
    ('00000001-0000-0000-0000-000000000003', 'product-list-page-template', 'Product List Page Template', 'Product listing page with filters and pagination', TRUE, TRUE),
    ('00000001-0000-0000-0000-000000000004', 'product-detail-page-template', 'Product Detail Page Template', 'Product detail page with gallery and related products', TRUE, TRUE),
    ('00000001-0000-0000-0000-000000000005', 'static-page-template', 'Static Page Template', 'Simple static page for About, Contact, FAQ etc.', TRUE, TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- Insert template slots for homepage-template
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0001-0000-0000-000000000001', 'slot_homepage_header', id, 'Header', 'TOP', 0, TRUE FROM page_templates WHERE uid = 'homepage-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0001-0000-0000-000000000002', 'slot_homepage_hero', id, 'HeroSection', 'TOP', 1, FALSE FROM page_templates WHERE uid = 'homepage-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0001-0000-0000-000000000003', 'slot_homepage_main', id, 'MainContent', 'CENTER', 2, TRUE FROM page_templates WHERE uid = 'homepage-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0001-0000-0000-000000000004', 'slot_homepage_footer', id, 'Footer', 'BOTTOM', 3, TRUE FROM page_templates WHERE uid = 'homepage-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Insert template slots for content-page-template
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0002-0000-0000-000000000001', 'slot_content_header', id, 'Header', 'TOP', 0, TRUE FROM page_templates WHERE uid = 'content-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0002-0000-0000-000000000002', 'slot_content_main', id, 'MainContent', 'CENTER', 1, TRUE FROM page_templates WHERE uid = 'content-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0002-0000-0000-000000000003', 'slot_content_sidebar', id, 'Sidebar', 'RIGHT', 2, FALSE FROM page_templates WHERE uid = 'content-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0002-0000-0000-000000000004', 'slot_content_footer', id, 'Footer', 'BOTTOM', 3, TRUE FROM page_templates WHERE uid = 'content-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Insert template slots for product-list-page-template
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0003-0000-0000-000000000001', 'slot_prodlist_header', id, 'Header', 'TOP', 0, TRUE FROM page_templates WHERE uid = 'product-list-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0003-0000-0000-000000000002', 'slot_prodlist_filters', id, 'Filters', 'LEFT', 1, FALSE FROM page_templates WHERE uid = 'product-list-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0003-0000-0000-000000000003', 'slot_prodlist_grid', id, 'ProductGrid', 'CENTER', 2, TRUE FROM page_templates WHERE uid = 'product-list-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0003-0000-0000-000000000004', 'slot_prodlist_pagination', id, 'Pagination', 'BOTTOM', 3, FALSE FROM page_templates WHERE uid = 'product-list-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0003-0000-0000-000000000005', 'slot_prodlist_footer', id, 'Footer', 'BOTTOM', 4, TRUE FROM page_templates WHERE uid = 'product-list-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Insert template slots for product-detail-page-template
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0004-0000-0000-000000000001', 'slot_proddetail_header', id, 'Header', 'TOP', 0, TRUE FROM page_templates WHERE uid = 'product-detail-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0004-0000-0000-000000000002', 'slot_proddetail_gallery', id, 'ProductGallery', 'LEFT', 1, TRUE FROM page_templates WHERE uid = 'product-detail-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0004-0000-0000-000000000003', 'slot_proddetail_info', id, 'ProductInfo', 'RIGHT', 2, TRUE FROM page_templates WHERE uid = 'product-detail-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0004-0000-0000-000000000004', 'slot_proddetail_related', id, 'RelatedProducts', 'BOTTOM', 3, FALSE FROM page_templates WHERE uid = 'product-detail-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0004-0000-0000-000000000005', 'slot_proddetail_footer', id, 'Footer', 'BOTTOM', 4, TRUE FROM page_templates WHERE uid = 'product-detail-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Insert template slots for static-page-template
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0005-0000-0000-000000000001', 'slot_static_header', id, 'Header', 'TOP', 0, TRUE FROM page_templates WHERE uid = 'static-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0005-0000-0000-000000000002', 'slot_static_main', id, 'MainContent', 'CENTER', 1, TRUE FROM page_templates WHERE uid = 'static-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT '00000001-0005-0000-0000-000000000003', 'slot_static_footer', id, 'Footer', 'BOTTOM', 2, TRUE FROM page_templates WHERE uid = 'static-page-template'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);
