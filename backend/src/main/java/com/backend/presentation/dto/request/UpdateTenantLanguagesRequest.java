package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

public record UpdateTenantLanguagesRequest(
    @NotBlank String defaultLanguage,
    @NotNull @NotEmpty List<String> supported) {
  public UpdateTenantLanguagesRequest {
    Objects.requireNonNull(defaultLanguage, "defaultLanguage is required");
    Objects.requireNonNull(supported, "supported is required");
  }
}
