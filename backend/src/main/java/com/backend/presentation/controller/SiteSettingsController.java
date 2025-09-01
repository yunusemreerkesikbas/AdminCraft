package com.backend.presentation.controller;

import com.backend.application.service.SiteSettingsService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.SiteSettingsGlobalDto;
import com.backend.presentation.dto.request.SiteSettingsI18nDto;
import com.backend.presentation.dto.response.SiteSettingsResponseDto;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/site-settings")
@RequiredArgsConstructor
@Slf4j
public class SiteSettingsController {

  private final SiteSettingsService service;
  private final SecurityHelper securityHelper;

  @GetMapping
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<SiteSettingsResponseDto>> get(
      @RequestParam(name = "lang", defaultValue = "TR") String lang) {
    try {
      Language language = Language.valueOf(lang.toUpperCase());
      SiteSettingsResponseDto dto = service.get(language);
      return ResponseEntity.ok(ApiResponse.success(dto));
    } catch (Exception ex) {
      log.error("Site settings get error", ex);
      return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }
  }

  @PatchMapping
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<SiteSettingsResponseDto>> patch(
      @RequestParam(name = "lang", defaultValue = "TR") String lang,
      @RequestBody SiteSettingsPatchRequest req) {
    try {
      Long userId = securityHelper.getCurrentUserId();
      Language language = Language.valueOf(lang.toUpperCase());
      SiteSettingsResponseDto dto = service.patch(language, req.global(), req.i18n(), userId);
      return ResponseEntity.ok(ApiResponse.success(dto));
    } catch (Exception ex) {
      log.error("Site settings patch error", ex);
      return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }
  }

  public record SiteSettingsPatchRequest(
      SiteSettingsGlobalDto global,
      SiteSettingsI18nDto i18n) {
  }
}
