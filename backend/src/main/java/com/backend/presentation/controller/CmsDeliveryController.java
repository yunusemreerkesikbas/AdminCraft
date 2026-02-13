package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // kept for navigation endpoint
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.NavigationDeliveryResponse;
import com.backend.application.dto.delivery.PageDeliveryResponse;
import com.backend.application.dto.delivery.SiteDeliveryResponse;
import com.backend.application.service.CmsDeliveryService;
import com.backend.application.service.NavigationService;
import com.backend.domain.enums.Language;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.shared.common.ApiResponse;
import com.google.common.util.concurrent.RateLimiter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/cms")
@RequiredArgsConstructor
@Slf4j
public class CmsDeliveryController {

  private static final int MAX_BATCH_SIZE = 50;

  @Value("${app.cms.rate-limit.permits-per-minute:100.0}")
  private double permitsPerMinute;

  private final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

  private final CmsDeliveryService cmsDeliveryService;
  private final NavigationService navigationService;
  private final MessageSource messageSource;
  private final TenantContext tenantContext;

  @PostConstruct
  public void init() {
    log.info("CMS Delivery Controller initialized with rate limit: {} permits/min", permitsPerMinute);
  }

  @GetMapping("/components/{uid}")
  public ResponseEntity<ApiResponse<ComponentDeliveryResponse>> getComponentByUid(
      @PathVariable String uid,
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
    ResponseEntity<ApiResponse<ComponentDeliveryResponse>> rateLimitResponse = checkRateLimit(locale);
    if (rateLimitResponse != null) {
      return rateLimitResponse;
    }

    Language resolvedLang = resolveLanguage(lang, acceptLanguage);

    log.debug("CMS Delivery: Fetching component uid={}, lang={}", uid, resolvedLang);

    return cmsDeliveryService.getComponentByUid(uid, resolvedLang)
        .map(response -> ResponseEntity.ok(
            ApiResponse.success(messageSource.getMessage("cms.component.found", null, locale), response)))
        .orElseGet(() -> ResponseEntity.ok(
            ApiResponse.error(messageSource.getMessage("cms.component.not.found", null, locale))));
  }

  @GetMapping("/components")
  public ResponseEntity<ApiResponse<BatchDeliveryResponse>> getComponentsByUids(
      @RequestParam List<String> uids,
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
    ResponseEntity<ApiResponse<BatchDeliveryResponse>> rateLimitResponse = checkRateLimit(locale);
    if (rateLimitResponse != null) {
      return rateLimitResponse;
    }

    if (uids == null || uids.isEmpty()) {
      return ResponseEntity.badRequest().body(
          ApiResponse.error(messageSource.getMessage("cms.uids.required", null, locale)));
    }

    if (uids.size() > MAX_BATCH_SIZE) {
      return ResponseEntity.badRequest().body(
          ApiResponse.error(messageSource.getMessage("cms.uids.limit.exceeded",
              new Object[] { MAX_BATCH_SIZE }, locale)));
    }

    Language resolvedLang = resolveLanguage(lang, acceptLanguage);
    log.debug("CMS Delivery: Fetching {} components, lang={}", uids.size(), resolvedLang);

    BatchDeliveryResponse response = cmsDeliveryService.getComponentsByUids(uids, resolvedLang);

    return ResponseEntity.ok(
        ApiResponse.success(messageSource.getMessage("cms.components.found", null, locale), response));
  }

  @GetMapping("/pages")
  public ResponseEntity<ApiResponse<PageDeliveryResponse>> resolvePage(
      @RequestParam(required = false) String pageType,
      @RequestParam(required = false) String pageLabelOrId,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
    ResponseEntity<ApiResponse<PageDeliveryResponse>> rateLimitResponse = checkRateLimit(locale);
    if (rateLimitResponse != null) {
      return rateLimitResponse;
    }

    Language resolvedLang = resolveLanguage(lang, acceptLanguage);
    log.debug("CMS Delivery: Resolving page pageType={}, pageLabelOrId={}, code={}, lang={}",
        pageType, pageLabelOrId, code, resolvedLang);

    return cmsDeliveryService.resolvePageForDelivery(pageType, pageLabelOrId, code, resolvedLang)
        .map(response -> ResponseEntity.ok(
            ApiResponse.success(messageSource.getMessage("cms.page.found", null, locale), response)))
        .orElseGet(() -> ResponseEntity.ok(
            ApiResponse.error(messageSource.getMessage("cms.page.not.found", null, locale))));
  }

  @GetMapping("/site")
  public ResponseEntity<ApiResponse<SiteDeliveryResponse>> getSiteConfig(
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
    ResponseEntity<ApiResponse<SiteDeliveryResponse>> rateLimitResponse = checkRateLimit(locale);
    if (rateLimitResponse != null) {
      return rateLimitResponse;
    }

    log.debug("CMS Delivery: Fetching site config");

    return cmsDeliveryService.getSiteForDelivery()
        .map(response -> ResponseEntity.ok(
            ApiResponse.success(messageSource.getMessage("cms.site.found", null, locale), response)))
        .orElseGet(() -> ResponseEntity.ok(
            ApiResponse.error(messageSource.getMessage("cms.site.not.found", null, locale))));
  }

  @GetMapping("/navigation/{uid}")
  public ResponseEntity<ApiResponse<NavigationDeliveryResponse>> getNavigationByUid(
      @PathVariable String uid,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
    ResponseEntity<ApiResponse<NavigationDeliveryResponse>> rateLimitResponse = checkRateLimit(locale);
    if (rateLimitResponse != null) {
      return rateLimitResponse;
    }

    log.debug("CMS Delivery: Fetching navigation uid={}", uid);

    return navigationService.getNavigationByUid(uid)
        .map(response -> ResponseEntity.ok(
            ApiResponse.success("Navigation found", response)))
        .orElseGet(() -> ResponseEntity.ok(
            ApiResponse.error("Navigation not found")));
  }

  private <T> ResponseEntity<ApiResponse<T>> checkRateLimit(Locale locale) {
    String tenantId = tenantContext.getTenantId();
    if (tenantId == null) {
      return null;
    }

    RateLimiter rateLimiter = rateLimiters.computeIfAbsent(tenantId,
        id -> RateLimiter.create(permitsPerMinute / 60.0));

    if (!rateLimiter.tryAcquire()) {
      log.warn("CMS Delivery: Rate limit exceeded for tenant {}", tenantId);
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
          ApiResponse.error(messageSource.getMessage("cms.rate.limit.exceeded", null, locale)));
    }

    return null;
  }

  private Language resolveLanguage(Language langParam, String acceptLanguage) {
    if (langParam != null) {
      return langParam;
    }

    return Language.fromCode(acceptLanguage)
        .orElseGet(cmsDeliveryService::getDefaultLanguage);
  }
}
