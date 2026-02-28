package com.backend.application.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.SiteTechnicalPatchRequest;
import com.backend.application.dto.response.SiteTechnicalAppDto;
import com.backend.application.dto.response.SiteTechnicalAppDto.CookieConsentAppDto;
import com.backend.application.dto.response.SiteTechnicalAppDto.SearchEngineAppDto;
import com.backend.application.dto.response.SiteTechnicalAppDto.VerificationAppDto;
import com.backend.domain.entity.Site;
import com.backend.domain.entity.SiteTechnicalSettings;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.SiteTechnicalSettingsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of SiteTechnicalService.
 * Manages technical settings for sites including robots.txt and verification codes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SiteTechnicalServiceImpl implements SiteTechnicalService {

    private final SiteRepository siteRepository;
    private final SiteTechnicalSettingsRepository technicalSettingsRepository;

    @Override
    public SiteTechnicalAppDto getTechnicalSettings() {
        log.debug("Getting technical settings");

        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);

        return buildResponse(site, settings);
    }

    @Override
    public SiteTechnicalAppDto patchTechnicalSettings(SiteTechnicalPatchRequest request) {
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
            settings.setGoogleVerification(
                    request.googleVerification().isBlank() ? null : request.googleVerification());
        }
        if (request.bingVerification() != null) {
            settings.setBingVerification(request.bingVerification().isBlank() ? null : request.bingVerification());
        }
        if (request.yandexVerification() != null) {
            settings.setYandexVerification(
                    request.yandexVerification().isBlank() ? null : request.yandexVerification());
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
    public String getRobotsTxt() {
        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);
        return settings.getEffectiveRobotsTxt();
    }

    @Override
    public boolean isSitemapEnabled() {
        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);
        return Boolean.TRUE.equals(settings.getSitemapEnabled());
    }

    @Override
    public boolean isIndexingEnabled() {
        Site site = getFirstSite();
        SiteTechnicalSettings settings = getOrCreateSettings(site);
        return Boolean.TRUE.equals(settings.getIndexingEnabled());
    }

    private synchronized Site getFirstSite() {
        List<Site> sites = siteRepository.findAll();
        if (sites.isEmpty()) {
            log.warn("No site found for current tenant. Creating default site.");
            return createDefaultSite();
        }
        return sites.get(0);
    }

    private synchronized Site createDefaultSite() {
        List<Site> sites = siteRepository.findAll();
        if (!sites.isEmpty()) {
            return sites.get(0);
        }

        Site site = new Site();
        site.setSiteName("Default Site");
        site.setDomain("localhost"); // Default subdomain
        site.setDefaultLanguage(Language.EN);
        site.setEnabledLanguages(Collections.singleton(Language.EN));
        site.setPublished(true);
        return siteRepository.save(site);
    }

    private SiteTechnicalSettings getOrCreateSettings(Site site) {
        return technicalSettingsRepository.findBySiteId(site.getId())
                .orElseGet(() -> createDefaultSettings(site));
    }

    private SiteTechnicalSettings createDefaultSettings(Site site) {
        // Use siteId directly - the site relationship is read-only
        SiteTechnicalSettings settings = SiteTechnicalSettings.builder()
                .siteId(site.getId())
                .robotsTxt(SiteTechnicalSettings.getDefaultRobotsTxt())
                .sitemapEnabled(true)
                .indexingEnabled(true)
                .cookieConsentEnabled(false)
                .build();

        return technicalSettingsRepository.save(settings);
    }

    private SiteTechnicalAppDto buildResponse(Site site, SiteTechnicalSettings settings) {
        // Search engine info
        VerificationAppDto verificationDto = new VerificationAppDto(
                settings.getGoogleVerification(),
                settings.getBingVerification(),
                settings.getYandexVerification());

        SearchEngineAppDto searchEngineDto = new SearchEngineAppDto(
                settings.getRobotsTxt(),
                settings.getSitemapEnabled(),
                settings.getIndexingEnabled(),
                verificationDto);

        CookieConsentAppDto cookieConsentDto = new CookieConsentAppDto(
                settings.getCookieConsentEnabled(),
                settings.getCookieConsentText());

        return new SiteTechnicalAppDto(
                searchEngineDto,
                cookieConsentDto);
    }
}