-- AdminCraft Platform Management Database Initialization
-- Creates only the platform control-plane database
-- Tenant databases are created dynamically by the provisioning service

CREATE DATABASE IF NOT EXISTS platform_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Note: Tenant databases (ac_tenant_1, ac_tenant_2, etc.) are created 
-- programmatically by the application's provisioning service with Flyway migrations
