package com.backend.application.command;

import com.backend.domain.enums.Language;

import java.util.Set;

public record UpdateTenantCommand(
    String companyName,
    Language defaultLanguage,
    Set<Language> supportedLanguages,
    String customDomain,
    String notes) {
}
