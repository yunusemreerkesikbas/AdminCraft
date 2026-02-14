-- Seed data for component types
-- Repeatable migration: runs on every checksum change
-- Updated: Sprint 35 - SAP Hybris compatible UIDs (PascalCase + Component suffix)

-- Idempotent upsert: keep existing component/component-slot relations intact.
INSERT INTO component_types (uuid, uid, name, category, created_at, updated_at)
VALUES
  (UUID(), 'HeaderComponent', 'Header', 'navigation', NOW(), NOW()),
  (UUID(), 'FooterComponent', 'Footer', 'navigation', NOW(), NOW()),
  (UUID(), 'SimpleBannerComponent', 'Banner', 'hero', NOW(), NOW()),
  (UUID(), 'CMSImageComponent', 'Image', 'content', NOW(), NOW()),
  (UUID(), 'CMSLinkComponent', 'CTA Button', 'cta', NOW(), NOW()),
  (UUID(), 'CMSParagraphComponent', 'Paragraph', 'content', NOW(), NOW()),
  (UUID(), 'CategoryNavigationComponent', 'Category Navigation', 'navigation', NOW(), NOW()),
  (UUID(), 'RotatingImagesComponent', 'Image Slider', 'gallery', NOW(), NOW()),
  (UUID(), 'FeatureCardComponent', 'Card', 'feature', NOW(), NOW()),
  (UUID(), 'CustomerReviewComponent', 'Testimonial', 'testimonial', NOW(), NOW()),
  (UUID(), 'ImageMapComponent', 'Gallery', 'gallery', NOW(), NOW()),
  (UUID(), 'PricingTableComponent', 'Pricing Table', 'pricing', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  updated_at = NOW();
