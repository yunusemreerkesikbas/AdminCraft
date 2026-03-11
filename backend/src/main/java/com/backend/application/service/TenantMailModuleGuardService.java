package com.backend.application.service;

import org.springframework.stereotype.Service;

import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantModuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantMailModuleGuardService {

    private static final String MAIL_MARKETING = "mail_marketing";
    private final TenantContextPort tenantContext;
    private final TenantModuleRepository tenantModuleRepository;

    public void assertEnabled() {
        String tenantIdRaw = tenantContext.getTenantId();
        if (tenantIdRaw == null || tenantIdRaw.isBlank()) {
            throw new IllegalStateException("mail.marketing.tenant.context.required");
        }
        Long tenantId;
        try {
            tenantId = Long.parseLong(tenantIdRaw);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("mail.marketing.tenant.context.required");
        }
        boolean enabled = tenantModuleRepository.findByTenantId(tenantId).stream()
            .anyMatch(tm -> MAIL_MARKETING.equalsIgnoreCase(tm.getModuleCode())
                && "enabled".equalsIgnoreCase(tm.getStatus()));
        if (!enabled) {
            throw new IllegalStateException("mail.marketing.module.not.enabled");
        }
    }
}
