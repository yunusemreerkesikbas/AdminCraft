package com.backend.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.PageDeliveryResponse;
import com.backend.application.dto.delivery.SiteDeliveryResponse;
import com.backend.application.dto.delivery.SitemapPageEntry;
import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.entity.Site;
import com.backend.domain.entity.SiteSetting;
import com.backend.domain.entity.SiteTechnicalSettings;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.RobotTag;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.PageI18nRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.SiteSettingRepository;
import com.backend.domain.repository.SiteTechnicalSettingsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
public class CmsDeliveryServiceImpl implements CmsDeliveryService {

  private static final String CANONICAL_BASE_URL_KEY = "global.canonicalBaseUrl";
  private static final String SEO_TITLE_KEY = "i18n.seo.title";
  private static final String SEO_DESCRIPTION_KEY = "i18n.seo.description";
  private static final String SEO_KEYWORDS_KEY = "i18n.seo.keywords";
  private static final String SEO_OG_TITLE_KEY = "i18n.seo.ogTitle";
  private static final String SEO_OG_DESCRIPTION_KEY = "i18n.seo.ogDescription";
  private static final String SEO_TWITTER_CARD_KEY = "i18n.seo.twitterCard";
  private static final String SEO_TITLE_SEPARATOR_KEY = "i18n.seo.titleSeparator";

  // Global contact
  private static final String CONTACT_EMAIL_KEY   = "global.contactEmail";
  private static final String CONTACT_PHONE_KEY   = "global.contactPhone";
  private static final String WHATSAPP_PHONE_KEY  = "global.whatsappPhone";
  // Global address
  private static final String ADDRESS_LINE1_KEY   = "global.address.line1";
  private static final String ADDRESS_LINE2_KEY   = "global.address.line2";
  private static final String ADDRESS_CITY_KEY    = "global.address.city";
  private static final String ADDRESS_STATE_KEY   = "global.address.state";
  private static final String ADDRESS_POSTAL_KEY  = "global.address.postalCode";
  private static final String ADDRESS_COUNTRY_KEY = "global.address.country";
  private static final String ADDRESS_MAP_KEY     = "global.address.mapEmbedUrl";
  // Global social
  private static final String SOCIAL_FACEBOOK_KEY  = "global.social.facebook";
  private static final String SOCIAL_INSTAGRAM_KEY = "global.social.instagram";
  private static final String SOCIAL_X_KEY         = "global.social.x";
  private static final String SOCIAL_LINKEDIN_KEY  = "global.social.linkedin";
  private static final String SOCIAL_YOUTUBE_KEY   = "global.social.youtube";
  private static final String SOCIAL_TIKTOK_KEY    = "global.social.tiktok";
  // Global robots default
  private static final String DEFAULT_ROBOTS_KEY  = "global.robots";
  // Per-language i18n
  private static final String I18N_SITE_NAME_KEY  = "i18n.siteName";
  private static final String I18N_TAGLINE_KEY    = "i18n.tagline";

  private final ComponentDeliveryService componentDeliveryService;
  private final PageDeliveryService pageDeliveryService;
  private final TenantContextPort tenantContext;
  private final SiteRepository siteRepository;
  private final SiteSettingRepository siteSettingRepository;
  private final SiteTechnicalSettingsRepository siteTechnicalSettingsRepository;
  private final MediaService mediaService;
  private final PageI18nRepository pageI18nRepository;
  private final PageRepository pageRepository;

  @Override
  public Optional<ComponentDeliveryResponse> getComponentByUid(String uid, Language lang) {
    Language resolvedLang = lang != null ? lang : getDefaultLanguage();
    return componentDeliveryService.getComponentByUid(uid, resolvedLang);
  }

  @Override
  public BatchDeliveryResponse getComponentsByUids(List<String> uids, Language lang) {
    Language resolvedLang = lang != null ? lang : getDefaultLanguage();
    return componentDeliveryService.getComponentsByUids(uids, resolvedLang);
  }

  @Override
  public Optional<PageDeliveryResponse> resolvePageForDelivery(String pageType, String pageLabelOrId, String code,
      Language lang) {
    Language resolvedLang = lang != null ? lang : getDefaultLanguage();
    return pageDeliveryService.resolvePageForDelivery(pageType, pageLabelOrId, code, resolvedLang);
  }

