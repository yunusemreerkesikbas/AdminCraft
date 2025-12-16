package com.backend.application.dto.request;

import java.util.Set;

import com.backend.domain.enums.Language;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TenantLanguagesUpdateRequest(
    @NotNull(message = "validation.default.language.required") Language defaultLanguage,
    @NotEmpty(message = "validation.supported.languages.required") Set<Language> supportedLanguages) {

  public boolean isValid() {
    return supportedLanguages != null &&
        defaultLanguage != null &&
        supportedLanguages.contains(defaultLanguage);
  }

  public String getValidationErrorMessageKey() {
    if (supportedLanguages == null || supportedLanguages.isEmpty()) {
      return "tenant.languages.supported.empty";
    }
    if (defaultLanguage == null) {
      return "tenant.languages.default.required";
    }
    if (!supportedLanguages.contains(defaultLanguage)) {
      return "tenant.languages.default.not.in.supported";
    }
    return null;
  }
}

