package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.SecuritySettingsResult;
import com.backend.application.dto.request.CreateSiteRequest;
import com.backend.application.dto.request.SiteTechnicalPatchRequest;
import com.backend.application.dto.request.UpdateSiteRequest;
import com.backend.application.dto.response.SiteOverviewAppDto;
import com.backend.application.dto.response.SiteTechnicalAppDto;
import com.backend.application.service.SecuritySettingsService;
import com.backend.application.service.SiteOverviewService;
import com.backend.application.service.SiteService;
import com.backend.application.service.SiteTechnicalService;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.UpdateSecuritySettingsRequest;
import com.backend.presentation.dto.response.SecuritySettingsResponse;
import com.backend.presentation.dto.response.SiteOverviewResponse;
import com.backend.presentation.dto.response.SiteResponse;
import com.backend.presentation.dto.response.SiteTechnicalResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityHelper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/sites")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class SiteController {

    private final SiteService siteService;
    private final SiteOverviewService siteOverviewService;
    private final SiteTechnicalService siteTechnicalService;
    private final SecuritySettingsService securitySettingsService;
    private final MessageSource messageSource;
    private final SecurityHelper securityHelper;

    @PostMapping
    public ResponseEntity<ApiResponse<SiteResponse>> createSite(
            @Valid @RequestBody CreateSiteRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.createSite(request, securityHelper.getCurrentUserId(), displayLanguage);
            String message = messageSource.getMessage("site.created.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error creating site: {}", ex.getMessage());
            String message = messageSource.getMessage("site.create.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteResponse>> getSiteById(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            Optional<SiteResponse> response = siteService.getSiteById(id, displayLanguage);
            if (response.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(response.get()));
            } else {
                String message = messageSource.getMessage("site.not.found", new Object[] { id },
                        Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting site by id {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.get.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getAllSites(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            List<SiteResponse> sites = siteService.getAllSites(displayLanguage);
            return ResponseEntity.ok(ApiResponse.success(sites));
        } catch (Exception ex) {
            log.error("Error getting all sites: {}", ex.getMessage());
            String message = messageSource.getMessage("site.list.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteResponse>> updateSite(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @Valid @RequestBody UpdateSiteRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.updateSite(id, request, securityHelper.getCurrentUserId(),
                    displayLanguage);
            String message = messageSource.getMessage("site.updated.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error updating site {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.update.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSite(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            siteService.deleteSite(id);
            String message = messageSource.getMessage("site.delete.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting site {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.delete.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<SiteResponse>> publishSite(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.publishSite(id, displayLanguage);
            String message = messageSource.getMessage("site.published.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error publishing site {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.publish.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<SiteResponse>> unpublishSite(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.unpublishSite(id, displayLanguage);
            String message = messageSource.getMessage("site.unpublished.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error unpublishing site {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.unpublish.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PostMapping("/{id}/maintenance")
    public ResponseEntity<ApiResponse<SiteResponse>> enableMaintenanceMode(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestParam(required = false) String message,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.enableMaintenanceMode(id, message, displayLanguage);
            String successMessage = messageSource.getMessage("site.maintenance.enabled.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(successMessage, response));
        } catch (Exception ex) {
            log.error("Error enabling maintenance mode for site {}: {}", id, ex.getMessage());
            String errorMessage = messageSource.getMessage("site.maintenance.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(errorMessage));
        }
    }

    @DeleteMapping("/{id}/maintenance")
    public ResponseEntity<ApiResponse<SiteResponse>> disableMaintenanceMode(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            SiteResponse response = siteService.disableMaintenanceMode(id, displayLanguage);
            String message = messageSource.getMessage("site.maintenance.disabled.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error disabling maintenance mode for site {}: {}", id, ex.getMessage());
            String errorMessage = messageSource.getMessage("site.maintenance.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(errorMessage));
        }
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<ApiResponse<SiteResponse>> getSiteByDomain(
            @PathVariable @Valid String domain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            String sanitizedDomain = sanitizeInput(domain);
            if (sanitizedDomain == null || sanitizedDomain.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid domain");
            }

            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            Optional<SiteResponse> response = siteService.getSiteByDomain(sanitizedDomain, displayLanguage);
            if (response.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(response.get()));
            } else {
                String message = messageSource.getMessage("site.domain.not.found", new Object[] { domain },
                        Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting site by domain {}: {}", domain, ex.getMessage());
            String message = messageSource.getMessage("site.get.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/check/domain/{domain}")
    public ResponseEntity<ApiResponse<Boolean>> checkDomainAvailability(
            @PathVariable @Valid String domain,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            String sanitizedDomain = sanitizeInput(domain);
            if (sanitizedDomain == null || sanitizedDomain.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid domain");
            }

            boolean available = siteService.isDomainAvailable(sanitizedDomain);
            String messageKey = available ? "site.domain.available" : "site.domain.taken";
            String message = messageSource.getMessage(messageKey, new Object[] { domain },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, available));
        } catch (Exception ex) {
            log.error("Error checking domain availability for {}: {}", domain, ex.getMessage());
            String message = messageSource.getMessage("site.domain.check.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    // ========== Site Dashboard Endpoints ==========

    /**
     * Get site overview for dashboard.
     * Includes status, stats, recent activity, and available actions.
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<SiteOverviewResponse>> getOverview(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            SiteOverviewAppDto appDto = siteOverviewService.getOverview();
            SiteOverviewResponse response = toSiteOverviewResponse(appDto);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error getting site overview", ex);
            String message = messageSource.getMessage("site.overview.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            if (message.length() > 500)
                message = message.substring(0, 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    /**
     * Get technical settings for the site.
     * Includes domain info, robots.txt, verification codes, and scripts.
     */
    @GetMapping("/technical")
    public ResponseEntity<ApiResponse<SiteTechnicalResponse>> getTechnicalSettings(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            SiteTechnicalAppDto appDto = siteTechnicalService.getTechnicalSettings();
            SiteTechnicalResponse response = toSiteTechnicalResponse(appDto);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error getting technical settings", ex);
            String message = messageSource.getMessage("site.technical.get.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            if (message.length() > 500)
                message = message.substring(0, 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    /**
     * Update technical settings for the site.
     * Only provided fields will be updated (PATCH semantics).
     */
    @PatchMapping("/technical")
    public ResponseEntity<ApiResponse<SiteTechnicalResponse>> patchTechnicalSettings(
            @Valid @RequestBody SiteTechnicalPatchRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            SiteTechnicalAppDto appDto = siteTechnicalService.patchTechnicalSettings(request);
            SiteTechnicalResponse response = toSiteTechnicalResponse(appDto);
            String message = messageSource.getMessage("site.technical.updated.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception ex) {
            log.error("Error updating technical settings", ex);
            String message = messageSource.getMessage("site.technical.update.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            if (message.length() > 500)
                message = message.substring(0, 500);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    // ========== Security Settings Endpoints ==========

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @GetMapping("/security")
    public ResponseEntity<ApiResponse<SecuritySettingsResponse>> getSecuritySettings(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            SecuritySettingsResult result = securitySettingsService.getSecuritySettings();
            return ResponseEntity.ok(ApiResponse.success(toSecuritySettingsResponse(result)));
        } catch (Exception ex) {
            log.error("Error getting security settings", ex);
            String message = messageSource.getMessage("site.security.get.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            if (message.length() > 500)
                message = message.substring(0, 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PatchMapping("/security")
    public ResponseEntity<ApiResponse<SecuritySettingsResponse>> updateSecuritySettings(
            @Valid @RequestBody UpdateSecuritySettingsRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            SecuritySettingsResult result = securitySettingsService.updateTwoFactorPolicy(request.twoFactorPolicy());
            String message = messageSource.getMessage("site.security.updated.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, toSecuritySettingsResponse(result)));
        } catch (Exception ex) {
            log.error("Error updating security settings", ex);
            String message = messageSource.getMessage("site.security.update.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            if (message.length() > 500)
                message = message.substring(0, 500);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    private SecuritySettingsResponse toSecuritySettingsResponse(SecuritySettingsResult result) {
        return SecuritySettingsResponse.of(result.policy(), result.policyDescription());
    }

    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim()
                .replaceAll("[<>\"'&]", "")
                .toLowerCase();
    }

    // Mappers

    private SiteOverviewResponse toSiteOverviewResponse(SiteOverviewAppDto dto) {
        if (dto == null)
            return null;

        // Map Status
        SiteOverviewResponse.SiteStatusDto status = null;
        if (dto.status() != null) {
            SiteOverviewResponse.UserDto lastUpdatedBy = null;
            if (dto.status().lastUpdatedBy() != null) {
                lastUpdatedBy = new SiteOverviewResponse.UserDto(
                        dto.status().lastUpdatedBy().id(),
                        dto.status().lastUpdatedBy().email(),
                        dto.status().lastUpdatedBy().displayName());
            }
            status = new SiteOverviewResponse.SiteStatusDto(
                    dto.status().state(),
                    dto.status().publishedAt(),
                    dto.status().lastUpdatedAt(),
                    lastUpdatedBy);
        }

        // Map Stats
        SiteOverviewResponse.SiteStatsDto stats = null;
        if (dto.stats() != null) {
            SiteOverviewResponse.EntityStatsDto pageStats = new SiteOverviewResponse.EntityStatsDto(
                    dto.stats().pages().total(),
                    dto.stats().pages().published(),
                    dto.stats().pages().draft(),
                    Math.toIntExact(dto.stats().pages().weeklyChange()));
            SiteOverviewResponse.EntityStatsDto componentStats = new SiteOverviewResponse.EntityStatsDto(
                    dto.stats().components().total(),
                    dto.stats().components().published(),
                    dto.stats().components().draft(),
                    Math.toIntExact(dto.stats().components().weeklyChange()));
            SiteOverviewResponse.MediaStatsDto mediaStats = new SiteOverviewResponse.MediaStatsDto(
                    dto.stats().media().totalCount(),
                    dto.stats().media().totalSizeMb(),
                    Math.toIntExact(dto.stats().media().dailyChange()));
            SiteOverviewResponse.EntityStatsDto productStats = new SiteOverviewResponse.EntityStatsDto(
                    dto.stats().products().total(),
                    dto.stats().products().published(),
                    dto.stats().products().draft(),
                    Math.toIntExact(dto.stats().products().weeklyChange()));
            stats = new SiteOverviewResponse.SiteStatsDto(pageStats, componentStats, mediaStats, productStats);
        }

        // Map Recent Activity
        List<SiteOverviewResponse.ActivityDto> recentActivity = null;
        if (dto.recentActivity() != null) {
            recentActivity = dto.recentActivity().stream().map(a -> {
                SiteOverviewResponse.UserDto user = null;
                if (a.user() != null) {
                    user = new SiteOverviewResponse.UserDto(a.user().id(), a.user().email(), a.user().displayName());
                }
                return new SiteOverviewResponse.ActivityDto(
                        a.id(), a.action(), a.entityType(), a.entityId(), a.entityName(), a.description(), user,
                        a.createdAt());
            }).collect(Collectors.toList());
        }

        // Map Actions
        SiteOverviewResponse.ActionsDto actions = null;
        if (dto.actions() != null) {
            actions = new SiteOverviewResponse.ActionsDto(
                    dto.actions().canPublish(),
                    dto.actions().canPreview(),
                    dto.actions().canEnableMaintenance(),
                    dto.actions().canDisableMaintenance(),
                    dto.actions().previewUrl());
        }

        return SiteOverviewResponse.builder()
                .status(status)
                .stats(stats)
                .recentActivity(recentActivity)
                .actions(actions)
                .build();
    }

    private SiteTechnicalResponse toSiteTechnicalResponse(SiteTechnicalAppDto dto) {
        if (dto == null)
            return null;

        SiteTechnicalResponse.SearchEngineDto searchEngine = null;
        if (dto.searchEngine() != null) {
            SiteTechnicalResponse.VerificationDto verification = null;
            if (dto.searchEngine().verification() != null) {
                verification = new SiteTechnicalResponse.VerificationDto(
                        dto.searchEngine().verification().google(),
                        dto.searchEngine().verification().bing(),
                        dto.searchEngine().verification().yandex());
            }
            searchEngine = new SiteTechnicalResponse.SearchEngineDto(
                    dto.searchEngine().robotsTxt(),
                    dto.searchEngine().sitemapEnabled(),
                    dto.searchEngine().indexingEnabled(),
                    verification);
        }

        SiteTechnicalResponse.ScriptsDto scripts = null;
        if (dto.scripts() != null) {
            scripts = new SiteTechnicalResponse.ScriptsDto(
                    dto.scripts().head(),
                    dto.scripts().bodyStart(),
                    dto.scripts().bodyEnd());
        }

        SiteTechnicalResponse.CookieConsentDto cookieConsent = null;
        if (dto.cookieConsent() != null) {
            cookieConsent = new SiteTechnicalResponse.CookieConsentDto(
                    dto.cookieConsent().enabled(),
                    dto.cookieConsent().text());
        }

        return SiteTechnicalResponse.builder()
                .searchEngine(searchEngine)
                .scripts(scripts)
                .cookieConsent(cookieConsent)
                .build();
    }
}
