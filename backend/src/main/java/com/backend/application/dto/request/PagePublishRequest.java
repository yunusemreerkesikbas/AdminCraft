package com.backend.application.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record PagePublishRequest(
    @NotNull(message = "validation.tenant.id.required") Long tenantId,
    LocalDateTime scheduledAt) {

  public boolean isImmediatePublish() {
    return scheduledAt == null;
  }

  public boolean isScheduledPublish() {
    return scheduledAt != null;
  }
}

