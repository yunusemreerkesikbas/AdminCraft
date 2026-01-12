-- =====================================================
-- Seed: Product Types and Attribute Definitions
-- Sprint 40: Product Module
-- =====================================================

-- Product Types
INSERT INTO product_types (id, uuid, uid, code, name, category) VALUES
(10000, UUID(), 'producttype_general', 'general', 'General', 'general'),
(10001, UUID(), 'producttype_electronics', 'electronics', 'Electronics', 'electronics'),
(10002, UUID(), 'producttype_clothing', 'clothing', 'Clothing', 'clothing')
ON DUPLICATE KEY UPDATE name = VALUES(name), category = VALUES(category);

-- General Product Type Attributes
INSERT INTO product_attribute_definitions (id, uuid, uid, product_type_id, code, name, field_type, is_required, is_searchable, sort_order) VALUES
(10000, UUID(), 'attr_general_brand', 10000, 'brand', 'Brand', 'TEXT', FALSE, TRUE, 1),
(10001, UUID(), 'attr_general_description_long', 10000, 'description_long', 'Long Description', 'RICHTEXT', FALSE, FALSE, 2)
ON DUPLICATE KEY UPDATE name = VALUES(name), field_type = VALUES(field_type);

-- Electronics Product Type Attributes
INSERT INTO product_attribute_definitions (id, uuid, uid, product_type_id, code, name, field_type, is_required, is_searchable, sort_order) VALUES
(10010, UUID(), 'attr_electronics_brand', 10001, 'brand', 'Brand', 'TEXT', TRUE, TRUE, 1),
(10011, UUID(), 'attr_electronics_model', 10001, 'model', 'Model', 'TEXT', TRUE, TRUE, 2),
(10012, UUID(), 'attr_electronics_warranty', 10001, 'warranty_months', 'Warranty (months)', 'NUMBER', FALSE, FALSE, 3),
(10013, UUID(), 'attr_electronics_specs', 10001, 'specifications', 'Specifications', 'RICHTEXT', FALSE, FALSE, 4)
ON DUPLICATE KEY UPDATE name = VALUES(name), field_type = VALUES(field_type);

-- Clothing Product Type Attributes
INSERT INTO product_attribute_definitions (id, uuid, uid, product_type_id, code, name, field_type, is_required, is_searchable, sort_order) VALUES
(10020, UUID(), 'attr_clothing_brand', 10002, 'brand', 'Brand', 'TEXT', TRUE, TRUE, 1),
(10021, UUID(), 'attr_clothing_material', 10002, 'material', 'Material', 'TEXT', FALSE, TRUE, 2),
(10022, UUID(), 'attr_clothing_care', 10002, 'care_instructions', 'Care Instructions', 'RICHTEXT', FALSE, FALSE, 3)
ON DUPLICATE KEY UPDATE name = VALUES(name), field_type = VALUES(field_type);
