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
public class TenantModule {

    private Long id;
    private Long tenantId;
    private String moduleCode;
    private String status;
    private String targetVersion;
    private LocalDateTime installedAt;
    private LocalDateTime updatedAt;
}
