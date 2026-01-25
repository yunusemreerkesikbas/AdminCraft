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

import com.backend.domain.entity.SiteSetting;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.SettingType;
import com.backend.domain.repository.SiteSettingRepository;
import com.backend.presentation.dto.request.SiteSettingsGlobalDto;
import com.backend.presentation.dto.request.SiteSettingsI18nDto;
import com.backend.presentation.dto.response.SiteSettingsResponseDto;

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
  public SiteSettingsResponseDto getAdminSettings(Long tenantId) {
    // Batch fetch all settings for this tenant to prevent N+1 queries
    List<SiteSetting> allSettings = repository.findByTenantId(tenantId);

    // Build global settings
    SiteSettingsGlobalDto global = buildGlobalResponse(allSettings);

    // Build language-specific settings for all supported languages
    Map<String, SiteSettingsI18nDto> languages = new HashMap<>();

    // Get all language-specific settings
    Arrays.stream(Language.values()).forEach(lang -> {
      SiteSettingsI18nDto i18nSettings = buildI18nResponse(allSettings, lang);
      languages.put(lang.name().toLowerCase(), i18nSettings);
    });

    return new SiteSettingsResponseDto(global, languages);
  }

  @Override
  public SiteSettingsResponseDto patchSettings(Long tenantId, SiteSettingsGlobalDto global,
      Map<Language, SiteSettingsI18nDto> languages, Long updatedBy) {
    List<SiteSetting> settingsToUpdate = new ArrayList<>();

    // Update global settings if provided
    if (global != null) {
      settingsToUpdate.addAll(processGlobalSettings(tenantId, global, updatedBy));
    }

    // Update language-specific settings if provided
    if (languages != null) {
      for (Map.Entry<Language, SiteSettingsI18nDto> entry : languages.entrySet()) {
        settingsToUpdate.addAll(processI18nSettings(tenantId, entry.getKey(), entry.getValue(), updatedBy));
      }
    }

    // Save all updated settings in batch
    if (!settingsToUpdate.isEmpty()) {
      repository.saveAll(settingsToUpdate);
      log.info("Updated {} site settings for tenant {}", settingsToUpdate.size(), tenantId);
    }

    // Return updated settings
    return getAdminSettings(tenantId);
  }

  private SiteSettingsGlobalDto buildGlobalResponse(List<SiteSetting> allSettings) {
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

    return new SiteSettingsGlobalDto(
        globalSettingsMap.get("global.contactEmail"),
        globalSettingsMap.get("global.contactPhone"),
        globalSettingsMap.get("global.whatsappPhone"),
        globalSettingsMap.get("global.canonicalBaseUrl"),
        globalSettingsMap.get("global.robots"));
  }

  private SiteSettingsI18nDto buildI18nResponse(List<SiteSetting> allSettings, Language language) {
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

    String seoTitle = i18nSettingsMap.get("i18n.seo.title");
    String seoDescription = i18nSettingsMap.get("i18n.seo.description");
    String seoJson = buildSeoJson(seoTitle, seoDescription);

    return new SiteSettingsI18nDto(
        i18nSettingsMap.get("i18n.siteName"),
        i18nSettingsMap.get("i18n.tagline"),
        seoJson,
        i18nSettingsMap.get("i18n.footerText"),
        i18nSettingsMap.get("i18n.headerTopbarText"),
        null // addressLocalized - not implemented yet
    );
  }

  private List<SiteSetting> processGlobalSettings(Long tenantId, SiteSettingsGlobalDto global, Long updatedBy) {
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
      settings.add(upsertSetting(tenantId, "global.robots", global.robots(), null, SettingType.TEXT,
          updatedBy));
    }

    return settings;
  }

  private List<SiteSetting> processI18nSettings(Long tenantId, Language language, SiteSettingsI18nDto i18n,
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
      String seoTitle = extractSeoTitle(i18n.seo());
      String seoDescription = extractSeoDescription(i18n.seo());
      if (seoTitle != null) {
        settings.add(upsertSetting(tenantId, "i18n.seo.title", seoTitle, language, SettingType.I18N_TEXT, updatedBy));
      }
      if (seoDescription != null) {
        settings.add(upsertSetting(tenantId, "i18n.seo.description", seoDescription, language, SettingType.I18N_TEXT,
            updatedBy));
      }
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

  private String extractSeoTitle(String seoJson) {
    if (seoJson == null || seoJson.isEmpty()) {
      return null;
    }
    try {
      if (seoJson.contains("\"title\"")) {
        String[] parts = seoJson.split("\"title\"\\s*:\\s*\"");
        if (parts.length > 1) {
          String titlePart = parts[1];
          int endIndex = titlePart.indexOf("\"");
          if (endIndex > 0) {
            return titlePart.substring(0, endIndex);
          }
        }
      }
    } catch (Exception e) {
      // Return null if parsing fails
    }
    return null;
  }

  private String extractSeoDescription(String seoJson) {
    if (seoJson == null || seoJson.isEmpty()) {
      return null;
    }
    try {
      if (seoJson.contains("\"description\"")) {
        String[] parts = seoJson.split("\"description\"\\s*:\\s*\"");
        if (parts.length > 1) {
          String descPart = parts[1];
          int endIndex = descPart.indexOf("\"");
          if (endIndex > 0) {
            return descPart.substring(0, endIndex);
          }
        }
      }
    } catch (Exception e) {
      // Return null if parsing fails
    }
    return null;
  }

  private String buildSeoJson(String title, String description) {
    if (title == null && description == null) {
      return null;
    }

    StringBuilder json = new StringBuilder("{");
    if (title != null) {
      json.append("\"title\":\"").append(escapeJson(title)).append("\"");
    }
    if (description != null) {
      if (title != null) {
        json.append(",");
      }
      json.append("\"description\":\"").append(escapeJson(description)).append("\"");
    }
    json.append("}");
    return json.toString();
  }

  private String escapeJson(String value) {
    if (value == null) {
      return null;
    }
    return value.replace("\"", "\\\"")
        .replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }
}