package com.backend.presentation.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BulkDeleteRequest(
        @NotEmpty @Size(max = 100) List<@NotNull Long> ids) {
}
