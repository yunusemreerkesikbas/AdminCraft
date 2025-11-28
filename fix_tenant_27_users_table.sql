-- Fix for Tenant 27: Add missing columns to users table
-- Issue: Unknown column 'created_by' in 'field list'
-- Date: 2025-11-16
--
-- This script adds the missing audit columns that exist in User.java entity
-- but were not present in the initial V1 migration for this tenant.

USE ac_tenant_27;

-- Add missing audit columns
ALTER TABLE users
    ADD COLUMN created_by BIGINT NULL
        COMMENT 'User ID who created this record',
    ADD COLUMN updated_by BIGINT NULL
        COMMENT 'User ID who last updated this record';

-- Add missing notes column (also from V2 migration)
ALTER TABLE users
    ADD COLUMN notes VARCHAR(500) NULL
        COMMENT 'Additional notes about the user';

-- Verify the columns were added
SHOW COLUMNS FROM users LIKE '%_by';
SHOW COLUMNS FROM users LIKE 'notes';

-- Check table structure
DESCRIBE users;
