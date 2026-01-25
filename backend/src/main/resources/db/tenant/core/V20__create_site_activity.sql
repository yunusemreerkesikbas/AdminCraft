-- V20: Create site_activity table for tracking user actions
-- This table stores activity logs for the Site Dashboard Overview tab

CREATE TABLE site_activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'Global unique identifier',
    uid VARCHAR(50) NOT NULL UNIQUE COMMENT 'User friendly unique identifier',
    action VARCHAR(20) NOT NULL COMMENT 'Activity action type (CREATED, UPDATED, DELETED, etc.)',
    entity_type VARCHAR(20) NOT NULL COMMENT 'Entity type (PAGE, COMPONENT, MEDIA, PRODUCT, SITE, etc.)',
    entity_id BIGINT NULL COMMENT 'ID of the affected entity',
    entity_name VARCHAR(255) NULL COMMENT 'Name/title of the affected entity',
    user_id BIGINT NULL COMMENT 'ID of the user who performed the action',
    user_email VARCHAR(100) NULL COMMENT 'Email of the user',
    user_full_name VARCHAR(100) NULL COMMENT 'Full name of the user',
    description VARCHAR(500) NULL COMMENT 'Optional description of the activity',
    
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,

    INDEX idx_site_activity_uuid (uuid),
    INDEX idx_site_activity_uid (uid),
    INDEX idx_site_activity_created_at (created_at DESC),
    INDEX idx_site_activity_entity_type (entity_type),
    INDEX idx_site_activity_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Stores site activity logs for dashboard overview';
