package com.backend.application.dto.impex;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImpExRequest(
    @NotBlank
    @Size(max = 100_000)
    String sqlContent
) {}
