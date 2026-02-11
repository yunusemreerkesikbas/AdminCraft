-- V44: Optimize platform verification token index for lookup performance
--
-- Current: Separate index on token_hash (uk_platform_verification_token_hash)
-- Problem: Most queries filter by both token_hash AND status
-- Solution: Composite index (token_hash, status) enables index-only scans
--
-- Typical query pattern:
--   SELECT * FROM platform_verification_tokens 
--   WHERE token_hash = ? AND status = 'ACTIVE'
--
-- Performance benefit:
--   - Covering index (no table lookup needed)
--   - Faster WHERE clause evaluation
--   - Minimal overhead (status is small ENUM)
--
-- Author: Code Review Optimization
-- Date: 2026-02-10
-- Related: Suggestion #16

-- Create composite index
CREATE INDEX idx_platform_token_hash_status 
ON platform_verification_tokens(token_hash, status);

-- Drop old single-column index
-- Note: Keep unique constraint on entity definition (@Index name="uk_...")
-- This migration only optimizes query performance
DROP INDEX uk_platform_verification_token_hash ON platform_verification_tokens;

-- Re-create as unique index (maintain uniqueness constraint)
CREATE UNIQUE INDEX uk_platform_verification_token_hash 
ON platform_verification_tokens(token_hash);
