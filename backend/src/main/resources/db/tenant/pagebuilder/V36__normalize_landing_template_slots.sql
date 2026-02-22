-- Normalize LandingPageTemplate slot model for shared multi-tenant reuse.
-- Target model: Header, Content, Footer

SET @landing_template_id := (
    SELECT id
    FROM page_templates
    WHERE uid = 'LandingPageTemplate'
    LIMIT 1
);

-- Ensure Content slot exists in template (migrate Section1 if needed).
UPDATE template_slots ts
LEFT JOIN template_slots existing_content
    ON existing_content.template_id = ts.template_id
   AND existing_content.slot_name = 'Content'
SET ts.slot_name = 'Content',
    ts.position = 'CENTER',
    ts.sort_order = 1,
    ts.uid = 'LandingPageContentSlot'
WHERE ts.template_id = @landing_template_id
  AND ts.slot_name = 'Section1'
  AND existing_content.id IS NULL;

INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'aa000013-0000-4000-8000-000000000013', 'LandingPageContentSlot', pt.id, 'Content', 'CENTER', 1, FALSE
FROM page_templates pt
WHERE pt.uid = 'LandingPageTemplate'
  AND NOT EXISTS (
      SELECT 1
      FROM template_slots ts
      WHERE ts.template_id = pt.id
        AND ts.slot_name = 'Content'
  );

-- Remove legacy Landing sections from template definition.
DELETE FROM template_slots
WHERE template_id = @landing_template_id
  AND slot_name IN ('Section1', 'Section2', 'Section3');

-- Keep chrome slot ordering deterministic.
UPDATE template_slots
SET position = 'TOP',
    sort_order = -1
WHERE template_id = @landing_template_id
  AND slot_name = 'Header';

UPDATE template_slots
SET position = 'BOTTOM',
    sort_order = 99
WHERE template_id = @landing_template_id
  AND slot_name = 'Footer';

-- Migrate existing landing page slots to Content when possible.
UPDATE page_slots ps
JOIN pages p ON p.id = ps.page_id
JOIN page_templates pt ON pt.id = p.template_id
LEFT JOIN page_slots content_slot
    ON content_slot.page_id = ps.page_id
   AND content_slot.slot_name = 'Content'
SET ps.slot_name = 'Content',
    ps.position = 'CENTER',
    ps.sort_order = 1,
    ps.uid = CASE
        WHEN ps.uid LIKE '%-Section1Slot' THEN REPLACE(ps.uid, '-Section1Slot', '-ContentSlot')
        ELSE ps.uid
    END
WHERE pt.uid = 'LandingPageTemplate'
  AND ps.slot_name = 'Section1'
  AND content_slot.id IS NULL;

-- Ensure every landing page has a Content slot.
INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT UUID(), CONCAT(p.uid, '-ContentSlot'), p.id, 'Content', 'CENTER', 1, TRUE, FALSE, NOW(), NOW()
FROM pages p
JOIN page_templates pt ON pt.id = p.template_id
LEFT JOIN page_slots ps ON ps.page_id = p.id AND ps.slot_name = 'Content'
WHERE pt.uid = 'LandingPageTemplate'
  AND ps.id IS NULL;

-- Cleanup orphan legacy landing section slots on pages.
DELETE ps
FROM page_slots ps
JOIN pages p ON p.id = ps.page_id
JOIN page_templates pt ON pt.id = p.template_id
WHERE pt.uid = 'LandingPageTemplate'
  AND ps.slot_name IN ('Section2', 'Section3');