  @Override
  public Optional<SiteDeliveryResponse> getSiteForDelivery(Language lang) {
    Language resolvedLang = lang != null ? lang : getDefaultLanguage();
    Long tenantId = Long.parseLong(tenantContext.getTenantId());
    Map<String, String> globalSettings = toSettingsMap(siteSettingRepository.findByTenantIdAndLanguageIsNull(tenantId));
    Map<String, String> localizedSettings = toSettingsMap(siteSettingRepository.findByTenantIdAndLanguage(tenantId, resolvedLang));

    return siteRepository.findFirstByOrderByIdAsc().map(site -> {
      SiteTechnicalSettings technicalSettings = siteTechnicalSettingsRepository.findBySiteId(site.getId()).orElse(null);
      String logoUrl = mediaService.resolvePublicUrl(site.getLogoMediaUid());
      String logoDarkUrl = mediaService.resolvePublicUrl(site.getLogoDarkMediaUid());
      SiteDeliveryResponse.SeoInfo seo = buildSeoInfo(localizedSettings);
      String canonicalBaseUrl = readValue(globalSettings, CANONICAL_BASE_URL_KEY);
      SiteDeliveryResponse.SearchEngineInfo searchEngine = buildSearchEngineInfo(technicalSettings, globalSettings);
      SiteDeliveryResponse.ContactInfo contact = buildContactInfo(globalSettings);
      SiteDeliveryResponse.AddressInfo address = buildAddressInfo(globalSettings);
      SiteDeliveryResponse.SocialLinksInfo social = buildSocialLinksInfo(globalSettings);
      SiteDeliveryResponse.I18nInfo i18n = buildI18nInfo(localizedSettings);
      SiteDeliveryResponse.CookieConsentInfo cookieConsent = buildCookieConsentInfo(technicalSettings, localizedSettings);
      return SiteDeliveryResponse.from(site, logoUrl, logoDarkUrl, seo, canonicalBaseUrl,
          searchEngine, contact, address, social, i18n, cookieConsent);
    });
  }

  @Override
  public List<SitemapPageEntry> getSitemapPages(Language lang) {
    Language resolvedLang = lang != null ? lang : getDefaultLanguage();

    return siteRepository.findFirstByOrderByIdAsc().map(site -> {
      SiteTechnicalSettings tech = siteTechnicalSettingsRepository.findBySiteId(site.getId()).orElse(null);
      if (tech != null && Boolean.FALSE.equals(tech.getIndexingEnabled())) {
        return List.<SitemapPageEntry>of();
      }

      List<PageI18n> publishedPages = pageI18nRepository.findByLanguageAndStatus(resolvedLang, PageStatus.PUBLISHED);
      if (publishedPages.isEmpty()) return List.<SitemapPageEntry>of();

      List<Long> pageIds = publishedPages.stream().map(PageI18n::getPageId).toList();
      Map<Long, Page> pageMap = pageRepository.findAllById(pageIds).stream()
          .collect(Collectors.toMap(Page::getId, p -> p));

      return publishedPages.stream()
          .filter(pi -> {
            Page page = pageMap.get(pi.getPageId());
            if (page == null) return false;
            RobotTag rt = page.getRobotTag();
            return rt == null || (rt != RobotTag.NOINDEX_FOLLOW && rt != RobotTag.NOINDEX_NOFOLLOW);
          })
          .map(pi -> {
            Page page = pageMap.get(pi.getPageId());
            return new SitemapPageEntry(
                pi.getUid(),
                page.getPageType() != null ? page.getPageType().name() : null,
                pi.getCanonicalUrl(),
                pi.getUpdatedAt());
          })
          .toList();
    }).orElse(List.of());
  }

  @Override
  public Language getDefaultLanguage() {
    return tenantContext.getDefaultLanguage();
  }

  private Map<String, String> toSettingsMap(List<SiteSetting> settings) {
    return settings.stream()
        .collect(Collectors.toMap(
            SiteSetting::getSettingKey,
            SiteSetting::getSettingValue,
            (existing, replacement) -> existing));
  }

