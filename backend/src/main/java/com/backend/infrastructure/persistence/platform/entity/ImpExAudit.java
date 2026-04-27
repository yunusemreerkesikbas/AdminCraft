package com.backend.infrastructure.persistence.platform.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        schema = "platform_management",
        name = "impex_audit",
        indexes = @Index(name = "idx_impex_audit_correlation", columnList = "correlation_id,executed_at"))
public class ImpExAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_email", length = 255)
    private String actorEmail;

    @Column(name = "actor_role", length = 50)
    private String actorRole;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "tenant_db", length = 64)
    private String tenantDb;

    @Column(name = "full_sql", nullable = false, columnDefinition = "LONGTEXT")
    private String fullSql;

    @Column(name = "statement_count", nullable = false)
    private Integer statementCount;

    @Column(name = "success_count", nullable = false)
    private Integer successCount;

    @Column(name = "failed_count", nullable = false)
    private Integer failedCount;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @PrePersist
    protected void onCreate() {
        if (executedAt == null) {
            executedAt = LocalDateTime.now();
        }
    }
}
