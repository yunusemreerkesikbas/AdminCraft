package com.backend.application.commerce;

import org.springframework.stereotype.Service;

import com.backend.application.service.TenantModuleAccessService;
import com.backend.domain.enums.ModuleCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommerceModuleAccessGuard {

    static final String TENANT_CONTEXT_REQUIRED_MESSAGE_KEY = "commerce.tenant.context.required";
    static final String MODULE_NOT_ENABLED_MESSAGE_KEY = "commerce.module.not.enabled";

    private final TenantModuleAccessService tenantModuleAccessService;

    public boolean isEnabledForCurrentTenant() {
        return tenantModuleAccessService.isEnabledForCurrentTenant(ModuleCode.COMMERCE);
    }

    public void assertEnabledForCurrentTenant() {
        tenantModuleAccessService.assertEnabledForCurrentTenant(
                ModuleCode.COMMERCE,
                TENANT_CONTEXT_REQUIRED_MESSAGE_KEY,
                MODULE_NOT_ENABLED_MESSAGE_KEY);
    }
}
