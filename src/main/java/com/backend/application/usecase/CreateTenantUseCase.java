package com.backend.application.usecase;

import com.backend.application.service.TenantService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.CreateTenantRequest;
import com.backend.presentation.dto.response.TenantResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateTenantUseCase {

    @Autowired
    private TenantService tenantService;

    public TenantResponse execute(CreateTenantRequest request, Language displayLanguage) {
        validateRequest(request);
        return tenantService.createTenant(request, displayLanguage);
    }

    private void validateRequest(CreateTenantRequest request) {
        if (request.subdomain() == null || request.subdomain().trim().isEmpty()) {
            throw new IllegalArgumentException("Subdomain is required");
        }
        
        if (request.companyName() == null || request.companyName().trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required");
        }
        
        if (request.adminEmail() == null || request.adminEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Admin email is required");
        }
        
        if (request.adminName() == null || request.adminName().trim().isEmpty()) {
            throw new IllegalArgumentException("Admin name is required");
        }
        
        if (!tenantService.isSubdomainAvailable(request.subdomain())) {
            throw new IllegalArgumentException("Subdomain is already taken: " + request.subdomain());
        }
        
        // Validate subdomain format
        if (!request.subdomain().matches("^[a-z0-9]+(-[a-z0-9]+)*$")) {
            throw new IllegalArgumentException("Invalid subdomain format. Use only lowercase letters, numbers, and hyphens.");
        }
        
        // Ensure default language is in supported languages
        if (!request.supportedLanguages().contains(request.defaultLanguage())) {
            throw new IllegalArgumentException("Default language must be included in supported languages");
        }
    }
}