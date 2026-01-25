package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.SiteSettingsCommand;
import com.backend.application.dto.SiteSettingsQuery;
import com.backend.domain.entity.SiteSetting;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.SettingType;
import com.backend.domain.repository.SiteSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SiteSettingsServiceImpl implements SiteSettingsService {

  private final SiteSettingRepository repository;

  @Override
  @Transactional(readOnly = true)
  public SiteSettingsQuery getAdminSettings(Long tenantId) {
    // Batch fetch all settings for this tenant to prevent N+1 queries
    List<SiteSetting> allSettings = repository.findByTenantId(tenantId);

    // Build global settings
    SiteSettingsQuery.GlobalSettings global = buildGlobalQuery(allSettings);

    // Build language-specific settings for all supported languages
    Map<Language, SiteSettingsQuery.I18nSettings> languages = new HashMap<>();

    // Get all language-specific settings
    Arrays.stream(Language.values()).forEach(lang -> {
      SiteSettingsQuery.I18nSettings i18nSettings = buildI18nQuery(allSettings, lang);
      languages.put(lang, i18nSettings);
    });

    return new SiteSettingsQuery(global, languages);
  }

  @Override
  public SiteSettingsQuery patchSettings(SiteSettingsCommand command) {
    List<SiteSetting> settingsToUpdate = new ArrayList<>();

    // Update global settings if provided
    if (command.global() != null) {
      settingsToUpdate.addAll(processGlobalCommand(command));
    }

    // Update language-specific settings if provided
    if (command.languages() != null) {
      for (Map.Entry<Language, SiteSettingsCommand.I18nSettings> entry : command.languages().entrySet()) {
        settingsToUpdate
            .addAll(processI18nCommand(command.tenantId(), entry.getKey(), entry.getValue(), command.updatedBy()));
      }
    }

    // Save all updated settings in batch
    if (!settingsToUpdate.isEmpty()) {
      repository.saveAll(settingsToUpdate);
      log.info("Updated {} site settings for tenant {}", settingsToUpdate.size(), command.tenantId());
    }

    // Return updated settings
    return getAdminSettings(command.tenantId());
  }

  private SiteSettingsQuery.GlobalSettings buildGlobalQuery(List<SiteSetting> allSettings) {
    Map<String, String> globalSettingsMap = allSettings.stream()
        .filter(s -> s.getLanguage() == null) // Global settings have null language
        .collect(Collectors.toMap(
            SiteSetting::getSettingKey,
            SiteSetting::getSettingValue,
            (existing, replacement) -> {
              log.warn("Duplicate global setting found. Keeping existing: {} over replacement: {}", existing,
                  replacement);
              return existing;
            }));

    return new SiteSettingsQuery.GlobalSettings(
        globalSettingsMap.get("global.contactEmail"),
        globalSettingsMap.get("global.contactPhone"),
        globalSettingsMap.get("global.whatsappPhone"),
        globalSettingsMap.get("global.canonicalBaseUrl"),
        globalSettingsMap.get("global.robots"));
  }

  private SiteSettingsQuery.I18nSettings buildI18nQuery(List<SiteSetting> allSettings, Language language) {
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

    return new SiteSettingsQuery.I18nSettings(
        i18nSettingsMap.get("i18n.siteName"),
        i18nSettingsMap.get("i18n.tagline"),
        i18nSettingsMap.get("i18n.seo.title"),
        i18nSettingsMap.get("i18n.seo.description"),
        i18nSettingsMap.get("i18n.footerText"),
        i18nSettingsMap.get("i18n.headerTopbarText"));
  }

  private List<SiteSetting> processGlobalCommand(SiteSettingsCommand command) {
    List<SiteSetting> settings = new ArrayList<>();
    SiteSettingsCommand.GlobalSettings global = command.global();

    if (global.contactEmail() != null) {
      settings.add(upsertSetting(command.tenantId(), "global.contactEmail", global.contactEmail(), null,
          SettingType.TEXT, command.updatedBy()));
    }
    if (global.contactPhone() != null) {
      settings.add(upsertSetting(command.tenantId(), "global.contactPhone", global.contactPhone(), null,
          SettingType.TEXT, command.updatedBy()));
    }
    if (global.whatsappPhone() != null) {
      settings.add(upsertSetting(command.tenantId(), "global.whatsappPhone", global.whatsappPhone(), null,
          SettingType.TEXT, command.updatedBy()));
    }
    if (global.canonicalBaseUrl() != null) {
      settings.add(upsertSetting(command.tenantId(), "global.canonicalBaseUrl", global.canonicalBaseUrl(), null,
          SettingType.URL, command.updatedBy()));
    }
    if (global.robots() != null) {
      settings.add(upsertSetting(command.tenantId(), "global.robots", global.robots(), null, SettingType.TEXT,
          command.updatedBy()));
    }

    return settings;
  }

  private List<SiteSetting> processI18nCommand(Long tenantId, Language language, SiteSettingsCommand.I18nSettings i18n,
      Long updatedBy) {
    List<SiteSetting> settings = new ArrayList<>();

    if (i18n.siteName() != null) {
      settings
          .add(upsertSetting(tenantId, "i18n.siteName", i18n.siteName(), language, SettingType.I18N_TEXT, updatedBy));
    }
    if (i18n.tagline() != null) {
      settings.add(upsertSetting(tenantId, "i18n.tagline", i18n.tagline(), language, SettingType.I18N_TEXT, updatedBy));
    }
    if (i18n.seoTitle() != null) {
      settings
          .add(upsertSetting(tenantId, "i18n.seo.title", i18n.seoTitle(), language, SettingType.I18N_TEXT, updatedBy));
    }
    if (i18n.seoDescription() != null) {
      settings.add(upsertSetting(tenantId, "i18n.seo.description", i18n.seoDescription(), language,
          SettingType.I18N_TEXT, updatedBy));
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