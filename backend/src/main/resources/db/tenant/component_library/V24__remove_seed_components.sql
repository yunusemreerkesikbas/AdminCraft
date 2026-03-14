-- Remove deprecated Seed* components (from seed_components.sql)
-- Replaced by Homepage* components from seed_liko_components.sql
-- CASCADE handles slot_components, component_entries, component_i18n, component_media_links
DELETE FROM components
WHERE uid IN (
    'SeedHeroBanner', 'SeedWelcomeParagraph', 'SeedCtaShopNow',
    'SeedSection2Banner', 'SeedProductSummaryCta', 'SeedLandingPortfolioGrid'
);
