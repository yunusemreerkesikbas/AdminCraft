package com.backend.presentation.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppGlobalDto;
import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppI18nDto;
import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppResponseDto;
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
    SiteSettingsAppResponseDto responseApp = service.getAdminSettings(tenantId);
    SiteSettingsResponseDto response = toPresentationResponse(responseApp);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PatchMapping
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<SiteSettingsResponseDto>> patch(
      @Valid @RequestBody SiteSettingsPatchRequest req) {
    Long tenantId = securityHelper.getCurrentUserTenantId();
    Long userId = securityHelper.getCurrentUserId();

    // Convert string keys to Language enum
    Map<Language, SiteSettingsAppI18nDto> languageMap = null;
    if (req.languages() != null) {
      languageMap = new HashMap<>();
      for (Map.Entry<String, SiteSettingsI18nDto> entry : req.languages().entrySet()) {
        try {
          Language lang = Language.valueOf(entry.getKey().toUpperCase());
          languageMap.put(lang, toAppI18nDto(entry.getValue()));
        } catch (IllegalArgumentException e) {
          log.warn("Invalid language code in request: {}", entry.getKey());
          // Optionally throw exception or ignore invalid language
          return ResponseEntity.badRequest()
              .body(ApiResponse.error("Invalid language code: " + entry.getKey()));
        }
      }
    }

    SiteSettingsAppGlobalDto globalAppDto = toAppGlobalDto(req.global());

    SiteSettingsAppResponseDto responseApp = service.patchSettings(tenantId, globalAppDto, languageMap, userId);
    SiteSettingsResponseDto response = toPresentationResponse(responseApp);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  public record SiteSettingsPatchRequest(
      @Valid SiteSettingsGlobalDto global,
      Map<String, @Valid SiteSettingsI18nDto> languages) {
  }

  private SiteSettingsAppGlobalDto toAppGlobalDto(SiteSettingsGlobalDto dto) {
    if (dto == null)
      return null;

    SiteSettingsAppGlobalDto.AddressDto address = null;
    if (dto.address() != null) {
      address = new SiteSettingsAppGlobalDto.AddressDto(
          dto.address().line1(),
          dto.address().line2(),
          dto.address().city(),
          dto.address().state(),
          dto.address().postalCode(),
          dto.address().country(),
          dto.address().mapEmbedUrl());
    }

    SiteSettingsAppGlobalDto.SocialDto social = null;
    if (dto.social() != null) {
      social = new SiteSettingsAppGlobalDto.SocialDto(
          dto.social().facebook(),
          dto.social().instagram(),
          dto.social().x(),
          dto.social().linkedin(),
          dto.social().youtube(),
          dto.social().tiktok());
    }

    return new SiteSettingsAppGlobalDto(
        dto.contactEmail(),
        dto.contactPhone(),
        dto.whatsappPhone(),
        dto.canonicalBaseUrl(),
        dto.robots(),
        address,
        social);
  }

  private SiteSettingsAppI18nDto toAppI18nDto(SiteSettingsI18nDto dto) {
    if (dto == null)
      return null;
    return new SiteSettingsAppI18nDto(
        dto.siteName(),
        dto.tagline(),
        mapSeoToApp(dto.seo()),
        dto.footerText(),
        dto.headerTopbarText(),
        dto.addressLocalized());
  }

  private SiteSettingsAppI18nDto.SeoDto mapSeoToApp(SiteSettingsI18nDto.SeoDto seoDto) {
    if (seoDto == null)
      return null;
    return new SiteSettingsAppI18nDto.SeoDto(
        seoDto.title(),
        seoDto.description(),
        seoDto.keywords(),
        seoDto.ogTitle(),
        seoDto.ogDescription(),
        seoDto.twitterCard());
  }

  private SiteSettingsResponseDto toPresentationResponse(SiteSettingsAppResponseDto dto) {
    if (dto == null)
      return null;

    SiteSettingsGlobalDto global = null;
    if (dto.global() != null) {
      SiteSettingsGlobalDto.AddressDto address = null;
      if (dto.global().address() != null) {
        address = new SiteSettingsGlobalDto.AddressDto(
            dto.global().address().line1(),
            dto.global().address().line2(),
            dto.global().address().city(),
            dto.global().address().state(),
            dto.global().address().postalCode(),
            dto.global().address().country(),
            dto.global().address().mapEmbedUrl());
      }

      SiteSettingsGlobalDto.SocialDto social = null;
      if (dto.global().social() != null) {
        social = new SiteSettingsGlobalDto.SocialDto(
            dto.global().social().facebook(),
            dto.global().social().instagram(),
            dto.global().social().x(),
            dto.global().social().linkedin(),
            dto.global().social().youtube(),
            dto.global().social().tiktok());
      }

      global = new SiteSettingsGlobalDto(
          dto.global().contactEmail(),
          dto.global().contactPhone(),
          dto.global().whatsappPhone(),
          dto.global().canonicalBaseUrl(),
          dto.global().robots(),
          address,
          social);
    }

    Map<String, SiteSettingsI18nDto> languages = null;
    if (dto.languages() != null) {
      languages = dto.languages().entrySet().stream().collect(Collectors.toMap(
          Map.Entry::getKey,
          e -> {
            SiteSettingsAppI18nDto v = e.getValue();
            return new SiteSettingsI18nDto(
                v.siteName(),
                v.tagline(),
                mapSeoToPresentation(v.seo()),
                v.footerText(),
                v.headerTopbarText(),
                v.addressLocalized());
          }));
    }

    return new SiteSettingsResponseDto(global, languages);
  }

  private SiteSettingsI18nDto.SeoDto mapSeoToPresentation(SiteSettingsAppI18nDto.SeoDto seoAppDto) {
    if (seoAppDto == null)
      return null;
    return new SiteSettingsI18nDto.SeoDto(
        seoAppDto.title(),
        seoAppDto.description(),
        seoAppDto.keywords(),
        seoAppDto.ogTitle(),
        seoAppDto.ogDescription(),
        seoAppDto.twitterCard());
  }
}
