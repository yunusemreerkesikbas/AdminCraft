package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppGlobalDto;
import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppI18nDto;
import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppResponseDto;
import com.backend.domain.entity.Site;
import com.backend.domain.entity.SiteSetting;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.RobotsMetaTag;
import com.backend.domain.enums.SettingType;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.SiteSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SiteSettingsServiceImpl implements SiteSettingsService {

  private final SiteSettingRepository repository;
  private final TenantLanguageService tenantLanguageService;
  private final SiteRepository siteRepository;

  @Override
  @Transactional(readOnly = true)
  public SiteSettingsAppResponseDto getAdminSettings(Long tenantId) {
    List<SiteSetting> allSettings = repository.findByTenantId(tenantId);
    Site site = siteRepository.findFirstByOrderByIdAsc().orElse(null);
    SiteSettingsAppGlobalDto global = buildGlobalResponse(allSettings, site);

    Map<String, SiteSettingsAppI18nDto> languages = new HashMap<>();
    tenantLanguageService.getLanguages(tenantId).supportedLanguages().forEach(lang -> {
      SiteSettingsAppI18nDto i18nSettings = buildI18nResponse(allSettings, lang);
      languages.put(lang.name().toLowerCase(), i18nSettings);
    });

    return new SiteSettingsAppResponseDto(global, languages);
  }

  @Override
  public SiteSettingsAppResponseDto patchSettings(Long tenantId, SiteSettingsAppGlobalDto global,
      Map<Language, SiteSettingsAppI18nDto> languages, Long updatedBy) {
    List<SiteSetting> settingsToUpdate = new ArrayList<>();

    if (global != null) {
      settingsToUpdate.addAll(processGlobalSettings(tenantId, global, updatedBy));
      persistLogoUids(global);
    }
    if (languages != null) {
      for (Map.Entry<Language, SiteSettingsAppI18nDto> entry : languages.entrySet()) {
        settingsToUpdate.addAll(processI18nSettings(tenantId, entry.getKey(), entry.getValue(), updatedBy));
      }
    }
    if (!settingsToUpdate.isEmpty()) {
      repository.saveAll(settingsToUpdate);
      log.info("Updated {} site settings for tenant {}", settingsToUpdate.size(), tenantId);
    }
    return getAdminSettings(tenantId);
  }

  private void persistLogoUids(SiteSettingsAppGlobalDto global) {
    if (global.logoMediaUid() == null && global.logoDarkMediaUid() == null) {
      return;
    }
    siteRepository.findFirstByOrderByIdAsc().ifPresent(site -> {
      if (global.logoMediaUid() != null) {
        site.setLogoMediaUid(global.logoMediaUid());
      }
      if (global.logoDarkMediaUid() != null) {
        site.setLogoDarkMediaUid(global.logoDarkMediaUid());
      }
      siteRepository.save(site);
    });
  }

  private SiteSettingsAppGlobalDto buildGlobalResponse(List<SiteSetting> allSettings, Site site) {
    Map<String, String> globalSettingsMap = allSettings.stream()
        .filter(s -> s.getLanguage() == null)
        .collect(Collectors.toMap(
            SiteSetting::getSettingKey,
            SiteSetting::getSettingValue,
            (existing, replacement) -> {
              log.warn("Duplicate global setting found. Keeping existing: {} over replacement: {}", existing,
                  replacement);
              return existing;
            }));

    SiteSettingsAppGlobalDto.AddressDto address = new SiteSettingsAppGlobalDto.AddressDto(
        globalSettingsMap.get("global.address.line1"),
        globalSettingsMap.get("global.address.line2"),
        globalSettingsMap.get("global.address.city"),
        globalSettingsMap.get("global.address.state"),
        globalSettingsMap.get("global.address.postalCode"),
        globalSettingsMap.get("global.address.country"),
        globalSettingsMap.get("global.address.mapEmbedUrl"));

    SiteSettingsAppGlobalDto.SocialDto social = new SiteSettingsAppGlobalDto.SocialDto(
        globalSettingsMap.get("global.social.facebook"),
        globalSettingsMap.get("global.social.instagram"),
        globalSettingsMap.get("global.social.x"),
        globalSettingsMap.get("global.social.linkedin"),
        globalSettingsMap.get("global.social.youtube"),
        globalSettingsMap.get("global.social.tiktok"));

    return new SiteSettingsAppGlobalDto(
        globalSettingsMap.get("global.contactEmail"),
        globalSettingsMap.get("global.contactPhone"),
        globalSettingsMap.get("global.whatsappPhone"),
        globalSettingsMap.get("global.canonicalBaseUrl"),
        RobotsMetaTag.fromValue(globalSettingsMap.get("global.robots")),
        address,
        social,
        site != null ? site.getLogoMediaUid() : null,
        site != null ? site.getLogoDarkMediaUid() : null);
  }

  private SiteSettingsAppI18nDto buildI18nResponse(List<SiteSetting> allSettings, Language language) {
    Map<String, String> i18nSettingsMap = allSettings.stream()
        .filter(s -> Objects.equals(s.getLanguage(), language))
        .collect(Collectors.toMap(
            SiteSetting::getSettingKey,
            SiteSetting::getSettingValue,
            (existing, replacement) -> {
              log.warn("Duplicate i18n setting found. Keeping existing: {} over replacement: {}", existing,
                  replacement);
              return existing;
            }));

    SiteSettingsAppI18nDto.SeoDto seo = new SiteSettingsAppI18nDto.SeoDto(
        i18nSettingsMap.get("i18n.seo.title"),
        i18nSettingsMap.get("i18n.seo.description"),
        i18nSettingsMap.get("i18n.seo.keywords") != null ? List.of(i18nSettingsMap.get("i18n.seo.keywords").split(","))
            : null,
        i18nSettingsMap.get("i18n.seo.ogTitle"),
        i18nSettingsMap.get("i18n.seo.ogDescription"),
        i18nSettingsMap.get("i18n.seo.twitterCard"));

    return new SiteSettingsAppI18nDto(
        i18nSettingsMap.get("i18n.siteName"),
        i18nSettingsMap.get("i18n.tagline"),
        seo,
        i18nSettingsMap.get("i18n.footerText"),
        i18nSettingsMap.get("i18n.headerTopbarText"),
        null // addressLocalized - not implemented yet
    );
  }

  private List<SiteSetting> processGlobalSettings(Long tenantId, SiteSettingsAppGlobalDto global, Long updatedBy) {
    List<SiteSetting> settings = new ArrayList<>();

    if (global.contactEmail() != null) {
      settings.add(upsertSetting(tenantId, "global.contactEmail", global.contactEmail(), null,
          SettingType.TEXT, updatedBy));
    }
    if (global.contactPhone() != null) {
      settings.add(upsertSetting(tenantId, "global.contactPhone", global.contactPhone(), null,
          SettingType.TEXT, updatedBy));
    }
    if (global.whatsappPhone() != null) {
      settings.add(upsertSetting(tenantId, "global.whatsappPhone", global.whatsappPhone(), null,
          SettingType.TEXT, updatedBy));
    }
    if (global.canonicalBaseUrl() != null) {
      settings.add(upsertSetting(tenantId, "global.canonicalBaseUrl", global.canonicalBaseUrl(), null,
          SettingType.URL, updatedBy));
    }
    if (global.robots() != null) {
      settings.add(upsertSetting(tenantId, "global.robots", global.robots().toString(), null, SettingType.TEXT,
          updatedBy));
    }

    if (global.address() != null) {
      var addr = global.address();
      if (addr.line1() != null)
        settings.add(upsertSetting(tenantId, "global.address.line1", addr.line1(), null, SettingType.TEXT, updatedBy));
      if (addr.line2() != null)
        settings.add(upsertSetting(tenantId, "global.address.line2", addr.line2(), null, SettingType.TEXT, updatedBy));
      if (addr.city() != null)
        settings.add(upsertSetting(tenantId, "global.address.city", addr.city(), null, SettingType.TEXT, updatedBy));
      if (addr.state() != null)
        settings.add(upsertSetting(tenantId, "global.address.state", addr.state(), null, SettingType.TEXT, updatedBy));
      if (addr.postalCode() != null)
        settings.add(
            upsertSetting(tenantId, "global.address.postalCode", addr.postalCode(), null, SettingType.TEXT, updatedBy));
      if (addr.country() != null)
        settings
            .add(upsertSetting(tenantId, "global.address.country", addr.country(), null, SettingType.TEXT, updatedBy));
      if (addr.mapEmbedUrl() != null)
        settings.add(upsertSetting(tenantId, "global.address.mapEmbedUrl", addr.mapEmbedUrl(), null, SettingType.TEXT,
            updatedBy));
    }

    if (global.social() != null) {
      var social = global.social();
      if (social.facebook() != null)
        settings.add(
            upsertSetting(tenantId, "global.social.facebook", social.facebook(), null, SettingType.TEXT, updatedBy));
      if (social.instagram() != null)
        settings.add(
            upsertSetting(tenantId, "global.social.instagram", social.instagram(), null, SettingType.TEXT, updatedBy));
      if (social.x() != null)
        settings.add(upsertSetting(tenantId, "global.social.x", social.x(), null, SettingType.TEXT, updatedBy));
      if (social.linkedin() != null)
        settings.add(
            upsertSetting(tenantId, "global.social.linkedin", social.linkedin(), null, SettingType.TEXT, updatedBy));
      if (social.youtube() != null)
        settings
            .add(upsertSetting(tenantId, "global.social.youtube", social.youtube(), null, SettingType.TEXT, updatedBy));
      if (social.tiktok() != null)
        settings
            .add(upsertSetting(tenantId, "global.social.tiktok", social.tiktok(), null, SettingType.TEXT, updatedBy));
    }

    return settings;
  }

  private List<SiteSetting> processI18nSettings(Long tenantId, Language language, SiteSettingsAppI18nDto i18n,
      Long updatedBy) {
    List<SiteSetting> settings = new ArrayList<>();

    if (i18n.siteName() != null) {
      settings
          .add(upsertSetting(tenantId, "i18n.siteName", i18n.siteName(), language, SettingType.I18N_TEXT, updatedBy));
    }
    if (i18n.tagline() != null) {
      settings.add(upsertSetting(tenantId, "i18n.tagline", i18n.tagline(), language, SettingType.I18N_TEXT, updatedBy));
    }

    if (i18n.seo() != null) {
      var seo = i18n.seo();
      if (seo.title() != null)
        settings
            .add(upsertSetting(tenantId, "i18n.seo.title", seo.title(), language, SettingType.I18N_TEXT, updatedBy));
      if (seo.description() != null)
        settings.add(upsertSetting(tenantId, "i18n.seo.description", seo.description(), language, SettingType.I18N_TEXT,
            updatedBy));
      if (seo.keywords() != null)
        settings.add(upsertSetting(tenantId, "i18n.seo.keywords", String.join(",", seo.keywords()), language,
            SettingType.I18N_TEXT, updatedBy));
      if (seo.ogTitle() != null)
        settings.add(
            upsertSetting(tenantId, "i18n.seo.ogTitle", seo.ogTitle(), language, SettingType.I18N_TEXT, updatedBy));
      if (seo.ogDescription() != null)
        settings.add(upsertSetting(tenantId, "i18n.seo.ogDescription", seo.ogDescription(), language,
            SettingType.I18N_TEXT, updatedBy));
      if (seo.twitterCard() != null)
        settings.add(upsertSetting(tenantId, "i18n.seo.twitterCard", seo.twitterCard(), language, SettingType.I18N_TEXT,
            updatedBy));
    }

    if (i18n.footerText() != null) {
      settings.add(
          upsertSetting(tenantId, "i18n.footerText", i18n.footerText(), language, SettingType.I18N_TEXT, updatedBy));
    }
    if (i18n.headerTopbarText() != null) {
      settings.add(upsertSetting(tenantId, "i18n.headerTopbarText", i18n.headerTopbarText(), language,
          SettingType.I18N_TEXT, updatedBy));
    }

    return settings;
  }

  private SiteSetting upsertSetting(Long tenantId, String key, String value, Language language, SettingType type,
      Long updatedBy) {
    Optional<SiteSetting> existingOpt = language == null
        ? repository.findByTenantIdAndSettingKeyAndLanguageIsNull(tenantId, key)
        : repository.findByTenantIdAndSettingKeyAndLanguage(tenantId, key, language);

    SiteSetting setting;
    if (existingOpt.isPresent()) {
      setting = existingOpt.get();
      setting.updateValue(value, updatedBy);
    } else {
      setting = createNewSetting(tenantId, key, value, language, type, updatedBy);
    }

    return setting;
  }

  private SiteSetting createNewSetting(Long tenantId, String key, String value, Language language, SettingType type,
      Long updatedBy) {
    SiteSetting setting = new SiteSetting();
    setting.setSettingKey(key);
    setting.setSettingValue(value);
    setting.setLanguage(language);
    setting.setSettingType(type);
    setting.setCategory("general");
    setting.setIsPublic(false);
    setting.setSortOrder(0);
    setting.setUpdatedBy(updatedBy);
    setting.setUpdatedAt(LocalDateTime.now());
    return setting;
  }
}