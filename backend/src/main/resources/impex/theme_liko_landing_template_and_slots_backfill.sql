-- #CRAFTIVE_IMPEX
-- Liko Home-2 landing backfill for tenants that still have only Section1-3.
-- Run via Admin UI /{lang}/impex AFTER seed_liko_components.sql.
-- Safe to run multiple times.

-- 1. TEMPLATE_SLOTS — backfill Section4-8 on LandingPageTemplate
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT seed.slot_uuid, seed.slot_uid, pt.id, seed.slot_name, seed.position, seed.sort_order, FALSE
FROM page_templates pt
JOIN (
    SELECT 'dd000004-0000-4000-8000-000000000004' AS slot_uuid, 'LandingPageSection4Slot' AS slot_uid, 'Section4' AS slot_name, 'CENTER' AS position, 4 AS sort_order
    UNION ALL
    SELECT 'dd000005-0000-4000-8000-000000000005', 'LandingPageSection5Slot', 'Section5', 'CENTER', 5
    UNION ALL
    SELECT 'dd000006-0000-4000-8000-000000000006', 'LandingPageSection6Slot', 'Section6', 'BOTTOM', 6
    UNION ALL
    SELECT 'dd000007-0000-4000-8000-000000000007', 'LandingPageSection7Slot', 'Section7', 'BOTTOM', 7
    UNION ALL
    SELECT 'dd000008-0000-4000-8000-000000000008', 'LandingPageSection8Slot', 'Section8', 'BOTTOM', 8
) seed ON 1 = 1
WHERE pt.uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE
    uid = VALUES(uid),
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

-- 2. PAGE_SLOTS — backfill homepage Section4-8 slots from the landing template contract
INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT UUID(), CONCAT(LOWER(p.uid), '-', seed.slot_name, 'Slot'), p.id, seed.slot_name, seed.position, seed.sort_order, TRUE, FALSE, NOW(), NOW()
FROM pages p
JOIN page_templates pt ON pt.id = p.template_id
JOIN (
    SELECT 'Section4' AS slot_name, 'CENTER' AS position, 4 AS sort_order
    UNION ALL
    SELECT 'Section5', 'CENTER', 5
    UNION ALL
    SELECT 'Section6', 'BOTTOM', 6
    UNION ALL
    SELECT 'Section7', 'BOTTOM', 7
    UNION ALL
    SELECT 'Section8', 'BOTTOM', 8
) seed ON 1 = 1
WHERE pt.uid = 'LandingPageTemplate'
  AND LOWER(p.uid) = 'homepage'
ON DUPLICATE KEY UPDATE
    uid = VALUES(uid),
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_active = VALUES(is_active),
    is_shared = VALUES(is_shared),
    updated_at = NOW();

-- 3. SLOT_COMPONENTS — wire Section4-8 homepage slots to the Home-2 components
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, seed.sort_order, TRUE, NOW()
FROM pages p
JOIN page_templates pt ON pt.id = p.template_id
JOIN page_slots ps ON ps.page_id = p.id
JOIN (
    SELECT 'Section4' AS slot_name, 'HomepageServiceSection' AS component_uid, 0 AS sort_order
    UNION ALL
    SELECT 'Section5', 'HomepageProjectSection', 0
    UNION ALL
    SELECT 'Section6', 'HomepageAwardSection', 0
    UNION ALL
    SELECT 'Section7', 'HomepageMarqueeText', 0
    UNION ALL
    SELECT 'Section8', 'HomepageInstagramSection', 0
) seed ON seed.slot_name = ps.slot_name
JOIN components c ON c.uid = seed.component_uid
WHERE pt.uid = 'LandingPageTemplate'
  AND LOWER(p.uid) = 'homepage'
ON DUPLICATE KEY UPDATE
    sort_order = VALUES(sort_order),
    is_visible = VALUES(is_visible);
