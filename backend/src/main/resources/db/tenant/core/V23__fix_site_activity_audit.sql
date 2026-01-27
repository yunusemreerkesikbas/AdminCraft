-- V23: Fix site_activity table missing audit columns
-- This migration ensures created_by and updated_by columns exist in site_activity
-- Required because BaseEntity expects these columns, but they might be missing in some environments

DROP PROCEDURE IF EXISTS upgrade_site_activity_v23;

DELIMITER //

CREATE PROCEDURE upgrade_site_activity_v23()
BEGIN
    -- Check and add created_by column
    IF NOT EXISTS(
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'site_activity'
        AND COLUMN_NAME = 'created_by'
    ) THEN
        ALTER TABLE site_activity ADD COLUMN created_by BIGINT NULL;
    END IF;

    -- Check and add updated_by column
    IF NOT EXISTS(
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'site_activity'
        AND COLUMN_NAME = 'updated_by'
    ) THEN
        ALTER TABLE site_activity ADD COLUMN updated_by BIGINT NULL;
    END IF;
END //

DELIMITER ;

CALL upgrade_site_activity_v23();
DROP PROCEDURE upgrade_site_activity_v23;
