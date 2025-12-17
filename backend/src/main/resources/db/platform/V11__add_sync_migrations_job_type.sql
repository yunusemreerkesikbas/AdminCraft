-- Add 'sync-migrations' to provisioning_jobs type ENUM
ALTER TABLE provisioning_jobs 
MODIFY COLUMN type ENUM('create-db', 'add-modules', 'migrate', 'full-provision', 'sync-migrations') DEFAULT 'full-provision';
