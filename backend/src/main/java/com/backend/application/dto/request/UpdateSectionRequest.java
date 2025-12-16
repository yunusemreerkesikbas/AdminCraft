package com.backend.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSectionRequest(
    @NotNull Long id,
    @Size(max = 50) String type,
    Integer displayOrder,
    String data) {
}
