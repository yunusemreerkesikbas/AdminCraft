package com.backend.presentation.controller;

import com.backend.application.service.SiteSettingsService;
import com.backend.application.dto.SiteSettingsCommand;
import com.backend.application.dto.SiteSettingsQuery;
import com.backend.presentation.dto.request.SiteSettingsGlobalDto;
import com.backend.presentation.dto.request.SiteSettingsI18nDto;
import com.backend.presentation.dto.response.SiteSettingsResponseDto;
import com.backend.presentation.mapper.SiteSettingsMapper;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityHelper;
import java.util.Map;
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
  private final SiteSettingsMapper mapper;
  private final SecurityHelper securityHelper;

  @GetMapping
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<SiteSettingsResponseDto>> get() {
    Long tenantId = securityHelper.getCurrentUserTenantId();
    SiteSettingsQuery query = service.getAdminSettings(tenantId);
    SiteSettingsResponseDto dto = mapper.toResponse(query);
    return ResponseEntity.ok(ApiResponse.success(dto));
  }

  @PatchMapping
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<SiteSettingsResponseDto>> patch(
      @RequestBody SiteSettingsPatchRequest req) {
    Long tenantId = securityHelper.getCurrentUserTenantId();
    Long userId = securityHelper.getCurrentUserId();

    SiteSettingsCommand command = mapper.toCommand(tenantId, req.global(), req.languages(), userId);
    SiteSettingsQuery query = service.patchSettings(command);
    SiteSettingsResponseDto dto = mapper.toResponse(query);
    return ResponseEntity.ok(ApiResponse.success(dto));
  }

  public record SiteSettingsPatchRequest(
      SiteSettingsGlobalDto global,
      Map<String, SiteSettingsI18nDto> languages) {
  }
}
