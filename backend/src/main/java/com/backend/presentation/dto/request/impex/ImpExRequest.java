package com.backend.presentation.dto.request.impex;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImpExRequest(
        @NotBlank @Size(max = 200_000) String sqlContent) {
}
