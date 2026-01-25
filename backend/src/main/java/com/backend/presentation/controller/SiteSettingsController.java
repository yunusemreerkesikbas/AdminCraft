package com.backend.presentation.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.SiteSettingsService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.SiteSettingsGlobalDto;
import com.backend.presentation.dto.request.SiteSettingsI18nDto;
import com.backend.presentation.dto.response.SiteSettingsResponseDto;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityHelper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/site-settings")
@RequiredArgsConstructor
@Slf4j
public class SiteSettingsController {

  private final SiteSettingsService service;
  private final SecurityHelper securityHelper;

  @GetMapping
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<SiteSettingsResponseDto>> get() {
    Long tenantId = securityHelper.getCurrentUserTenantId();
    SiteSettingsResponseDto response = service.getAdminSettings(tenantId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PatchMapping
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<SiteSettingsResponseDto>> patch(
      @Valid @RequestBody SiteSettingsPatchRequest req) {
    Long tenantId = securityHelper.getCurrentUserTenantId();
    Long userId = securityHelper.getCurrentUserId();

    // Convert string keys to Language enum
    Map<Language, SiteSettingsI18nDto> languageMap = null;
    if (req.languages() != null) {
      languageMap = new HashMap<>();
      for (Map.Entry<String, SiteSettingsI18nDto> entry : req.languages().entrySet()) {
        Language lang = Language.valueOf(entry.getKey().toUpperCase());
        languageMap.put(lang, entry.getValue());
      }
    }

    SiteSettingsResponseDto response = service.patchSettings(tenantId, req.global(), languageMap, userId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  public record SiteSettingsPatchRequest(
      @Valid SiteSettingsGlobalDto global,
      Map<String, @Valid SiteSettingsI18nDto> languages) {
  }
}
