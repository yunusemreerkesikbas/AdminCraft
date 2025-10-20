package com.backend.application.command;

import com.backend.domain.enums.Language;

import java.util.Set;

public record CreateTenantCommand(
    String subdomain,
    String companyName,
    String adminEmail,
    String adminName,
    String phone,
    Language defaultLanguage,
    Set<Language> supportedLanguages,
    String timezone,
    String currency,
    String notes) {
}
