package com.backend.application.service;

import com.backend.application.dto.request.SiteTechnicalPatchRequest;
import com.backend.domain.entity.Site;
import com.backend.domain.entity.SiteTechnicalSettings;
import com.backend.domain.exception.SiteNotFoundException;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.SiteTechnicalSettingsRepository;
import com.backend.presentation.dto.response.SiteTechnicalResponse;
import com.backend.presentation.dto.response.SiteTechnicalResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of SiteTechnicalService.
 * Manages technical settings for sites including robots.txt, scripts, and verification codes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SiteTechnicalServiceImpl implements SiteTechnicalService {

    private static final String PLATFORM_DOMAIN = "admincraft.com";

    private final SiteRepository siteRepository;
    private final SiteTechnicalSettingsRepository technicalSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public SiteTechnicalResponse getTechnicalSettings() {
        log.debug("Getting technical settings");

        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);

        return buildResponse(site, settings);
    }

    @Override
    public SiteTechnicalResponse patchTechnicalSettings(SiteTechnicalPatchRequest request) {
        log.debug("Patching technical settings");

        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);

        // Apply updates (only non-null fields)
        if (request.robotsTxt() != null) {
            settings.setRobotsTxt(request.robotsTxt());
        }
        if (request.sitemapEnabled() != null) {
            settings.setSitemapEnabled(request.sitemapEnabled());
        }
        if (request.indexingEnabled() != null) {
            settings.setIndexingEnabled(request.indexingEnabled());
        }
        if (request.googleVerification() != null) {
            settings.setGoogleVerification(request.googleVerification().isBlank() ? null : request.googleVerification());
        }
        if (request.bingVerification() != null) {
            settings.setBingVerification(request.bingVerification().isBlank() ? null : request.bingVerification());
        }
        if (request.yandexVerification() != null) {
            settings.setYandexVerification(request.yandexVerification().isBlank() ? null : request.yandexVerification());
        }
        if (request.headScripts() != null) {
            settings.setHeadScripts(request.headScripts().isBlank() ? null : request.headScripts());
        }
        if (request.bodyStartScripts() != null) {
            settings.setBodyStartScripts(request.bodyStartScripts().isBlank() ? null : request.bodyStartScripts());
        }
        if (request.bodyEndScripts() != null) {
            settings.setBodyEndScripts(request.bodyEndScripts().isBlank() ? null : request.bodyEndScripts());
        }
        if (request.cookieConsentEnabled() != null) {
            settings.setCookieConsentEnabled(request.cookieConsentEnabled());
        }
        if (request.cookieConsentText() != null) {
            settings.setCookieConsentText(request.cookieConsentText().isBlank() ? null : request.cookieConsentText());
        }

        SiteTechnicalSettings savedSettings = technicalSettingsRepository.save(settings);
        log.info("Technical settings updated for site: {}", site.getId());

        return buildResponse(site, savedSettings);
    }

    @Override
    @Transactional(readOnly = true)
    public String getRobotsTxt() {
        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);
        return settings.getEffectiveRobotsTxt();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSitemapEnabled() {
        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);
        return Boolean.TRUE.equals(settings.getSitemapEnabled());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isIndexingEnabled() {
        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);
        return Boolean.TRUE.equals(settings.getIndexingEnabled());
    }

    @Override
    @Transactional(readOnly = true)
    public String getHeadScripts() {
        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);
        return settings.getHeadScripts();
    }

    @Override
    @Transactional(readOnly = true)
    public String getBodyEndScripts() {
        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);
        return settings.getBodyEndScripts();
    }

    private Site getFirstSite() {
        List<Site> sites = siteRepository.findAll();
        if (sites.isEmpty()) {
            throw new SiteNotFoundException("No site found for current tenant");
        }
        return sites.get(0);
    }

    private SiteTechnicalSettings getOrCreateSettings(Site site) {
        return technicalSettingsRepository.findBySiteId(site.getId())
                .orElseGet(() -> createDefaultSettings(site));
    }

    private SiteTechnicalSettings createDefaultSettings(Site site) {
        SiteTechnicalSettings settings = SiteTechnicalSettings.builder()
                .siteId(site.getId())
                .site(site)
                .robotsTxt(SiteTechnicalSettings.getDefaultRobotsTxt())
                .sitemapEnabled(true)
                .indexingEnabled(true)
                .cookieConsentEnabled(false)
                .build();

        return technicalSettingsRepository.save(settings);
    }

    private SiteTechnicalResponse buildResponse(Site site, SiteTechnicalSettings settings) {
        // Domain info
        String subdomain = site.getDomain();
        String customDomain = site.getCustomDomain();
        String fullUrl = site.getSiteUrl();

        DomainDto domainDto = new DomainDto(
                subdomain,
                PLATFORM_DOMAIN,
                fullUrl,
                customDomain,
                site.getSslEnabled()
        );

        // Search engine info
        VerificationDto verificationDto = new VerificationDto(
                settings.getGoogleVerification(),
                settings.getBingVerification(),
                settings.getYandexVerification()
        );

        SearchEngineDto searchEngineDto = new SearchEngineDto(
                settings.getRobotsTxt(),
                settings.getSitemapEnabled(),
                settings.getIndexingEnabled(),
                verificationDto
        );

        // Scripts info
        ScriptsDto scriptsDto = new ScriptsDto(
                settings.getHeadScripts(),
                settings.getBodyStartScripts(),
                settings.getBodyEndScripts()
        );

        // Cookie consent info
        CookieConsentDto cookieConsentDto = new CookieConsentDto(
                settings.getCookieConsentEnabled(),
                settings.getCookieConsentText()
        );

        return SiteTechnicalResponse.builder()
                .domain(domainDto)
                .searchEngine(searchEngineDto)
                .scripts(scriptsDto)
                .cookieConsent(cookieConsentDto)
                .build();
    }
}
