-- #ADMINCRAFT_IMPEX
-- Version-controlled ImpEx reference script.
-- Run via Admin UI /{lang}/impex when needed to seed navigation demo data.
-- Idempotent: safe to run multiple times.
-- Prerequisite: core tables (navigation_nodes, navigation_entries) must exist (Flyway).

-- ============================================
-- 1. ROOT NODES
-- ============================================

INSERT INTO navigation_nodes (uuid, uid, parent_id, position, sort_order, is_visible, is_tab, created_at, updated_at, created_by, updated_by)
VALUES ('c1000001-0000-4000-8000-000000000001', 'LandingMainNavNode', NULL, 'TOP', 0, TRUE, FALSE, NOW(), NOW(), NULL, NULL)
ON DUPLICATE KEY UPDATE position = VALUES(position), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_nodes (uuid, uid, parent_id, position, sort_order, is_visible, is_tab, created_at, updated_at, created_by, updated_by)
VALUES ('c1000002-0000-4000-8000-000000000002', 'LandingSocialNavNode', NULL, 'RIGHT', 1, TRUE, FALSE, NOW(), NOW(), NULL, NULL)
ON DUPLICATE KEY UPDATE position = VALUES(position), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

-- ============================================
-- 2. ROOT NODE I18N
-- ============================================

INSERT INTO navigation_node_i18n (uid, uuid, node_id, language, title, created_at, updated_at, created_by, updated_by)
SELECT 'c2000001-0000-4000-8000-000000000001', 'c2000001-0000-4000-8000-000000000101', n.id, 'TR', 'Ana Menu', NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingMainNavNode'
ON DUPLICATE KEY UPDATE title = VALUES(title), updated_at = NOW();

INSERT INTO navigation_node_i18n (uid, uuid, node_id, language, title, created_at, updated_at, created_by, updated_by)
SELECT 'c2000002-0000-4000-8000-000000000002', 'c2000002-0000-4000-8000-000000000102', n.id, 'EN', 'Main Menu', NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingMainNavNode'
ON DUPLICATE KEY UPDATE title = VALUES(title), updated_at = NOW();

INSERT INTO navigation_node_i18n (uid, uuid, node_id, language, title, created_at, updated_at, created_by, updated_by)
SELECT 'c2000003-0000-4000-8000-000000000003', 'c2000003-0000-4000-8000-000000000103', n.id, 'TR', 'Sosyal Medya', NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingSocialNavNode'
ON DUPLICATE KEY UPDATE title = VALUES(title), updated_at = NOW();

INSERT INTO navigation_node_i18n (uid, uuid, node_id, language, title, created_at, updated_at, created_by, updated_by)
SELECT 'c2000004-0000-4000-8000-000000000004', 'c2000004-0000-4000-8000-000000000104', n.id, 'EN', 'Social Media', NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingSocialNavNode'
ON DUPLICATE KEY UPDATE title = VALUES(title), updated_at = NOW();

-- ============================================
-- 3. MAIN NAV ENTRIES (Hakkimda / Iletisim / Portfolyo)
-- ============================================

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT 'c3000001-0000-4000-8000-000000000001', 'LandingMainNavAbout', n.id, 'PAGE', 'about', NULL, NULL, '_self', FALSE, 0, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingMainNavNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), item_id = VALUES(item_id), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT 'c3000002-0000-4000-8000-000000000002', 'LandingMainNavContact', n.id, 'PAGE', 'contact', NULL, NULL, '_self', FALSE, 1, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingMainNavNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), item_id = VALUES(item_id), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT 'c3000003-0000-4000-8000-000000000003', 'LandingMainNavPortfolio', n.id, 'PAGE', 'portfolio', NULL, NULL, '_self', FALSE, 2, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingMainNavNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), item_id = VALUES(item_id), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

