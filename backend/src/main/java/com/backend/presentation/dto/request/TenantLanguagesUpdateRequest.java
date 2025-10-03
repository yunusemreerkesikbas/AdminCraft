package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record TenantLanguagesUpdateRequest(

        @NotNull(message = "validation.default.language.required") Language defaultLanguage,

        @NotEmpty(message = "validation.supported.languages.required") Set<Language> supportedLanguages) {
    public boolean isValid() {
        return supportedLanguages != null &&
                defaultLanguage != null &&
                supportedLanguages.contains(defaultLanguage);
    }

    public String getValidationErrorMessage() {
        if (supportedLanguages == null || supportedLanguages.isEmpty()) {
            return "Supported languages cannot be empty";
        }
        if (defaultLanguage == null) {
            return "Default language must be specified";
        }
        if (!supportedLanguages.contains(defaultLanguage)) {
            return "Default language must be in supported languages list";
        }
        return null;
    }
}
