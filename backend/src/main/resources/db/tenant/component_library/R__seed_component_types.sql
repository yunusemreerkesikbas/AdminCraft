-- Seed data for component types
-- Repeatable migration: runs on every checksum change
-- Updated: Sprint 35 - SAP Hybris compatible UIDs (PascalCase + Component suffix)

-- Idempotent upsert: keep existing component/component-slot relations intact.
INSERT INTO component_types (
  uuid,
  uid,
  name,
  category,
  navigation_profile,
  created_at,
  updated_at
)
VALUES
  (UUID(), 'HeaderComponent', 'Header', 'navigation', 'NODE_WITH_LINK', NOW(), NOW()),
  (UUID(), 'FooterComponent', 'Footer', 'navigation', 'NODE', NOW(), NOW()),
  (UUID(), 'SimpleBannerComponent', 'Banner', 'hero', 'NONE', NOW(), NOW()),
  (UUID(), 'CMSImageComponent', 'Image', 'content', 'NONE', NOW(), NOW()),
  (UUID(), 'CMSLinkComponent', 'CTA Button', 'cta', 'NONE', NOW(), NOW()),
  (UUID(), 'CMSParagraphComponent', 'Paragraph', 'content', 'NONE', NOW(), NOW()),
  (UUID(), 'CategoryNavigationComponent', 'Category Navigation', 'navigation', 'CATEGORY', NOW(), NOW()),
  (UUID(), 'RotatingImagesComponent', 'Image Slider', 'gallery', 'NONE', NOW(), NOW()),
  (UUID(), 'FeatureCardComponent', 'Card', 'feature', 'NONE', NOW(), NOW()),
  (UUID(), 'CustomerReviewComponent', 'Testimonial', 'testimonial', 'NONE', NOW(), NOW()),
  (UUID(), 'ImageMapComponent', 'Gallery', 'gallery', 'NONE', NOW(), NOW()),
  (UUID(), 'PricingTableComponent', 'Pricing Table', 'pricing', 'NONE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  navigation_profile = VALUES(navigation_profile),
  updated_at = NOW();
