-- Speed admin lookups by correlation id + execution time
CREATE INDEX idx_impex_audit_correlation ON impex_audit (correlation_id, executed_at);
