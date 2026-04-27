package com.backend.domain.entity;

import java.time.LocalDateTime;

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
public class ImpExAudit {

    private Long id;
    private LocalDateTime executedAt;
    private Long actorUserId;
    private String actorEmail;
    private String actorRole;
    private Long tenantId;
    private String tenantDb;
    private String fullSql;
    private Integer statementCount;
    private Integer successCount;
    private Integer failedCount;
    private String status;
    private String correlationId;
    private String clientIp;
    private Integer durationMs;
}