-- ============================================
-- 4. SOCIAL NAV ENTRIES (Behance / Dribbble / Instagram)
-- ============================================

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT 'c3000004-0000-4000-8000-000000000004', 'LandingSocialNavBehance', n.id, 'URL', NULL, 'https://www.behance.net', NULL, '_blank', TRUE, 0, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingSocialNavNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), url = VALUES(url), target = VALUES(target), is_external = VALUES(is_external), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT 'c3000005-0000-4000-8000-000000000005', 'LandingSocialNavDribbble', n.id, 'URL', NULL, 'https://dribbble.com', NULL, '_blank', TRUE, 1, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingSocialNavNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), url = VALUES(url), target = VALUES(target), is_external = VALUES(is_external), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

INSERT INTO navigation_entries (uuid, uid, node_id, item_type, item_id, url, link_color, target, is_external, sort_order, is_visible, created_at, updated_at, created_by, updated_by)
SELECT 'c3000006-0000-4000-8000-000000000006', 'LandingSocialNavInstagram', n.id, 'URL', NULL, 'https://www.instagram.com', NULL, '_blank', TRUE, 2, TRUE, NOW(), NOW(), NULL, NULL
FROM navigation_nodes n WHERE n.uid = 'LandingSocialNavNode'
ON DUPLICATE KEY UPDATE item_type = VALUES(item_type), url = VALUES(url), target = VALUES(target), is_external = VALUES(is_external), sort_order = VALUES(sort_order), is_visible = VALUES(is_visible), updated_at = NOW();

-- ============================================
-- 5. ENTRY I18N
-- ============================================

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000001-0000-4000-8000-000000000001', 'c4000001-0000-4000-8000-000000000101', e.id, 'TR', 'Hakkimda', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingMainNavAbout'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000002-0000-4000-8000-000000000002', 'c4000002-0000-4000-8000-000000000102', e.id, 'EN', 'About', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingMainNavAbout'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000003-0000-4000-8000-000000000003', 'c4000003-0000-4000-8000-000000000103', e.id, 'TR', 'Iletisim', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingMainNavContact'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000004-0000-4000-8000-000000000004', 'c4000004-0000-4000-8000-000000000104', e.id, 'EN', 'Contact', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingMainNavContact'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000005-0000-4000-8000-000000000005', 'c4000005-0000-4000-8000-000000000105', e.id, 'TR', 'Portfolyo', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingMainNavPortfolio'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000006-0000-4000-8000-000000000006', 'c4000006-0000-4000-8000-000000000106', e.id, 'EN', 'Portfolio', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingMainNavPortfolio'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000007-0000-4000-8000-000000000007', 'c4000007-0000-4000-8000-000000000107', e.id, 'TR', 'Behance', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingSocialNavBehance'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000008-0000-4000-8000-000000000008', 'c4000008-0000-4000-8000-000000000108', e.id, 'EN', 'Behance', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingSocialNavBehance'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000009-0000-4000-8000-000000000009', 'c4000009-0000-4000-8000-000000000109', e.id, 'TR', 'Dribbble', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingSocialNavDribbble'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000010-0000-4000-8000-000000000010', 'c4000010-0000-4000-8000-000000000110', e.id, 'EN', 'Dribbble', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingSocialNavDribbble'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000011-0000-4000-8000-000000000011', 'c4000011-0000-4000-8000-000000000111', e.id, 'TR', 'Instagram', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingSocialNavInstagram'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();

INSERT INTO navigation_entry_i18n (uid, uuid, entry_id, language, link_name, created_at, updated_at, created_by, updated_by)
SELECT 'c4000012-0000-4000-8000-000000000012', 'c4000012-0000-4000-8000-000000000112', e.id, 'EN', 'Instagram', NOW(), NOW(), NULL, NULL
FROM navigation_entries e WHERE e.uid = 'LandingSocialNavInstagram'
ON DUPLICATE KEY UPDATE link_name = VALUES(link_name), updated_at = NOW();
