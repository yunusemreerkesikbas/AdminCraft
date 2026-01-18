package com.backend.application.usecase;

import com.backend.application.dto.request.CreateTenantRequest;
import com.backend.application.service.TenantService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.response.TenantDetailResponse;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateTenantUseCase {

    private final TenantService tenantService;

    public TenantDetailResponse execute(CreateTenantRequest request, Language displayLanguage) {
        validate(request);
        return tenantService.createTenantWithDetail(request, displayLanguage);
    }

    private void validate(CreateTenantRequest request) {
        if (request.subdomain() == null || request.subdomain().trim().isEmpty()) {
            throw new IllegalArgumentException("validation.subdomain.required");
        }

        if (request.companyName() == null || request.companyName().trim().isEmpty()) {
            throw new IllegalArgumentException("validation.company.name.required");
        }

        if (!request.subdomain().matches("^[a-z0-9]+(-[a-z0-9]+)*$")) {
            throw new IllegalArgumentException("validation.subdomain.pattern");
        }

        if (request.supportedLanguages() == null || !request.supportedLanguages().contains(request.defaultLanguage())) {
            throw new IllegalArgumentException("Default language must be included in supported languages");
        }
    }
}