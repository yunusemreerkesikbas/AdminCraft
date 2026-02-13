-- Backfill missing system template slots for existing tenants
-- Ensures baseline slots exist even if historical seed data was incomplete

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'LandingPageSection1Slot', id, 'Section1', 'TOP', 0, FALSE
FROM page_templates
WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'LandingPageSection2Slot', id, 'Section2', 'CENTER', 1, FALSE
FROM page_templates
WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'LandingPageSection3Slot', id, 'Section3', 'BOTTOM', 2, FALSE
FROM page_templates
WHERE uid = 'LandingPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'ContentPageTopSlot', id, 'TopContent', 'TOP', 0, FALSE
FROM page_templates
WHERE uid = 'ContentPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'ContentPageBodySlot', id, 'BodyContent', 'CENTER', 1, TRUE
FROM page_templates
WHERE uid = 'ContentPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'ContentPageSideSlot', id, 'SideContent', 'RIGHT', 2, FALSE
FROM page_templates
WHERE uid = 'ContentPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'CategoryPageTopSlot', id, 'TopContent', 'TOP', 0, FALSE
FROM page_templates
WHERE uid = 'CategoryPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'CategoryPageContentSlot', id, 'ProductGrid', 'CENTER', 1, TRUE
FROM page_templates
WHERE uid = 'CategoryPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'ProductDetailsSummarySlot', id, 'Summary', 'TOP', 0, TRUE
FROM page_templates
WHERE uid = 'ProductDetailsPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'ProductDetailsTabsSlot', id, 'Tabs', 'CENTER', 1, FALSE
FROM page_templates
WHERE uid = 'ProductDetailsPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'ProductDetailsCrossSellingSlot', id, 'CrossSelling', 'BOTTOM', 2, FALSE
FROM page_templates
WHERE uid = 'ProductDetailsPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'SearchResultsTopSlot', id, 'TopContent', 'TOP', 0, FALSE
FROM page_templates
WHERE uid = 'SearchResultsPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'SearchResultsContentSlot', id, 'Results', 'CENTER', 1, TRUE
FROM page_templates
WHERE uid = 'SearchResultsPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'AccountPageBodySlot', id, 'BodyContent', 'CENTER', 0, TRUE
FROM page_templates
WHERE uid = 'AccountPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'LoginPageLeftSlot', id, 'LeftContent', 'LEFT', 0, FALSE
FROM page_templates
WHERE uid = 'LoginPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'LoginPageRightSlot', id, 'RightContent', 'RIGHT', 1, FALSE
FROM page_templates
WHERE uid = 'LoginPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'ErrorPageMiddleSlot', id, 'MiddleContent', 'CENTER', 0, TRUE
FROM page_templates
WHERE uid = 'ErrorPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT UUID(), 'NotFoundPageMiddleSlot', id, 'MiddleContent', 'CENTER', 0, TRUE
FROM page_templates
WHERE uid = 'NotFoundPageTemplate'
ON DUPLICATE KEY UPDATE
    position = VALUES(position),
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required);
