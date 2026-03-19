package com.backend.presentation.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.SiteSettingsAppDto;
import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppResponseDto;
import com.backend.application.service.SiteSettingsService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.SiteSettingsGlobalDto;
import com.backend.presentation.dto.request.SiteSettingsI18nDto;
import com.backend.presentation.dto.response.SiteSettingsGlobalResponseDto;
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
  private final MessageSource messageSource;

  @GetMapping
  @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
  public ResponseEntity<ApiResponse<SiteSettingsResponseDto>> get() {
    Locale locale = LocaleContextHolder.getLocale();
    try {
      Long tenantId = securityHelper.getCurrentUserTenantId();
      SiteSettingsAppResponseDto responseApp = service.getAdminSettings(tenantId);
      SiteSettingsResponseDto response = toPresentationResponse(responseApp);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception ex) {
      log.error("Error getting site settings", ex);
      String message = messageSource.getMessage("site.settings.get.error", null, locale);
      return ResponseEntity.internalServerError().body(ApiResponse.error(message));
    }
  }

  @PatchMapping
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  public ResponseEntity<ApiResponse<SiteSettingsResponseDto>> patch(
      @Valid @RequestBody SiteSettingsPatchRequest req) {
    Locale locale = LocaleContextHolder.getLocale();
    try {
      Long tenantId = securityHelper.getCurrentUserTenantId();
      Long userId = securityHelper.getCurrentUserId();

      // Convert string keys to Language enum
      Map<Language, SiteSettingsAppDto.SiteSettingsAppI18nDto> languageMap = null;
      if (req.languages() != null) {
        languageMap = new HashMap<>();
        for (Map.Entry<String, SiteSettingsI18nDto> entry : req.languages().entrySet()) {
          try {
            Language lang = Language.valueOf(entry.getKey().toUpperCase());
            languageMap.put(lang, toAppI18nDto(entry.getValue()));
          } catch (IllegalArgumentException e) {
            log.warn("Invalid language code in request: {}", entry.getKey());
            String message = messageSource.getMessage("common.invalid.language",
                new Object[] { entry.getKey() }, locale);
            return ResponseEntity.badRequest().body(ApiResponse.error(message));
          }
        }
      }

      SiteSettingsAppDto.SiteSettingsAppGlobalDto globalAppDto = toAppGlobalDto(req.global());

      SiteSettingsAppResponseDto responseApp = service.patchSettings(tenantId, globalAppDto, languageMap, userId);
      SiteSettingsResponseDto response = toPresentationResponse(responseApp);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception ex) {
      log.error("Error updating site settings", ex);
      String message = messageSource.getMessage("site.settings.patch.error", null, locale);
      return ResponseEntity.internalServerError().body(ApiResponse.error(message));
    }
  }

  public record SiteSettingsPatchRequest(
      @Valid SiteSettingsGlobalDto global,
      Map<String, @Valid SiteSettingsI18nDto> languages) {
  }

  private SiteSettingsAppDto.SiteSettingsAppGlobalDto toAppGlobalDto(SiteSettingsGlobalDto dto) {
    if (dto == null)
      return null;

    SiteSettingsAppDto.SiteSettingsAppGlobalDto.AddressDto address = null;
    if (dto.address() != null) {
      address = new SiteSettingsAppDto.SiteSettingsAppGlobalDto.AddressDto(
          dto.address().line1(),
          dto.address().line2(),
          dto.address().city(),
          dto.address().state(),
          dto.address().postalCode(),
          dto.address().country(),
          dto.address().mapEmbedUrl());
    }

    SiteSettingsAppDto.SiteSettingsAppGlobalDto.SocialDto social = null;
    if (dto.social() != null) {
      social = new SiteSettingsAppDto.SiteSettingsAppGlobalDto.SocialDto(
          dto.social().facebook(),
          dto.social().instagram(),
          dto.social().x(),
          dto.social().linkedin(),
          dto.social().youtube(),
          dto.social().tiktok());
    }

    return new SiteSettingsAppDto.SiteSettingsAppGlobalDto(
        dto.contactEmail(),
        dto.contactPhone(),
        dto.whatsappPhone(),
        dto.canonicalBaseUrl(),
        dto.robots(),
        address,
        social,
        dto.logoMediaUid(),
        dto.logoDarkMediaUid(),
        null,
        null);
  }

  private SiteSettingsAppDto.SiteSettingsAppI18nDto toAppI18nDto(SiteSettingsI18nDto dto) {
    if (dto == null)
      return null;
    return new SiteSettingsAppDto.SiteSettingsAppI18nDto(
        dto.siteName(),
        dto.tagline(),
        mapSeoToApp(dto.seo()),
        dto.footerText(),
        dto.headerTopbarText());
  }

  private SiteSettingsAppDto.SiteSettingsAppI18nDto.SeoDto mapSeoToApp(SiteSettingsI18nDto.SeoDto seoDto) {
    if (seoDto == null)
      return null;
    return new SiteSettingsAppDto.SiteSettingsAppI18nDto.SeoDto(
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

    SiteSettingsGlobalResponseDto global = null;
    if (dto.global() != null) {
      SiteSettingsGlobalResponseDto.AddressDto address = null;
      if (dto.global().address() != null) {
        address = new SiteSettingsGlobalResponseDto.AddressDto(
            dto.global().address().line1(),
            dto.global().address().line2(),
            dto.global().address().city(),
            dto.global().address().state(),
            dto.global().address().postalCode(),
            dto.global().address().country(),
            dto.global().address().mapEmbedUrl());
      }

      SiteSettingsGlobalResponseDto.SocialDto social = null;
      if (dto.global().social() != null) {
        social = new SiteSettingsGlobalResponseDto.SocialDto(
            dto.global().social().facebook(),
            dto.global().social().instagram(),
            dto.global().social().x(),
            dto.global().social().linkedin(),
            dto.global().social().youtube(),
            dto.global().social().tiktok());
      }

      global = new SiteSettingsGlobalResponseDto(
          dto.global().contactEmail(),
          dto.global().contactPhone(),
          dto.global().whatsappPhone(),
          dto.global().canonicalBaseUrl(),
          dto.global().robots(),
          address,
          social,
          dto.global().logoMediaUid(),
          dto.global().logoDarkMediaUid(),
          mapMediaSummaryToPresentation(dto.global().logoMedia()),
          mapMediaSummaryToPresentation(dto.global().logoDarkMedia()));
    }

    Map<String, SiteSettingsI18nDto> languages = null;
    if (dto.languages() != null) {
      languages = dto.languages().entrySet().stream().collect(Collectors.toMap(
          Map.Entry::getKey,
          e -> {
            SiteSettingsAppDto.SiteSettingsAppI18nDto v = e.getValue();
            return new SiteSettingsI18nDto(
                v.siteName(),
                v.tagline(),
                mapSeoToPresentation(v.seo()),
                v.footerText(),
                v.headerTopbarText(),
                null);
          }));
    }

    return new SiteSettingsResponseDto(global, languages);
  }

  private SiteSettingsGlobalResponseDto.MediaSummaryDto mapMediaSummaryToPresentation(
      SiteSettingsAppDto.MediaSummaryDto mediaSummary) {
    if (mediaSummary == null) {
      return null;
    }
    return new SiteSettingsGlobalResponseDto.MediaSummaryDto(
        mediaSummary.id(),
        mediaSummary.uid(),
        mediaSummary.fileName(),
        mediaSummary.originalName(),
        mediaSummary.mimeType(),
        mediaSummary.publicUrl(),
        mediaSummary.width(),
        mediaSummary.height(),
        mediaSummary.fileSizeFormatted());
  }

  private SiteSettingsI18nDto.SeoDto mapSeoToPresentation(SiteSettingsAppDto.SiteSettingsAppI18nDto.SeoDto seoAppDto) {
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
