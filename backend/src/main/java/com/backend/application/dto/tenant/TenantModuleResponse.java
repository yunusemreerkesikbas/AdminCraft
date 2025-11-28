package com.backend.application.dto.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantModuleResponse {

  private Long id;
  private String moduleCode;
  private String moduleName;
  private String status;
  private String targetVersion;
  private LocalDateTime installedAt;
}
