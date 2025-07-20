package com.backend.application.usecase;

import com.backend.application.service.TenantService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.response.TenantResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivateTenantUseCase {

    @Autowired
    private TenantService tenantService;

    public TenantResponse execute(Long tenantId, Language displayLanguage) {
        validateTenantId(tenantId);
        
        // Get current tenant to validate state
        TenantResponse currentTenant = tenantService.getTenantById(tenantId, displayLanguage);
        validateTenantCanBeActivated(currentTenant);
        
        return tenantService.activateTenant(tenantId, displayLanguage);
    }

    private void validateTenantId(Long tenantId) {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Invalid tenant ID");
        }
    }

    private void validateTenantCanBeActivated(TenantResponse tenant) {
        switch (tenant.status()) {
            case ACTIVE:
                throw new IllegalStateException("Tenant is already active");
            case SUSPENDED:
                throw new IllegalStateException("Cannot activate a suspended tenant. Please contact support.");
            case MAINTENANCE:
                throw new IllegalStateException("Cannot activate a tenant in maintenance mode");
            case PENDING:
                // This is the only valid state for activation
                break;
            default:
                throw new IllegalStateException("Unknown tenant status: " + tenant.status());
        }
        
        // Additional business validations can be added here
        if (tenant.adminEmail() == null || tenant.adminEmail().trim().isEmpty()) {
            throw new IllegalStateException("Tenant must have a valid admin email before activation");
        }
        
        if (tenant.companyName() == null || tenant.companyName().trim().isEmpty()) {
            throw new IllegalStateException("Tenant must have a valid company name before activation");
        }
    }
}