package com.backend.application.usecase;

import com.backend.application.command.CreateTenantCommand;
import com.backend.application.service.TenantService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.response.TenantDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateTenantUseCase {

    @Autowired
    private TenantService tenantService;

    public TenantDetailResponse execute(CreateTenantCommand command, Language displayLanguage) {
        validate(command);
        return tenantService.createTenantWithDetail(command, displayLanguage);
    }

    private void validate(CreateTenantCommand command) {
        if (command.subdomain() == null || command.subdomain().trim().isEmpty()) {
            throw new IllegalArgumentException("validation.subdomain.required");
        }

        if (command.companyName() == null || command.companyName().trim().isEmpty()) {
            throw new IllegalArgumentException("validation.company.name.required");
        }

        if (!command.subdomain().matches("^[a-z0-9]+(-[a-z0-9]+)*$")) {
            throw new IllegalArgumentException("validation.subdomain.pattern");
        }

        if (command.supportedLanguages() == null || !command.supportedLanguages().contains(command.defaultLanguage())) {
            throw new IllegalArgumentException("Default language must be included in supported languages");
        }
    }
}