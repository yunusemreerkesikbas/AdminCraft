package com.backend.application.dto.request;

import java.time.LocalDateTime;

public record PagePublishRequest(LocalDateTime scheduledAt) {

  public boolean isImmediatePublish() {
    return scheduledAt == null;
  }

  public boolean isScheduledPublish() {
    return scheduledAt != null;
  }
}
