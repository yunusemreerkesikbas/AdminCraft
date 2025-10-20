package com.backend.application.command;

import com.backend.domain.enums.Language;

import java.util.Set;

public record UpdateTenantCommand(
    String companyName,
    String adminEmail,
    String adminName,
    String phone,
    Language defaultLanguage,
    Set<Language> supportedLanguages,
    String customDomain,
    Boolean sslEnabled,
    String timezone,
    String currency,
    String notes) {
}
