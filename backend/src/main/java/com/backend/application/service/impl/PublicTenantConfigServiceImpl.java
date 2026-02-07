package com.backend.application.service.impl;

import com.backend.application.dto.PublicTenantConfigResult;
import com.backend.application.service.PublicTenantConfigService;
import com.backend.domain.entity.Site;
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

    @Override
    @Transactional(readOnly = true)
    public PublicTenantConfigResult getPublicConfig() {
        var siteOpt = siteRepository.findFirstByOrderByIdAsc();

        if (siteOpt.isEmpty()) {
            log.debug("No site found for current tenant, returning disabled config");
            return PublicTenantConfigResult.disabled();
        }

        var site = siteOpt.get();
        
        return PublicTenantConfigResult.of(
            site.getRecaptchaEnabled() != null ? site.getRecaptchaEnabled() : false,
            site.getRecaptchaSiteKey(),
            site.getRecaptchaThreshold()
        );
    }
}
