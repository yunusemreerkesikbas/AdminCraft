package com.backend.application.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.SecuritySettingsResult;
import com.backend.application.dto.UpdateSecuritySettingsCommand;
import com.backend.application.service.SecuritySettingsService;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecuritySettingsServiceImpl implements SecuritySettingsService {

    private final TenantRepository tenantRepository;
    private final TenantContextPort tenantContext;

    @Override
    public SecuritySettingsResult getSecuritySettings() {
        Tenant tenant = getCurrentTenant();
        TwoFactorPolicy policy = tenant.getTwoFactorPolicy() != null
                ? tenant.getTwoFactorPolicy()
                : TwoFactorPolicy.DISABLED;
        return SecuritySettingsResult.of(policy, getPolicyDescription(policy));
    }

    @Override
    @Transactional
    public SecuritySettingsResult updateTwoFactorPolicy(TwoFactorPolicy policy) {
        Tenant tenant = getCurrentTenant();
        log.info("Updating 2FA policy for tenant {} from {} to {}",
                tenant.getId(), tenant.getTwoFactorPolicy(), policy);
        tenant.setTwoFactorPolicy(policy);
        tenantRepository.save(tenant);
        return SecuritySettingsResult.of(policy, getPolicyDescription(policy));
    }

    @Override
    @Transactional
    public SecuritySettingsResult updateSecuritySettings(UpdateSecuritySettingsCommand command) {
        Tenant tenant = getCurrentTenant();
        if (command.twoFactorPolicy() != null) {
            log.info("Updating 2FA policy for tenant {} to {}", tenant.getId(), command.twoFactorPolicy());
            tenant.setTwoFactorPolicy(command.twoFactorPolicy());
            tenantRepository.save(tenant);
        }
        TwoFactorPolicy policy = tenant.getTwoFactorPolicy() != null
                ? tenant.getTwoFactorPolicy()
                : TwoFactorPolicy.DISABLED;
        return SecuritySettingsResult.of(policy, getPolicyDescription(policy));
    }

    private Tenant getCurrentTenant() {
        String tenantIdStr = tenantContext.getTenantId();
        if (tenantIdStr == null) {
            throw new TenantNotFoundException("No tenant context available");
        }
        try {
            Long tenantId = Long.parseLong(tenantIdStr);
            return tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new TenantNotFoundException(tenantId));
        } catch (NumberFormatException ex) {
            throw new TenantNotFoundException("Invalid tenant id in context: " + tenantIdStr);
        }
    }

    private String getPolicyDescription(TwoFactorPolicy policy) {
        return switch (policy) {
            case DISABLED -> "Two-factor authentication is disabled for all users";
            case REQUIRED -> "Two-factor authentication is required for all users";
        };
    }
}
