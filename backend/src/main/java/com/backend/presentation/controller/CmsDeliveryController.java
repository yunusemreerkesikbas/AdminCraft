package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.delivery.BatchDeliveryResponse;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.NavigationDeliveryResponse;
import com.backend.application.dto.delivery.PageDeliveryResponse;
import com.backend.application.dto.delivery.ShellDeliveryResponse;
import com.backend.application.dto.delivery.SiteDeliveryResponse;
import com.backend.application.dto.delivery.SitemapPageEntry;
import com.backend.application.service.CmsDeliveryService;
import com.backend.application.service.NavigationService;
import com.backend.application.service.ShellDeliveryService;
import com.backend.application.service.SiteTechnicalService;
import com.backend.domain.enums.Language;
import com.backend.shared.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/cms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CMS Delivery", description = "Public CMS delivery endpoints for storefront rendering")
public class CmsDeliveryController {

  private static final int MAX_BATCH_SIZE = 50;

  private final CmsDeliveryService cmsDeliveryService;
  private final NavigationService navigationService;
  private final SiteTechnicalService siteTechnicalService;
  private final ShellDeliveryService shellDeliveryService;
  private final MessageSource messageSource;

  @GetMapping("/components/{uid}")
  @Operation(summary = "Get component by UID", description = "Returns a CMS component delivery payload by UID")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Response returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
  })
  public ResponseEntity<ApiResponse<ComponentDeliveryResponse>> getComponentByUid(
      @PathVariable String uid,
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
    Language resolvedLang = resolveLanguage(lang, acceptLanguage);

    log.debug("CMS Delivery: Fetching component uid={}, lang={}", uid, resolvedLang);

    return cmsDeliveryService.getComponentByUid(uid, resolvedLang)
        .map(response -> ResponseEntity.ok(
            ApiResponse.success(messageSource.getMessage("cms.component.found", null, locale), response)))
        .orElseGet(() -> ResponseEntity.ok(
            ApiResponse.error(messageSource.getMessage("cms.component.not.found", null, locale))));
  }

  @GetMapping("/components")
  @Operation(summary = "Get components batch", description = "Returns CMS components for given UIDs")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Response returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
  })
  public ResponseEntity<ApiResponse<BatchDeliveryResponse>> getComponentsByUids(
      @RequestParam List<String> uids,
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);

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
  @Operation(summary = "Resolve page", description = "Resolves and returns CMS page delivery payload")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Response returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
  })
  public ResponseEntity<ApiResponse<PageDeliveryResponse>> resolvePage(
      @RequestParam(required = false) String pageType,
      @RequestParam(required = false) String pageLabelOrId,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
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
  @Operation(summary = "Get site config", description = "Returns CMS site config for delivery")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Response returned")
  })
  public ResponseEntity<ApiResponse<SiteDeliveryResponse>> getSiteConfig(
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
    Language resolvedLang = resolveLanguage(lang, acceptLanguage);
    log.debug("CMS Delivery: Fetching site config");

    return cmsDeliveryService.getSiteForDelivery(resolvedLang)
        .map(response -> ResponseEntity.ok(
            ApiResponse.success(messageSource.getMessage("cms.site.found", null, locale), response)))
        .orElseGet(() -> ResponseEntity.ok(
            ApiResponse.error(messageSource.getMessage("cms.site.not.found", null, locale))));
  }

  @GetMapping("/navigation/{uid}")
  @Operation(summary = "Get navigation by UID", description = "Returns navigation delivery payload")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Response returned")
  })
  public ResponseEntity<ApiResponse<NavigationDeliveryResponse>> getNavigationByUid(
      @PathVariable String uid,
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Language resolvedLang = resolveLanguage(lang, acceptLanguage);
    log.debug("CMS Delivery: Fetching navigation uid={}, lang={}", uid, resolvedLang);

    return navigationService.getNavigationByUid(uid, resolvedLang)
        .map(response -> ResponseEntity.ok(
            ApiResponse.success("Navigation found", response)))
        .orElseGet(() -> ResponseEntity.ok(
            ApiResponse.error("Navigation not found")));
  }

  @GetMapping("/pages/sitemap")
  @Operation(summary = "Get sitemap pages", description = "Returns sitemap entries for public pages")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sitemap returned")
  })
  public ResponseEntity<ApiResponse<List<SitemapPageEntry>>> getSitemapPages(
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
    Language resolvedLang = resolveLanguage(lang, acceptLanguage);
    log.debug("CMS Delivery: Fetching sitemap pages, lang={}", resolvedLang);
    List<SitemapPageEntry> entries = cmsDeliveryService.getSitemapPages(resolvedLang);
    return ResponseEntity.ok(ApiResponse.success(
        messageSource.getMessage("cms.sitemap.found", null, locale), entries));
  }

  @GetMapping("/shell")
  @Operation(summary = "Get shell", description = "Returns shell delivery response for storefront chrome")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shell returned")
  })
  public ResponseEntity<ApiResponse<ShellDeliveryResponse>> getShell(
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
    Language resolvedLang = resolveLanguage(lang, acceptLanguage);
    log.debug("CMS Delivery: Fetching shell, lang={}", resolvedLang);

    return shellDeliveryService.getShellForDelivery(resolvedLang)
        .map(shell -> ResponseEntity.ok(
            ApiResponse.success(messageSource.getMessage("cms.shell.found", null, locale), shell)))
        .orElseGet(() -> ResponseEntity.ok(
            ApiResponse.error(messageSource.getMessage("cms.shell.not.found", null, locale))));
  }

  @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(summary = "Get robots.txt", description = "Returns robots.txt content for current tenant site")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "robots.txt returned")
  })
  public ResponseEntity<String> getRobotsTxt() {
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .body(siteTechnicalService.getRobotsTxt());
  }

  private Language resolveLanguage(Language langParam, String acceptLanguage) {
    if (langParam != null) {
      return langParam;
    }

    return Language.fromCode(acceptLanguage)
        .orElseGet(cmsDeliveryService::getDefaultLanguage);
  }
}
