package com.backend.application.service.impl;

import java.math.BigDecimal;

import com.backend.application.dto.PublicTenantConfigResult;
import com.backend.application.service.PublicTenantConfigService;
import com.backend.domain.port.PlatformSettingsPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicTenantConfigServiceImpl implements PublicTenantConfigService {

    private final SiteRepository siteRepository;
    private final PlatformSettingsPort platformSettings;
    private final TenantContextPort tenantContext;

    @Override
    @Transactional(readOnly = true)
    public PublicTenantConfigResult getPublicConfig() {
        if (!tenantContext.isSet()) {
            var settings = platformSettings.getSingleton();
            return PublicTenantConfigResult.of(
                    settings.getRecaptchaEnabled() != null ? settings.getRecaptchaEnabled() : false,
                    settings.getRecaptchaSiteKey(),
                    settings.getRecaptchaThreshold() != null
                            ? settings.getRecaptchaThreshold()
                            : new BigDecimal("0.5"));
        }

        var siteOpt = siteRepository.findFirstByOrderByIdAsc();

        if (siteOpt.isEmpty()) {
            log.debug("No site found for current tenant, returning disabled config");
            return PublicTenantConfigResult.disabled();
        }

        var site = siteOpt.get();
        
        return PublicTenantConfigResult.of(
            site.getRecaptchaEnabled() != null ? site.getRecaptchaEnabled() : false,
            site.getRecaptchaSiteKey(),
            site.getRecaptchaThreshold() != null ? site.getRecaptchaThreshold() : new BigDecimal("0.5")
        );
    }
}
