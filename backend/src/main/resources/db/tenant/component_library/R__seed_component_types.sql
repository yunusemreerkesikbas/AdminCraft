-- Seed data for component types
-- Repeatable migration: runs on every checksum change
-- Updated: Sprint 35 - SAP Hybris compatible UIDs (PascalCase + Component suffix)

-- ============================================
-- CLEAN SLATE: Remove all existing data
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE entry_field_definitions;
TRUNCATE TABLE component_entry_i18n;
TRUNCATE TABLE component_entries;
TRUNCATE TABLE component_i18n;
TRUNCATE TABLE components;
TRUNCATE TABLE component_types;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- NAVIGATION COMPONENTS
-- ============================================

INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'HeaderComponent', 'Header', 'navigation', NOW(), NOW());

INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'FooterComponent', 'Footer', 'navigation', NOW(), NOW());

-- ============================================
-- SAP HYBRIS CORE COMPONENTS
-- ============================================

-- Banner (Hybris: SimpleBannerComponent)
INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'SimpleBannerComponent', 'Banner', 'hero', NOW(), NOW());

-- Image (Hybris: CMSImageComponent)
INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'CMSImageComponent', 'Image', 'content', NOW(), NOW());

-- CTA Link (Hybris: CMSLinkComponent)
INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'CMSLinkComponent', 'CTA Button', 'cta', NOW(), NOW());

-- Paragraph (Hybris: CMSParagraphComponent)
INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'CMSParagraphComponent', 'Paragraph', 'content', NOW(), NOW());

-- Slider (Hybris: RotatingImagesComponent)
INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'RotatingImagesComponent', 'Image Slider', 'gallery', NOW(), NOW());

-- ============================================
-- CUSTOM EXTENSION COMPONENTS
-- ============================================

-- Card
INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'FeatureCardComponent', 'Card', 'feature', NOW(), NOW());

-- Testimonial
INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'CustomerReviewComponent', 'Testimonial', 'testimonial', NOW(), NOW());

-- Gallery (Hybris: ImageMapComponent)
INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'ImageMapComponent', 'Gallery', 'gallery', NOW(), NOW());

-- Pricing Table
INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES (UUID(), 'PricingTableComponent', 'Pricing Table', 'pricing', NOW(), NOW());
