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

-- Homepage Content slot should render only the portfolio grid component.
DELETE sc
FROM slot_components sc
JOIN page_slots ps ON ps.id = sc.slot_id
JOIN pages p ON p.id = ps.page_id
JOIN components c ON c.id = sc.component_id
WHERE p.uid = 'homepage'
  AND ps.slot_name = 'Content'
  AND c.uid <> 'SeedLandingPortfolioGrid';

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-HeaderSlot' AND c.uid = 'SeedHeaderComponent'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-ContentSlot' AND c.uid = 'SeedLandingPortfolioGrid'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-FooterSlot' AND c.uid = 'SeedFooterComponent'
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

-- ============================================
-- 3. SHARED HEADER/FOOTER SLOTS (is_shared=TRUE, page_id=NULL)
-- ============================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
VALUES ('bb000001-0000-4000-8000-000000000001', 'SharedHeaderSlot', NULL, 'Header', 'TOP', -1, TRUE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
VALUES ('bb000002-0000-4000-8000-000000000002', 'SharedFooterSlot', NULL, 'Footer', 'BOTTOM', 99, TRUE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- SharedHeaderSlot → SeedHeaderComponent
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedHeaderSlot' AND c.uid = 'SeedHeaderComponent'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- SharedFooterSlot → SeedFooterComponent
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'SeedFooterComponent'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);
