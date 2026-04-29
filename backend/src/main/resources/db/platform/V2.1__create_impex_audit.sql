CREATE TABLE impex_audit (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    executed_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_user_id   BIGINT       NULL,
    actor_email     VARCHAR(255) NULL,
    actor_role      VARCHAR(50)  NULL,
    tenant_id       BIGINT       NULL,
    tenant_db       VARCHAR(64)  NULL,
    full_sql        LONGTEXT     NOT NULL,
    statement_count INT          NOT NULL DEFAULT 0,
    success_count   INT          NOT NULL DEFAULT 0,
    failed_count    INT          NOT NULL DEFAULT 0,
    status          VARCHAR(16)  NOT NULL,
    correlation_id  VARCHAR(100) NULL,
    client_ip       VARCHAR(45)  NULL,
    duration_ms     INT          NULL,
    CONSTRAINT pk_impex_audit PRIMARY KEY (id),
    INDEX idx_impex_audit_actor (actor_user_id, executed_at),
    INDEX idx_impex_audit_tenant (tenant_id, executed_at),
    INDEX idx_impex_audit_status (status, executed_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
