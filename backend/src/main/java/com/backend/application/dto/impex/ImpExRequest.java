package com.backend.application.dto.impex;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ImpExRequest(
    @NotNull
    @NotBlank
    @Size(max = 100_000)
    String sqlContent
) {}
