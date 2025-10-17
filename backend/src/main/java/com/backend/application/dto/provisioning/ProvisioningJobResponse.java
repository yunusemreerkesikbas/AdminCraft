package com.backend.application.dto.provisioning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisioningJobResponse {

  private Long jobId;
  private Long tenantId;
  private String type;
  private String status;
  private Integer progress;
  private String error;
  private LocalDateTime createdAt;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
}

