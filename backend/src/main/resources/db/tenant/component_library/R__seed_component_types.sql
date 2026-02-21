-- Seed data for component types
-- Repeatable migration: runs on every checksum change

INSERT INTO component_types (
  uuid,
  uid,
  name,
  category,
  is_navigation_aware,
  created_at,
  updated_at
)
VALUES
  (UUID(), 'NavigationComponent',        'Navigation Component',  'navigation', true,  NOW(), NOW()),
  (UUID(), 'HeaderComponent',            'Header',                'navigation', true,  NOW(), NOW()),
  (UUID(), 'FooterComponent',            'Footer',                'navigation', true,  NOW(), NOW()),
  (UUID(), 'SimpleBannerComponent',      'Banner',                'hero',       false, NOW(), NOW()),
  (UUID(), 'CMSImageComponent',          'Image',                 'content',    false, NOW(), NOW()),
  (UUID(), 'CMSLinkComponent',           'CTA Button',            'cta',        false, NOW(), NOW()),
  (UUID(), 'CMSParagraphComponent',      'Paragraph',             'content',    false, NOW(), NOW()),
  (UUID(), 'CategoryNavigationComponent','Category Navigation',   'navigation', true,  NOW(), NOW()),
  (UUID(), 'RotatingImagesComponent',    'Image Slider',          'gallery',    false, NOW(), NOW()),
  (UUID(), 'FeatureCardComponent',       'Card',                  'feature',    false, NOW(), NOW()),
  (UUID(), 'CustomerReviewComponent',    'Testimonial',           'testimonial',false, NOW(), NOW()),
  (UUID(), 'ImageMapComponent',          'Gallery',               'gallery',    false, NOW(), NOW()),
  (UUID(), 'PricingTableComponent',      'Pricing Table',         'pricing',    false, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name               = VALUES(name),
  category           = VALUES(category),
  is_navigation_aware= VALUES(is_navigation_aware),
  updated_at         = NOW();
