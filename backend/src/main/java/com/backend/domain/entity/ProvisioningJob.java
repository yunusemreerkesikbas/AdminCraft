package com.backend.domain.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisioningJob {

    private Long id;
    private Long tenantId;
    private String type;
    private String payload;
    private String status;
    private Integer progress;
    private String error;
    private String correlationId;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