  private SiteDeliveryResponse.SeoInfo buildSeoInfo(Map<String, String> localizedSettings) {
    String title = readValue(localizedSettings, SEO_TITLE_KEY);
    String description = readValue(localizedSettings, SEO_DESCRIPTION_KEY);
    List<String> keywords = parseKeywords(localizedSettings.get(SEO_KEYWORDS_KEY));
    String ogTitle = readValue(localizedSettings, SEO_OG_TITLE_KEY);
    String ogDescription = readValue(localizedSettings, SEO_OG_DESCRIPTION_KEY);
    String twitterCard = readValue(localizedSettings, SEO_TWITTER_CARD_KEY);
    String titleSeparator = readValue(localizedSettings, SEO_TITLE_SEPARATOR_KEY);

    if (title == null && description == null && keywords == null && ogTitle == null && ogDescription == null
        && twitterCard == null && titleSeparator == null) {
      return null;
    }

    return new SiteDeliveryResponse.SeoInfo(title, description, keywords, ogTitle, ogDescription, twitterCard, titleSeparator);
  }

  private SiteDeliveryResponse.SearchEngineInfo buildSearchEngineInfo(SiteTechnicalSettings settings, Map<String, String> globalSettings) {
    String defaultRobots = readValue(globalSettings, DEFAULT_ROBOTS_KEY);

    return new SiteDeliveryResponse.SearchEngineInfo(
        settings != null ? settings.getSitemapEnabled() : Boolean.TRUE,
        settings != null ? settings.getIndexingEnabled() : Boolean.TRUE,
        defaultRobots);
  }

  private SiteDeliveryResponse.CookieConsentInfo buildCookieConsentInfo(SiteTechnicalSettings settings, Map<String, String> localizedSettings) {
    if (settings == null || !Boolean.TRUE.equals(settings.getCookieConsentEnabled())) {
      return null;
    }
    String text = readValue(localizedSettings, "i18n.cookie.consent.text");
    return new SiteDeliveryResponse.CookieConsentInfo(true, text);
  }

  private SiteDeliveryResponse.ContactInfo buildContactInfo(Map<String, String> g) {
    String email = readValue(g, CONTACT_EMAIL_KEY);
    String phone = readValue(g, CONTACT_PHONE_KEY);
    String whatsapp = readValue(g, WHATSAPP_PHONE_KEY);
    if (email == null && phone == null && whatsapp == null) return null;
    return new SiteDeliveryResponse.ContactInfo(email, phone, whatsapp);
  }

  private SiteDeliveryResponse.AddressInfo buildAddressInfo(Map<String, String> g) {
    String line1 = readValue(g, ADDRESS_LINE1_KEY);
    String line2 = readValue(g, ADDRESS_LINE2_KEY);
    String city = readValue(g, ADDRESS_CITY_KEY);
    String state = readValue(g, ADDRESS_STATE_KEY);
    String postalCode = readValue(g, ADDRESS_POSTAL_KEY);
    String country = readValue(g, ADDRESS_COUNTRY_KEY);
    String mapEmbedUrl = readValue(g, ADDRESS_MAP_KEY);
    if (line1 == null && city == null && country == null) return null;
    return new SiteDeliveryResponse.AddressInfo(line1, line2, city, state, postalCode, country, mapEmbedUrl);
  }

  private SiteDeliveryResponse.SocialLinksInfo buildSocialLinksInfo(Map<String, String> g) {
    String facebook = readValue(g, SOCIAL_FACEBOOK_KEY);
    String instagram = readValue(g, SOCIAL_INSTAGRAM_KEY);
    String x = readValue(g, SOCIAL_X_KEY);
    String linkedin = readValue(g, SOCIAL_LINKEDIN_KEY);
    String youtube = readValue(g, SOCIAL_YOUTUBE_KEY);
    String tiktok = readValue(g, SOCIAL_TIKTOK_KEY);
    if (facebook == null && instagram == null && x == null && linkedin == null && youtube == null && tiktok == null) {
      return null;
    }
    return new SiteDeliveryResponse.SocialLinksInfo(facebook, instagram, x, linkedin, youtube, tiktok);
  }

  private SiteDeliveryResponse.I18nInfo buildI18nInfo(Map<String, String> loc) {
    String siteName = readValue(loc, I18N_SITE_NAME_KEY);
    String tagline = readValue(loc, I18N_TAGLINE_KEY);
    if (siteName == null && tagline == null) return null;
    return new SiteDeliveryResponse.I18nInfo(siteName, tagline);
  }

  private List<String> parseKeywords(String rawKeywords) {
    String value = readValue(rawKeywords);
    if (value == null) {
      return null;
    }

    List<String> keywords = java.util.Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(keyword -> !keyword.isEmpty())
        .distinct()
        .toList();

    return keywords.isEmpty() ? null : keywords;
  }

  private String readValue(Map<String, String> settings, String key) {
    return readValue(settings.get(key));
  }

  private String readValue(String value) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
