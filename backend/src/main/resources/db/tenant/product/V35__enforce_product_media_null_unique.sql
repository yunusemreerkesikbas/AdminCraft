-- Enforce uniqueness for (product_id, responsive_media_set_id) when responsive_media_set_id is NULL.
-- MySQL UNIQUE allows multiple NULLs; this trigger prevents duplicate (product_id, NULL) rows.

DROP TRIGGER IF EXISTS trg_product_media_prevent_dup_null;
DELIMITER //
CREATE TRIGGER trg_product_media_prevent_dup_null
BEFORE INSERT ON product_media
FOR EACH ROW
BEGIN
    IF NEW.responsive_media_set_id IS NULL THEN
        IF (SELECT COUNT(*) FROM product_media pm
            WHERE pm.product_id = NEW.product_id AND pm.responsive_media_set_id IS NULL) > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Duplicate (product_id, responsive_media_set_id=NULL) not allowed in product_media';
        END IF;
    END IF;
END //
DELIMITER ;
