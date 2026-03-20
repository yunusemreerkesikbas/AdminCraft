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
  (UUID(), 'SimpleBannerComponent',      'Banner',                'hero',       false, NOW(), NOW()),
  (UUID(), 'CMSImageComponent',          'Image',                 'content',    false, NOW(), NOW()),
  (UUID(), 'CMSLinkComponent',           'CTA Button',            'cta',        false, NOW(), NOW()),
  (UUID(), 'CMSParagraphComponent',      'Paragraph',             'content',    false, NOW(), NOW()),
  (UUID(), 'RotatingImagesComponent',    'Image Slider',          'gallery',    false, NOW(), NOW()),
  (UUID(), 'FeatureCardComponent',       'Card',                  'feature',    false, NOW(), NOW()),
  (UUID(), 'ContentHeroComponent',       'Content Hero',          'hero',       false, NOW(), NOW()),
  (UUID(), 'ServiceHeroComponent',       'Service Hero',          'hero',       false, NOW(), NOW()),
  (UUID(), 'ServiceCardsGridComponent',  'Service Cards Grid',    'layout',     false, NOW(), NOW()),
  (UUID(), 'ServicePanelComponent',      'Service Panels',        'content',    false, NOW(), NOW()),
  (UUID(), 'BrandGridComponent',         'Brand Grid',            'gallery',    false, NOW(), NOW()),
  (UUID(), 'ImageMarqueeComponent',      'Image Marquee',         'gallery',    false, NOW(), NOW()),
  (UUID(), 'BigTextCtaComponent',        'Big Text CTA',          'content',    false, NOW(), NOW()),
  (UUID(), 'SplitMediaIntroComponent',   'Split Media Intro',     'content',    false, NOW(), NOW()),
  (UUID(), 'PeopleCarouselComponent',    'People Carousel',       'people',     false, NOW(), NOW()),
  (UUID(), 'StatsGridComponent',         'Stats Grid',            'metrics',    false, NOW(), NOW()),
  (UUID(), 'LogoMarqueeComponent',       'Logo Marquee',          'gallery',    false, NOW(), NOW()),
  (UUID(), 'AwardsShowcaseComponent',    'Awards Showcase',       'showcase',   false, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name               = VALUES(name),
  category           = VALUES(category),
  is_navigation_aware= VALUES(is_navigation_aware),
  updated_at         = NOW();
