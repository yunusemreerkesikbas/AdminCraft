-- #CRAFTIVE_IMPEX

-- Seed data for entry field definitions
-- Source: R__seed_entry_field_definitions.sql

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

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'body', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'CMSParagraphComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

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

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'category', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'FeatureCardComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

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

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContentHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContentHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContentHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'supportingText', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'ContentHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'scrollTarget', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ContentHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ServiceHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'overlayMediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ServiceHeroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ServiceCardsGridComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ServicePanelComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'panelId', 'NUMBER', NOW()
FROM component_types ct WHERE ct.uid = 'ServicePanelComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'panelSubtitle', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ServicePanelComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'items', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'ServicePanelComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ServicePanelComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ServicePanelComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'BrandGridComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'texts', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'BrandGridComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ImageMarqueeComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'altText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'ImageMarqueeComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'BigTextCtaComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'buttonUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'BigTextCtaComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'SplitMediaIntroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'introLabel', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'SplitMediaIntroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'items', 'TEXTAREA', NOW()
FROM component_types ct WHERE ct.uid = 'SplitMediaIntroComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'PeopleCarouselComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'role', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'PeopleCarouselComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'profileUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'PeopleCarouselComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'statValue', 'NUMBER', NOW()
FROM component_types ct WHERE ct.uid = 'StatsGridComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'statSuffix', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'StatsGridComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'LogoMarqueeComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'linkUrl', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'LogoMarqueeComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'altText', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'LogoMarqueeComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'subtitle', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'AwardsShowcaseComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'date', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'AwardsShowcaseComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);

INSERT INTO entry_field_definitions (component_type_id, field_key, field_type, created_at)
SELECT ct.id, 'mediaUid', 'TEXT', NOW()
FROM component_types ct WHERE ct.uid = 'AwardsShowcaseComponent'
ON DUPLICATE KEY UPDATE field_type = VALUES(field_type);
