-- Migrate LandingPageTemplate from single Content slot to multi-section: Section1/Section2/Section3.
-- Frontend template-configs.ts expects Section1 (TOP), Section2 (CENTER), Section3 (BOTTOM).
-- V36 normalized to Content; this migration advances to the multi-section model.

SET @landing_id := (
    SELECT id FROM page_templates WHERE uid = 'LandingPageTemplate' LIMIT 1
);

-- ── Step 1: Rename existing Content template slot → Section1 ──────────────────
UPDATE template_slots
SET uid       = 'LandingPageSection1Slot',
    slot_name = 'Section1',
    position  = 'TOP',
    sort_order = 1
WHERE template_id = @landing_id
  AND slot_name   = 'Content';

-- ── Step 2: Add Section2 (CENTER) ────────────────────────────────────────────
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'dd000002-0000-4000-8000-000000000002', 'LandingPageSection2Slot', pt.id, 'Section2', 'CENTER', 2, FALSE
FROM page_templates pt
WHERE pt.uid = 'LandingPageTemplate'
  AND NOT EXISTS (
      SELECT 1 FROM template_slots ts
      WHERE ts.template_id = pt.id AND ts.slot_name = 'Section2'
  );

-- ── Step 3: Add Section3 (BOTTOM) ────────────────────────────────────────────
INSERT INTO template_slots (uuid, uid, template_id, slot_name, position, sort_order, is_required)
SELECT 'dd000003-0000-4000-8000-000000000003', 'LandingPageSection3Slot', pt.id, 'Section3', 'BOTTOM', 3, FALSE
FROM page_templates pt
WHERE pt.uid = 'LandingPageTemplate'
  AND NOT EXISTS (
      SELECT 1 FROM template_slots ts
      WHERE ts.template_id = pt.id AND ts.slot_name = 'Section3'
  );

-- ── Step 4: Migrate page_slots for existing landing pages: Content → Section1 ─
UPDATE page_slots ps
JOIN pages p          ON p.id  = ps.page_id
JOIN page_templates pt ON pt.id = p.template_id
SET ps.slot_name  = 'Section1',
    ps.position   = 'TOP',
    ps.sort_order = 1,
    ps.uid        = REPLACE(ps.uid, 'ContentSlot', 'Section1Slot')
WHERE pt.uid        = 'LandingPageTemplate'
  AND ps.slot_name  = 'Content';
