-- Backfill missing page_slots from template_slots and align existing slot coordinates.
-- Hybris-style contract: template defines slot structure (slotName + position + sortOrder).

-- 1) Create missing page-specific slots for all pages that have a template.
INSERT INTO page_slots (uuid, uid, page_id, slot_name, position, sort_order, is_active, is_shared, created_at, updated_at)
SELECT
    UUID(),
    CONCAT('ps-', p.id, '-', ts.id),
    p.id,
    ts.slot_name,
    ts.position,
    ts.sort_order,
    TRUE,
    FALSE,
    NOW(),
    NOW()
FROM pages p
JOIN template_slots ts ON ts.template_id = p.template_id
LEFT JOIN page_slots ps ON ps.page_id = p.id AND ps.slot_name = ts.slot_name
WHERE p.template_id IS NOT NULL
  AND ps.id IS NULL;

-- 2) Align only the newly backfilled slots (Step 1) to template contract.
-- Restricts to rows created by Step 1 (uid format: 'ps-{pageId}-{templateSlotId}') to avoid overwriting user-customized slot coordinates.
UPDATE page_slots ps
JOIN pages p ON p.id = ps.page_id
JOIN template_slots ts ON ts.template_id = p.template_id AND ts.slot_name = ps.slot_name
SET
    ps.position = ts.position,
    ps.sort_order = ts.sort_order,
    ps.is_shared = FALSE,
    ps.updated_at = NOW()
WHERE ps.uid LIKE 'ps-%';
