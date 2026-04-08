package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.SiteTechnicalPatchRequest;
import com.backend.application.dto.response.SiteTechnicalAppDto;
import com.backend.application.dto.response.SiteTechnicalAppDto.CookieConsentAppDto;
import com.backend.application.dto.response.SiteTechnicalAppDto.SearchEngineAppDto;
import com.backend.domain.entity.Site;
import com.backend.domain.entity.SiteSetting;
import com.backend.domain.entity.SiteTechnicalSettings;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.SettingType;
import com.backend.domain.constants.SiteSettingKeys;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.SiteSettingRepository;
import com.backend.domain.repository.SiteTechnicalSettingsRepository;
import com.backend.shared.constants.ValidationConstants;

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
    private final SiteSettingRepository siteSettingRepository;
    private final TenantContextPort tenantContext;

    private static final String COOKIE_CONSENT_TEXT_KEY = SiteSettingKeys.COOKIE_CONSENT_TEXT;

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
        if (request.cookieConsentEnabled() != null) {
            settings.setCookieConsentEnabled(request.cookieConsentEnabled());
        }

        SiteTechnicalSettings savedSettings = technicalSettingsRepository.save(settings);

        if (request.cookieConsentTexts() != null) {
            Long tenantId = Long.parseLong(tenantContext.getTenantId());
            for (Map.Entry<String, String> entry : request.cookieConsentTexts().entrySet()) {
                try {
                    Language lang = Language.valueOf(entry.getKey().toUpperCase());
                    upsertCookieConsentText(tenantId, lang, entry.getValue());
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping unknown language code in cookieConsentTexts: {}", entry.getKey());
                }
            }
        }
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

        Language defaultLanguage = tenantContext.getDefaultLanguage();
        java.util.Set<Language> supportedLanguages = tenantContext.getSupportedLanguages();

        Site site = new Site();
        site.setSiteName("Default Site");
        site.setDomain("localhost");
        site.setDefaultLanguage(defaultLanguage);
        site.setEnabledLanguages(supportedLanguages.isEmpty() ? Collections.singleton(defaultLanguage) : supportedLanguages);
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
        SearchEngineAppDto searchEngineDto = new SearchEngineAppDto(
                settings.getRobotsTxt(),
                settings.getSitemapEnabled(),
                settings.getIndexingEnabled());

        Long tenantId = Long.parseLong(tenantContext.getTenantId());
        Map<String, String> texts = siteSettingRepository
                .findByTenantIdAndSettingKeyIn(tenantId, List.of(COOKIE_CONSENT_TEXT_KEY))
                .stream()
                .filter(s -> s.getLanguage() != null)
                .collect(Collectors.toMap(
                        s -> s.getLanguage().name().toLowerCase(),
                        SiteSetting::getSettingValue));

        CookieConsentAppDto cookieConsentDto = new CookieConsentAppDto(
                settings.getCookieConsentEnabled(),
                texts.isEmpty() ? null : texts);

        return new SiteTechnicalAppDto(
                searchEngineDto,
                cookieConsentDto);
    }

    private void upsertCookieConsentText(Long tenantId, Language lang, String value) {
        String normalized = (value == null || value.isBlank()) ? null : value.trim();
        if (normalized != null && normalized.length() > ValidationConstants.COOKIE_CONSENT_TEXT_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Cookie consent text exceeds max length for lang: " + lang.name());
        }
        siteSettingRepository.findByTenantIdAndSettingKeyAndLanguage(tenantId, COOKIE_CONSENT_TEXT_KEY, lang)
                .ifPresentOrElse(
                        s -> {
                            s.updateValue(normalized, null);
                            siteSettingRepository.save(s);
                        },
                        () -> {
                            if (normalized != null) {
                                SiteSetting s = new SiteSetting();
                                s.setSettingKey(COOKIE_CONSENT_TEXT_KEY);
                                s.setSettingValue(normalized);
                                s.setLanguage(lang);
                                s.setSettingType(SettingType.I18N_TEXT);
                                s.setCategory("general");
                                s.setIsPublic(false);
                                s.setSortOrder(0);
                                s.setUpdatedAt(LocalDateTime.now());
                                siteSettingRepository.save(s);
                            }
                        });
    }
}
