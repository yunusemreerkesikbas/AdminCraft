package com.backend.application.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.SecuritySettingsResult;
import com.backend.application.dto.UpdateSecuritySettingsCommand;
import com.backend.application.service.SecuritySettingsService;
import com.backend.domain.entity.Site;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.security.EncryptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecuritySettingsServiceImpl implements SecuritySettingsService {

    private final TenantRepository tenantRepository;
    private final TenantContextPort tenantContext;
    private final SiteRepository siteRepository;
    private final EncryptionService encryptionService;

    @Override
    public SecuritySettingsResult getSecuritySettings() {
        Tenant tenant = getCurrentTenant();
        TwoFactorPolicy policy = tenant.getTwoFactorPolicy();
        if (policy == null) {
            policy = TwoFactorPolicy.DISABLED;
        }

        var siteOpt = siteRepository.findFirstByOrderByIdAsc();
        if (siteOpt.isEmpty()) {
            return SecuritySettingsResult.of(policy, getPolicyDescription(policy));
        }

        var site = siteOpt.get();

        return SecuritySettingsResult.of(
            policy,
            getPolicyDescription(policy),
            site.getRecaptchaEnabled(),
            site.getRecaptchaSiteKey(),
            site.getRecaptchaThreshold() != null ? site.getRecaptchaThreshold() : new BigDecimal("0.5")
        );
    }

    @Override
    @Transactional
    public SecuritySettingsResult updateTwoFactorPolicy(TwoFactorPolicy policy) {
        Tenant tenant = getCurrentTenant();

        log.info("Updating 2FA policy for tenant {} from {} to {}",
                tenant.getId(), tenant.getTwoFactorPolicy(), policy);

        tenant.setTwoFactorPolicy(policy);
        tenantRepository.save(tenant);

        log.info("2FA policy updated successfully for tenant {}", tenant.getId());

        return SecuritySettingsResult.of(policy, getPolicyDescription(policy));
    }

    @Override
    @Transactional
    public SecuritySettingsResult updateSecuritySettings(UpdateSecuritySettingsCommand command) {
        Tenant tenant = getCurrentTenant();

        if (command.twoFactorPolicy() != null) {
            log.info("Updating 2FA policy for tenant {} from {} to {}",
                    tenant.getId(), tenant.getTwoFactorPolicy(), command.twoFactorPolicy());
            tenant.setTwoFactorPolicy(command.twoFactorPolicy());
            tenantRepository.save(tenant);
        }

        var siteOpt = siteRepository.findFirstByOrderByIdAsc();
        if (siteOpt.isEmpty()) {
            log.warn("No site found for tenant {}, skipping reCAPTCHA update", tenant.getId());
            TwoFactorPolicy policy = tenant.getTwoFactorPolicy() != null ?
                    tenant.getTwoFactorPolicy() : TwoFactorPolicy.DISABLED;
            return SecuritySettingsResult.of(policy, getPolicyDescription(policy));
        }

        var site = siteOpt.get();

        if (command.recaptchaEnabled() != null) {
            site.setRecaptchaEnabled(command.recaptchaEnabled());
        }
        if (command.recaptchaSiteKey() != null) {
            site.setRecaptchaSiteKey(command.recaptchaSiteKey());
        }
        if (command.recaptchaSecretKey() != null && !command.recaptchaSecretKey().isBlank()) {
            String encryptedSecret = encryptionService.encrypt(command.recaptchaSecretKey());
            site.setRecaptchaSecretKeyEncrypted(encryptedSecret);
        }
        if (command.recaptchaThreshold() != null) {
            site.setRecaptchaThreshold(command.recaptchaThreshold());
        }

        site.setUpdatedAt(LocalDateTime.now());
        siteRepository.save(site);

        log.info("Security settings updated successfully for tenant {}, siteId: {}", tenant.getId(), site.getId());

        TwoFactorPolicy policy = tenant.getTwoFactorPolicy() != null ?
                tenant.getTwoFactorPolicy() : TwoFactorPolicy.DISABLED;

        return SecuritySettingsResult.of(
                policy,
                getPolicyDescription(policy),
                site.getRecaptchaEnabled(),
                site.getRecaptchaSiteKey(),
                site.getRecaptchaThreshold() != null ? site.getRecaptchaThreshold() : new BigDecimal("0.5")
        );
    }

    private Tenant getCurrentTenant() {
        String tenantIdStr = tenantContext.getTenantId();
        if (tenantIdStr == null) {
            throw new TenantNotFoundException("No tenant context available");
        }

        Long tenantId;
        try {
            tenantId = Long.parseLong(tenantIdStr);
        } catch (NumberFormatException ex) {
            log.warn("Invalid tenant id in context: {}", tenantIdStr);
            throw new TenantNotFoundException("Invalid tenant id in context: " + tenantIdStr);
        }
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));
    }

    private String getPolicyDescription(TwoFactorPolicy policy) {
        return switch (policy) {
            case DISABLED -> "Two-factor authentication is disabled for all users";
            case REQUIRED -> "Two-factor authentication is required for all users";
        };
    }
}
