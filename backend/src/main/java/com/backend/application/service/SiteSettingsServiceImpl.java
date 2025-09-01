package com.backend.application.service;

import com.backend.domain.entity.SiteSetting;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.SettingType;
import com.backend.domain.repository.SiteSettingRepository;
import com.backend.presentation.dto.request.SiteSettingsGlobalDto;
import com.backend.presentation.dto.request.SiteSettingsI18nDto;
import com.backend.presentation.dto.response.SiteSettingsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SiteSettingsServiceImpl implements SiteSettingsService {

  private final SiteSettingRepository repository;

  @Override
  public SiteSettingsResponseDto get(Language language) {
    var global = buildGlobalDto();
    var i18n = buildI18nDto(language);
    return new SiteSettingsResponseDto(global, i18n, language.name());
  }

  @Override
  public SiteSettingsResponseDto patch(Language language,
      SiteSettingsGlobalDto global,
      SiteSettingsI18nDto i18n,
      Long updatedBy) {
    if (global != null) {
      persistGlobal(global, updatedBy);
    }
    if (i18n != null) {
      persistI18n(language, i18n, updatedBy);
    }
    return get(language);
  }

  private SiteSettingsGlobalDto buildGlobalDto() {
    String contactEmail = readValue("global.contactEmail", null);
    String contactPhone = readValue("global.contactPhone", null);
    String whatsappPhone = readValue("global.whatsappPhone", null);
    String address = readValue("global.address", null);
    String businessHours = readValue("global.businessHours", null);
    String social = readValue("global.social", null);
    String canonicalBaseUrl = readValue("global.canonicalBaseUrl", null);
    String robots = readValue("global.robots", "index,follow");
    return new SiteSettingsGlobalDto(contactEmail, contactPhone, whatsappPhone,
        address, businessHours, social, canonicalBaseUrl, robots);
  }

  private SiteSettingsI18nDto buildI18nDto(Language language) {
    String prefix = "i18n." + language.name().toLowerCase() + ".";
    String siteName = readValue(prefix + "siteName", null, language);
    String tagline = readValue(prefix + "tagline", null, language);
    String seo = readValue(prefix + "seo", null, language);
    String footerText = readValue(prefix + "footerText", null, language);
    String headerTopbarText = readValue(prefix + "headerTopbarText", null, language);
    String addressLocalized = readValue(prefix + "addressLocalized", null, language);
    return new SiteSettingsI18nDto(siteName, tagline, seo, footerText,
        headerTopbarText, addressLocalized);
  }

  private void persistGlobal(SiteSettingsGlobalDto dto, Long updatedBy) {
    writeValue("global.contactEmail", dto.contactEmail(), SettingType.TEXT, updatedBy);
    writeValue("global.contactPhone", dto.contactPhone(), SettingType.TEXT, updatedBy);
    writeValue("global.whatsappPhone", dto.whatsappPhone(), SettingType.TEXT, updatedBy);
    writeValue("global.address", dto.address(), SettingType.JSON, updatedBy);
    writeValue("global.businessHours", dto.businessHours(), SettingType.JSON, updatedBy);
    writeValue("global.social", dto.social(), SettingType.JSON, updatedBy);
    writeValue("global.canonicalBaseUrl", dto.canonicalBaseUrl(), SettingType.URL, updatedBy);
    writeValue("global.robots", dto.robots(), SettingType.TEXT, updatedBy);
  }

  private void persistI18n(Language lang, SiteSettingsI18nDto dto, Long updatedBy) {
    String prefix = "i18n." + lang.name().toLowerCase() + ".";
    writeValue(prefix + "siteName", dto.siteName(), SettingType.I18N_TEXT, updatedBy, lang);
    writeValue(prefix + "tagline", dto.tagline(), SettingType.I18N_TEXT, updatedBy, lang);
    writeValue(prefix + "seo", dto.seo(), SettingType.JSON, updatedBy, lang);
    writeValue(prefix + "footerText", dto.footerText(), SettingType.I18N_TEXT, updatedBy, lang);
    writeValue(prefix + "headerTopbarText", dto.headerTopbarText(), SettingType.I18N_TEXT, updatedBy, lang);
    writeValue(prefix + "addressLocalized", dto.addressLocalized(), SettingType.JSON, updatedBy, lang);
  }

  private String readValue(String key, String defaultValue) {
    return repository.findBySettingKeyAndLanguageIsNull(key)
        .map(SiteSetting::getSettingValue)
        .orElse(defaultValue);
  }

  private String readValue(String key, String defaultValue, Language lang) {
    return repository.findBySettingKeyAndLanguage(key, lang)
        .map(SiteSetting::getSettingValue)
        .orElse(defaultValue);
  }

  private void writeValue(String key, String value, SettingType type, Long updatedBy) {
    if (value == null)
      return;
    SiteSetting s = repository.findBySettingKeyAndLanguageIsNull(key)
        .orElseGet(SiteSetting::new);
    s.setSettingKey(key);
    s.setSettingValue(value);
    s.setSettingType(type);
    s.setUpdatedBy(updatedBy);
    s.setUpdatedAt(LocalDateTime.now());
    repository.save(s);
  }

  private void writeValue(String key, String value, SettingType type, Long updatedBy, Language lang) {
    if (value == null)
      return;
    SiteSetting s = repository.findBySettingKeyAndLanguage(key, lang)
        .orElseGet(SiteSetting::new);
    s.setSettingKey(key);
    s.setLanguage(lang);
    s.setSettingValue(value);
    s.setSettingType(type);
    s.setUpdatedBy(updatedBy);
    s.setUpdatedAt(LocalDateTime.now());
    repository.save(s);
  }
}
