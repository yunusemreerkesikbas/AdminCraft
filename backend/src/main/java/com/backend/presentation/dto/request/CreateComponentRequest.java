package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateComponentRequest(
    @NotNull Long tenantId,
    @NotNull ComponentType type,
    @NotBlank @Size(max = 100) String key,
    ComponentStatus status,
    Boolean visible,
    Integer sortOrder,
    @Size(max = 200) String titleTr,
    @Size(max = 300) String subtitleTr,
    String dataTr,
    @Size(max = 200) String titleEn,
    @Size(max = 300) String subtitleEn,
    String dataEn) {
  public CreateComponentRequest {
    if (visible == null)
      visible = Boolean.TRUE;
    if (sortOrder == null)
      sortOrder = 0;
  }
}
