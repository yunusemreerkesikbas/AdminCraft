package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record LanguageProvisioningRequest(

        @NotEmpty(message = "validation.languages.required") Set<Language> languages) {
}
