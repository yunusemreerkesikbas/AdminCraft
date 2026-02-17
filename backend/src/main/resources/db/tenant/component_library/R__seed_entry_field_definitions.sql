-- Seed data for entry field definitions
-- Repeatable migration: runs on every checksum change
-- Sprint 35: 33 field definitions for 9 component types (SAP Hybris compatible UIDs)
-- Updated: 2026-02-16 - Minimal schema (fieldKey, fieldType only)

-- ============================================
-- BANNER FIELDS (4 fields)
-- Hybris: SimpleBannerComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'linkUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'linkTarget', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'altText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'SimpleBannerComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================
-- IMAGE FIELDS (3 fields)
-- Hybris: CMSImageComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'CMSImageComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'altText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'CMSImageComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'caption', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'CMSImageComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================
-- CTA FIELDS (4 fields)
-- Hybris: CMSLinkComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonStyle', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'openInNewTab', 'BOOLEAN', NOW()
FROM component_types ct WHERE ct.uid = 'CMSLinkComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================
-- PARAGRAPH FIELDS (1 field)
-- Hybris: CMSParagraphComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'body', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'CMSParagraphComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================
-- CARD FIELDS (3 fields)
-- Custom: FeatureCardComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'linkUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'badgeText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================
-- SLIDER FIELDS (4 fields)
-- Hybris: RotatingImagesComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'RotatingImagesComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'altText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'RotatingImagesComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'linkUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'RotatingImagesComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'duration', 'NUMBER', NOW()
FROM component_types ct WHERE ct.uid = 'RotatingImagesComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================
-- TESTIMONIAL FIELDS (5 fields)
-- Custom: CustomerReviewComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'authorName', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'CustomerReviewComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'authorTitle', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'CustomerReviewComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'authorImage', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'CustomerReviewComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'rating', 'NUMBER', NOW()
FROM component_types ct WHERE ct.uid = 'CustomerReviewComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'quote', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'CustomerReviewComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================
-- GALLERY FIELDS (3 fields)
-- Hybris: ImageMapComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'imageUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ImageMapComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'altText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ImageMapComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'thumbnailUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ImageMapComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

-- ============================================
-- PRICING FIELDS (6 fields)
-- Custom: PricingTableComponent
-- ============================================

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'planName', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'price', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'features', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'ctaText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'ctaUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'isPopular', 'BOOLEAN', NOW()
FROM component_types ct WHERE ct.uid = 'PricingTableComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);
