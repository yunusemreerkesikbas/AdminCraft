-- Seed page_slots and slot_components for sample pages (contentSlot data for CMS delivery)
-- Repeatable migration: runs after page templates + sample pages due filename ordering.
-- Depends on component_library R__seed_components (components with fixed UIDs).

-- ============================================
-- 1. PAGE_SLOTS (one per template slot per sample page)
-- ============================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT UUID(), CONCAT(p.uid, '-', ts.slot_name, 'Slot'), p.id, ts.slot_name, ts.position, ts.sort_order, TRUE, FALSE, NOW(), NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid IN ('homepage', 'productPage', 'categoryPage', 'searchResultsPage')
ON DUPLICATE KEY UPDATE position = VALUES(position), sort_order = VALUES(sort_order), updated_at = NOW();

-- ============================================
-- 2. SLOT_COMPONENTS (link slots to seed components)
-- ============================================

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-HeaderSlot' AND c.uid = 'SeedHeaderComponent'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section1Slot' AND c.uid = 'SeedHeroBanner'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 1, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section1Slot' AND c.uid = 'SeedWelcomeParagraph'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 2, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section1Slot' AND c.uid = 'SeedCtaShopNow'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section2Slot' AND c.uid = 'SeedSection2Banner'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'productPage-SummarySlot' AND c.uid = 'SeedProductSummaryCta'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'categoryPage-TopContentSlot' AND c.uid = 'SeedHeroBanner'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'searchResultsPage-TopContentSlot' AND c.uid = 'SeedWelcomeParagraph'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);
