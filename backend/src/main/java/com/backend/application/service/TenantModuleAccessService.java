package com.backend.application.service;

import org.springframework.stereotype.Service;

import com.backend.domain.enums.ModuleCode;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantModuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantModuleAccessService {

    private static final String MODULE_STATUS_ENABLED = "enabled";

    private final TenantContextPort tenantContext;
    private final TenantModuleRepository tenantModuleRepository;

    public boolean isEnabledForCurrentTenant(ModuleCode moduleCode) {
        if (moduleCode == null) {
            return false;
        }

        Long tenantId = resolveCurrentTenantId();
        if (tenantId == null) {
            return false;
        }

        return tenantModuleRepository.existsByTenantIdAndModuleCodeAndStatus(
                tenantId,
                moduleCode.getCode(),
                MODULE_STATUS_ENABLED);
    }

    public void assertEnabledForCurrentTenant(
            ModuleCode moduleCode,
            String tenantContextRequiredMessageKey,
            String moduleNotEnabledMessageKey) {
        if (moduleCode == null) {
            throw new IllegalArgumentException("Module code is required");
        }

        Long tenantId = resolveCurrentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException(tenantContextRequiredMessageKey);
        }

        if (!tenantModuleRepository.existsByTenantIdAndModuleCodeAndStatus(
                tenantId,
                moduleCode.getCode(),
                MODULE_STATUS_ENABLED)) {
            throw new IllegalStateException(moduleNotEnabledMessageKey);
        }
    }

    private Long resolveCurrentTenantId() {
        String tenantIdRaw = tenantContext.getTenantId();
        if (tenantIdRaw == null || tenantIdRaw.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(tenantIdRaw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
