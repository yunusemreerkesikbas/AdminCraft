package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBlockRequest(
    @NotNull Long id,
    @Size(max = 50) String type,
    Integer displayOrder,
    String data) {
}
