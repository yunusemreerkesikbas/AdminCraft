package com.backend.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBlockRequest(
        @NotNull Long sectionId,
        @Size(max = 50) String type,
        Integer displayOrder,
        String data) {
}
