package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.CmsDeliveryService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.response.delivery.BatchDeliveryResponse;
import com.backend.presentation.dto.response.delivery.ComponentDeliveryResponse;
import com.backend.shared.common.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/cms")
@RequiredArgsConstructor
@Slf4j
public class CmsDeliveryController {

  private static final int MAX_BATCH_SIZE = 50;

  private final CmsDeliveryService cmsDeliveryService;
  private final MessageSource messageSource;

  @GetMapping("/components/{uid}")
  public ResponseEntity<ApiResponse<ComponentDeliveryResponse>> getComponentByUid(
      @PathVariable String uid,
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Language resolvedLang = resolveLanguage(lang, acceptLanguage);
    Locale locale = Locale.forLanguageTag(acceptLanguage);

    log.debug("CMS Delivery: Fetching component uid={}, lang={}", uid, resolvedLang);

    return cmsDeliveryService.getComponentByUid(uid, resolvedLang)
        .map(response -> ResponseEntity.ok(
            ApiResponse.success(messageSource.getMessage("cms.component.found", null, locale), response)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/components")
  public ResponseEntity<ApiResponse<BatchDeliveryResponse>> getComponentsByUids(
      @RequestParam List<String> uids,
      @RequestParam(required = false) Language lang,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Language resolvedLang = resolveLanguage(lang, acceptLanguage);
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

    log.debug("CMS Delivery: Fetching {} components, lang={}", uids.size(), resolvedLang);

    BatchDeliveryResponse response = cmsDeliveryService.getComponentsByUids(uids, resolvedLang);

    return ResponseEntity.ok(
        ApiResponse.success(messageSource.getMessage("cms.components.found", null, locale), response));
  }

  private Language resolveLanguage(Language langParam, String acceptLanguage) {
    if (langParam != null) {
      return langParam;
    }

    return Language.fromCode(acceptLanguage)
        .orElseGet(cmsDeliveryService::getDefaultLanguage);
  }
}
