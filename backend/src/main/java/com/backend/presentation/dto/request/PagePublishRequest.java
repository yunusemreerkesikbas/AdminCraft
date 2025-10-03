package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

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
