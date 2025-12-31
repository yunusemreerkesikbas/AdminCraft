-- Seed data for entry field definitions
-- Repeatable migration: runs on every checksum change
-- Sprint 35: 33 field definitions for 9 component types (SAP Hybris compatible UIDs)
-- Updated: 2025-12-31 - Force re-run after component_types update

-- ============================================
-- BANNER FIELDS (4 fields)
-- Hybris: SimpleBannerComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', TRUE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'linkUrl', 'TEXT', FALSE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'linkTarget', 'TEXT', FALSE, 20, NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'altText', 'TEXT', TRUE, 200, NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

-- ============================================
-- IMAGE FIELDS (3 fields)
-- Hybris: CMSImageComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', TRUE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'CMSImageComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'altText', 'TEXT', TRUE, 200, NOW()
FROM component_types ct WHERE ct.uid = 'CMSImageComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, created_at)
SELECT ct.id, 'caption', 'TEXTAREA', FALSE, NOW()
FROM component_types ct WHERE ct.uid = 'CMSImageComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required);

-- ============================================
-- CTA FIELDS (4 fields)
-- Hybris: CMSLinkComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'buttonText', 'TEXT', TRUE, 100, NOW()
FROM component_types ct WHERE ct.uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'buttonUrl', 'TEXT', TRUE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'buttonStyle', 'TEXT', FALSE, 50, NOW()
FROM component_types ct WHERE ct.uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, created_at)
SELECT ct.id, 'openInNewTab', 'BOOLEAN', FALSE, NOW()
FROM component_types ct WHERE ct.uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required);

-- ============================================
-- PARAGRAPH FIELDS (1 field)
-- Hybris: CMSParagraphComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, created_at)
SELECT ct.id, 'body', 'TEXTAREA', TRUE, NOW()
FROM component_types ct WHERE ct.uid = 'CMSParagraphComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required);

-- ============================================
-- CARD FIELDS (3 fields)
-- Custom: FeatureCardComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', FALSE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'linkUrl', 'TEXT', FALSE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'badgeText', 'TEXT', FALSE, 50, NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

-- ============================================
-- SLIDER FIELDS (4 fields)
-- Hybris: RotatingImagesComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', TRUE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'RotatingImagesComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'altText', 'TEXT', TRUE, 200, NOW()
FROM component_types ct WHERE ct.uid = 'RotatingImagesComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'linkUrl', 'TEXT', FALSE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'RotatingImagesComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, min_value, max_value, created_at)
SELECT ct.id, 'duration', 'NUMBER', FALSE, 1000, 10000, NOW()
FROM component_types ct WHERE ct.uid = 'RotatingImagesComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), min_value = VALUES(min_value), max_value = VALUES(max_value);

-- ============================================
-- TESTIMONIAL FIELDS (5 fields)
-- Custom: CustomerReviewComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'authorName', 'TEXT', TRUE, 100, NOW()
FROM component_types ct WHERE ct.uid = 'CustomerReviewComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'authorTitle', 'TEXT', FALSE, 100, NOW()
FROM component_types ct WHERE ct.uid = 'CustomerReviewComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'authorImage', 'TEXT', FALSE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'CustomerReviewComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, min_value, max_value, created_at)
SELECT ct.id, 'rating', 'NUMBER', FALSE, 1, 5, NOW()
FROM component_types ct WHERE ct.uid = 'CustomerReviewComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), min_value = VALUES(min_value), max_value = VALUES(max_value);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, created_at)
SELECT ct.id, 'quote', 'TEXTAREA', TRUE, NOW()
FROM component_types ct WHERE ct.uid = 'CustomerReviewComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required);

-- ============================================
-- GALLERY FIELDS (3 fields)
-- Hybris: ImageMapComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', TRUE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'ImageMapComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'altText', 'TEXT', TRUE, 200, NOW()
FROM component_types ct WHERE ct.uid = 'ImageMapComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'thumbnailUrl', 'TEXT', FALSE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'ImageMapComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

-- ============================================
-- PRICING FIELDS (6 fields)
-- Custom: PricingTableComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'planName', 'TEXT', TRUE, 100, NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'price', 'TEXT', TRUE, 50, NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, created_at)
SELECT ct.id, 'features', 'TEXTAREA', TRUE, NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'ctaText', 'TEXT', TRUE, 50, NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, max_length, created_at)
SELECT ct.id, 'ctaUrl', 'TEXT', TRUE, 500, NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required), max_length = VALUES(max_length);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, is_required, created_at)
SELECT ct.id, 'isPopular', 'BOOLEAN', FALSE, NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE is_required = VALUES(is_required);
