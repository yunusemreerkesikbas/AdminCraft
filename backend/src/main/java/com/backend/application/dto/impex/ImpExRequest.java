package com.backend.application.dto.impex;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Deprecated
public record ImpExRequest(
        @NotBlank @Size(max = 200_000) String sqlContent) {
}
