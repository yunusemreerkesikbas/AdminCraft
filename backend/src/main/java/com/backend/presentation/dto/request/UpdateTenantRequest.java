package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.constraints.*;

import java.util.Set;

public record UpdateTenantRequest(
        @Size(max = 100, message = "validation.company.name.size") String companyName,

        Language defaultLanguage,

        Set<Language> supportedLanguages,

        @Size(max = 100, message = "validation.custom.domain.size") String customDomain,

        @Size(max = 1000, message = "validation.notes.size") String notes) {
    public UpdateTenantRequest {
        if (companyName != null) {
            companyName = companyName.trim();
        }
        if (customDomain != null) {
            customDomain = customDomain.toLowerCase().trim();
        }
        if (notes != null) {
            notes = notes.trim();
        }
    }
}