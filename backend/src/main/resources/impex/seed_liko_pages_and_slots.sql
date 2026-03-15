-- #ADMINCRAFT_IMPEX
-- Liko Home-2 Pages & Slot Wiring — ImpEx seed file.
-- Run via Admin UI /{lang}/impex AFTER seed_liko_components.sql and seed_liko_chrome_components.sql.
-- Idempotent: safe to run multiple times.
-- Prerequisites:
--   1. R__seed_page_templates.sql applied (Section4-8 slots exist in template_slots)
--   2. seed_liko_components.sql executed (8 homepage components exist)
--   3. seed_liko_chrome_components.sql executed (shared header/footer components exist)

-- ============================================================
-- 1. HOMEPAGE PAGE (idempotent — already created by seed_pages_and_slots.sql)
-- ============================================================

INSERT INTO pages (uuid, uid, template_id, status, page_type, is_home, robot_tag, created_by)
SELECT 'f0000001-0000-0000-0000-000000000001', 'homepage',
    (SELECT id FROM page_templates WHERE uid = 'LandingPageTemplate'),
    'PUBLISHED', 'LANDING', TRUE, 'INDEX_FOLLOW', NULL
ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), status = VALUES(status),
    page_type = VALUES(page_type), is_home = VALUES(is_home), robot_tag = VALUES(robot_tag);

-- ============================================================
-- 2. PAGE_I18N (idempotent)
-- ============================================================

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000001-0000-0000-0001-000000000001', 'homepage-tr',
    (SELECT id FROM pages WHERE uid = 'homepage'),
    'TR', 'Anasayfa', 'Hoş Geldiniz', '/', 'PUBLISHED'
ON DUPLICATE KEY UPDATE name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

INSERT INTO page_i18n (uuid, uid, page_id, language, name, title, canonical_url, status)
SELECT 'f0000001-0000-0000-0001-000000000002', 'homepage-en',
    (SELECT id FROM pages WHERE uid = 'homepage'),
    'EN', 'Homepage', 'Welcome', '/', 'PUBLISHED'
ON DUPLICATE KEY UPDATE name = VALUES(name), title = VALUES(title),
    canonical_url = VALUES(canonical_url), status = VALUES(status);

-- ============================================================
-- 3. PAGE_SLOTS — generate content slots for homepage from template_slots
--    Covers Section1-8 only. Header/Footer are handled by shared slots (Section 4).
-- ============================================================

-- Note: existing homepage-HeaderSlot / homepage-FooterSlot rows (if any) are harmless —
-- the backend prefers shared slots over page-specific ones when both exist (Fix 2).
INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT UUID(), CONCAT(p.uid, '-', ts.slot_name, 'Slot'), p.id, ts.slot_name, ts.position, ts.sort_order, TRUE, FALSE, NOW(), NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
WHERE p.uid = 'homepage'
  AND ts.slot_name IN ('Section1','Section2','Section3','Section4','Section5','Section6','Section7','Section8')
ON DUPLICATE KEY UPDATE position = VALUES(position), sort_order = VALUES(sort_order), updated_at = NOW();

-- ============================================================
-- 4. SHARED SLOTS (idempotent)
-- ============================================================

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
VALUES ('bb000001-0000-4000-8000-000000000001', 'SharedHeaderSlot', NULL, 'Header', 'TOP', -1, TRUE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
VALUES ('bb000002-0000-4000-8000-000000000002', 'SharedFooterSlot', NULL, 'Footer', 'BOTTOM', 99, TRUE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- ============================================================
-- 5. SLOT_COMPONENTS — wire shared Header/Footer slots
-- ============================================================

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedHeaderSlot' AND c.uid = 'StorefrontHeaderMainNavigation'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 1, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedHeaderSlot' AND c.uid = 'StorefrontHeaderSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 2, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedHeaderSlot' AND c.uid = 'StorefrontHeaderContactInfo'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'StorefrontFooterBrandBlock'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 1, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'StorefrontFooterSitemapNavigation'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 2, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'StorefrontFooterOfficeLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 3, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'StorefrontFooterNewsletter'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 4, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'SharedFooterSlot' AND c.uid = 'StorefrontFooterSocialLinks'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), is_visible = VALUES(is_visible);

-- ============================================================
-- 6. SLOT_COMPONENTS — wire 8 homepage sections to components
-- ============================================================

-- Section1 → HomepageHeroBanner
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section1Slot' AND c.uid = 'HomepageHeroBanner'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Section2 → HomepageAboutSection
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section2Slot' AND c.uid = 'HomepageAboutSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Section3 → HomepageVideoSection
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section3Slot' AND c.uid = 'HomepageVideoSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Section4 → HomepageServiceSection
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section4Slot' AND c.uid = 'HomepageServiceSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Section5 → HomepageProjectSection
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section5Slot' AND c.uid = 'HomepageProjectSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Section6 → HomepageAwardSection
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section6Slot' AND c.uid = 'HomepageAwardSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Section7 → HomepageMarqueeText
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section7Slot' AND c.uid = 'HomepageMarqueeText'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- Section8 → HomepageInstagramSection
INSERT INTO slot_components (slot_id, component_id, sort_order, is_visible, created_at)
SELECT ps.id, c.id, 0, TRUE, NOW()
FROM page_slots ps, components c
WHERE ps.uid = 'homepage-Section8Slot' AND c.uid = 'HomepageInstagramSection'
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);
