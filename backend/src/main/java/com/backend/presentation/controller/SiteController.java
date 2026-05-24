package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import com.backend.application.dto.TwoFactorPolicyChangeRequestResult;
import com.backend.application.dto.UpdateSecuritySettingsCommand;
import com.backend.application.dto.request.CreateSiteRequest;
import com.backend.application.dto.request.SiteTechnicalPatchRequest;
import com.backend.application.dto.request.UpdateSiteRequest;
import com.backend.application.dto.response.SiteAnalyticsSummaryAppDto;
import com.backend.application.dto.response.SiteInsightsSummaryAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto;
import com.backend.application.dto.response.SiteTechnicalAppDto;
import com.backend.application.service.SecuritySettingsService;
import com.backend.application.service.SiteAnalyticsService;
import com.backend.application.service.SiteInsightsService;
import com.backend.application.service.SiteOverviewService;
import com.backend.application.service.SiteService;
import com.backend.application.service.SiteTechnicalService;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.InvalidCredentialsException;
import com.backend.domain.exception.InvalidTokenException;
import com.backend.domain.exception.OtpRateLimitExceededException;
import com.backend.domain.exception.TwoFactorPolicyVerificationRequiredException;
import com.backend.presentation.dto.request.ConfirmTwoFactorPolicyChangeRequest;
import com.backend.presentation.dto.request.RequestTwoFactorPolicyChangeRequest;
import com.backend.presentation.dto.request.UpdateSecuritySettingsRequest;
import com.backend.presentation.dto.response.TwoFactorPolicyChangeRequestResponse;
import com.backend.presentation.dto.response.SecuritySettingsResponse;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SiteAnalyticsSummaryResponse;
import com.backend.presentation.dto.response.SiteInsightsSummaryResponse;
import com.backend.presentation.dto.response.SiteOverviewResponse;
import com.backend.presentation.dto.response.SiteResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.presentation.dto.response.SiteTechnicalResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.RequestUtils;
import com.backend.shared.common.SecurityHelper;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/sites")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
@Tag(name = "Sites", description = "Site management and dashboard endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SiteController {
    private static final int ACTIVITY_TREND_WINDOW_DAYS = 30;


    private final SiteService siteService;
    private final SiteOverviewService siteOverviewService;
    private final SiteAnalyticsService siteAnalyticsService;
    private final SiteInsightsService siteInsightsService;
    private final SiteTechnicalService siteTechnicalService;
    private final SecuritySettingsService securitySettingsService;
    private final MessageSource messageSource;
    private final SecurityHelper securityHelper;

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping
    @Operation(summary = "Create site", description = "Creates a new tenant site")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Site created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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
    @Operation(summary = "Get site by ID", description = "Returns site details by identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Site not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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
            String message = messageSource.getMessage("site.get.error", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    @Operation(summary = "List sites", description = "Returns all tenant sites")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sites listed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getAllSites(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Language displayLanguage = Language.fromCodeOrDefault(languageCode);
            List<SiteResponse> sites = siteService.getAllSites(displayLanguage);
            return ResponseEntity.ok(ApiResponse.success(sites));
        } catch (Exception ex) {
            log.error("Error getting all sites: {}", ex.getMessage());
            String message = messageSource.getMessage("site.list.error", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update site", description = "Updates site metadata")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete site", description = "Deletes a site by identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Delete failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> deleteSite(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            siteService.deleteSite(id);
            String message = messageSource.getMessage("site.delete.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting site {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("site.delete.error", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish site", description = "Publishes site content")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site published"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Publish failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/{id}/unpublish")
    @Operation(summary = "Unpublish site", description = "Unpublishes site content")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site unpublished"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Unpublish failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/{id}/maintenance")
    @Operation(summary = "Enable maintenance mode", description = "Enables maintenance mode for site")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Maintenance enabled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Operation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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
            String errorMessage = messageSource.getMessage("site.maintenance.error", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(errorMessage));
        }
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @DeleteMapping("/{id}/maintenance")
    @Operation(summary = "Disable maintenance mode", description = "Disables maintenance mode for site")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Maintenance disabled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Operation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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
            String errorMessage = messageSource.getMessage("site.maintenance.error", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(errorMessage));
        }
    }

    @GetMapping("/domain/{domain}")
    @Operation(summary = "Get site by domain", description = "Returns site by domain value")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Site not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid domain"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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
        } catch (IllegalArgumentException ex) {
            String message = messageSource.getMessage("site.get.error", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.badRequest().body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error getting site by domain {}: {}", domain, ex.getMessage());
            String message = messageSource.getMessage("site.get.error", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/check/domain/{domain}")
    @Operation(summary = "Check domain availability", description = "Checks if a domain is available for site binding")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Availability returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid domain"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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
    @Operation(summary = "Get site overview", description = "Returns dashboard overview metrics and actions")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Overview returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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

    @GetMapping("/analytics/summary")
    @Operation(summary = "Get analytics summary", description = "Returns site analytics summary")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Summary returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<SiteAnalyticsSummaryResponse>> getAnalyticsSummary() {
        try {
            SiteAnalyticsSummaryAppDto appDto = siteAnalyticsService.getSummary();
            return ResponseEntity.ok(ApiResponse.success(toSiteAnalyticsSummaryResponse(appDto)));
        } catch (Exception ex) {
            log.error("Error getting site analytics summary", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get site analytics summary"));
        }
    }

    @GetMapping("/insights/summary")
    @Operation(summary = "Get insights summary", description = "Returns combined SEO and performance insights summary")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Summary returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<SiteInsightsSummaryResponse>> getInsightsSummary() {
        try {
            SiteInsightsSummaryAppDto appDto = siteInsightsService.getSummary();
            return ResponseEntity.ok(ApiResponse.success(toSiteInsightsSummaryResponse(appDto)));
        } catch (Exception ex) {
            log.error("Error getting site insights summary", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get site insights summary"));
        }
    }

    /**
     * Get technical settings for the site.
     * Includes domain info, robots.txt, and verification codes.
     */
    @GetMapping("/technical")
    @Operation(summary = "Get technical settings", description = "Returns site technical and robots settings")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settings returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<SiteTechnicalResponse>> getTechnicalSettings(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            SiteTechnicalAppDto appDto = siteTechnicalService.getTechnicalSettings();
            SiteTechnicalResponse response = toSiteTechnicalResponse(appDto);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error getting technical settings", ex);
            String message = messageSource.getMessage("site.technical.get.error", null,
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
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PatchMapping("/technical")
    @Operation(summary = "Patch technical settings", description = "Updates technical settings with patch semantics")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settings updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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
            String message = messageSource.getMessage("site.technical.update.error", null,
                    Locale.forLanguageTag(languageCode));
            if (message.length() > 500)
                message = message.substring(0, 500);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    // ========== Security Settings Endpoints ==========

    @GetMapping("/security")
    @Operation(summary = "Get security settings", description = "Returns current security settings")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settings returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<SecuritySettingsResponse>> getSecuritySettings(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            SecuritySettingsResult result = securitySettingsService.getSecuritySettings();
            return ResponseEntity.ok(ApiResponse.success(toSecuritySettingsResponse(result)));
        } catch (Exception ex) {
            log.error("Error getting security settings", ex);
            String message = messageSource.getMessage("site.security.get.error", null,
                    Locale.forLanguageTag(languageCode));
            if (message.length() > 500)
                message = message.substring(0, 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PatchMapping("/security")
    @Operation(summary = "Update security settings", description = "Updates tenant security policy configuration")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settings updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<SecuritySettingsResponse>> updateSecuritySettings(
            @Valid @RequestBody UpdateSecuritySettingsRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            var command = new UpdateSecuritySettingsCommand(request.twoFactorPolicy());

            SecuritySettingsResult result = securitySettingsService.updateSecuritySettings(command);
            String message = messageSource.getMessage("site.security.updated.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, toSecuritySettingsResponse(result)));
        } catch (TwoFactorPolicyVerificationRequiredException ex) {
            String message = messageSource.getMessage("site.security.verification.required", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error updating security settings", ex);
            String message = messageSource.getMessage("site.security.update.error", null,
                    Locale.forLanguageTag(languageCode));
            if (message.length() > 500)
                message = message.substring(0, 500);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/security/two-factor/request-change")
    @Operation(summary = "Request two-factor policy change", description = "Sends an operation OTP to the acting admin email before applying a policy change")
    public ResponseEntity<ApiResponse<TwoFactorPolicyChangeRequestResponse>> requestTwoFactorPolicyChange(
            @Valid @RequestBody RequestTwoFactorPolicyChangeRequest request,
            HttpServletRequest httpRequest,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            String ipAddress = RequestUtils.getClientIpAddress(httpRequest);
            String userAgent = RequestUtils.getUserAgent(httpRequest);
            TwoFactorPolicyChangeRequestResult result = securitySettingsService.requestTwoFactorPolicyChange(
                    request.twoFactorPolicy(),
                    ipAddress,
                    userAgent);
            String message = messageSource.getMessage("site.security.twoFactor.otp.sent", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, TwoFactorPolicyChangeRequestResponse.from(result)));
        } catch (OtpRateLimitExceededException ex) {
            String message = messageSource.getMessage("auth.otp.rateLimit", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error(message));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error requesting two-factor policy change", ex);
            String message = messageSource.getMessage("site.security.twoFactor.otp.request.error", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/security/two-factor/confirm-change")
    @Operation(summary = "Confirm two-factor policy change", description = "Verifies the operation OTP and applies the pending two-factor policy")
    public ResponseEntity<ApiResponse<SecuritySettingsResponse>> confirmTwoFactorPolicyChange(
            @Valid @RequestBody ConfirmTwoFactorPolicyChangeRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            SecuritySettingsResult result = securitySettingsService.confirmTwoFactorPolicyChange(
                    request.pendingChangeId(),
                    request.otpCode());
            String message = messageSource.getMessage("site.security.updated.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, toSecuritySettingsResponse(result)));
        } catch (InvalidTokenException ex) {
            String message = messageSource.getMessage("site.security.twoFactor.otp.invalid", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        } catch (InvalidCredentialsException ex) {
            String message = messageSource.getMessage("auth.otp.invalid", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error confirming two-factor policy change", ex);
            String message = messageSource.getMessage("site.security.update.error", null,
                    Locale.forLanguageTag(languageCode));
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

    @GetMapping("/activity/trend")
    @Operation(summary = "Get activity trend", description = "Returns paginated activity trend for site dashboard")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trend returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid query parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PageableResponse<SiteOverviewResponse.ActivityTrendDayResponse>>> getActivityTrend(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "7") @Min(1) @Max(ACTIVITY_TREND_WINDOW_DAYS) int size,
            @RequestParam(required = false) String sort,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            String effectiveSort = SortParseUtil.getEffectiveSortCode(
                    sort,
                    SortableFieldsConfig.SITE_ACTIVITY_TREND_DEFAULT_SORT);
            Sort sortObj = SortParseUtil.parse(
                    effectiveSort,
                    SortableFieldsConfig.SITE_ACTIVITY_TREND_ALLOWED_FIELDS,
                    SortableFieldsConfig.SITE_ACTIVITY_TREND_DEFAULT_SORT);
            Pageable pageable = PageRequest.of(page, size, sortObj);

            Page<SiteOverviewAppDto.ActivityTrendDayAppDto> trendPage = siteOverviewService.getActivityTrend(
                    pageable,
                    ACTIVITY_TREND_WINDOW_DAYS);
            List<SiteOverviewResponse.ActivityTrendDayResponse> days = trendPage.getContent().stream()
                    .map(d -> new SiteOverviewResponse.ActivityTrendDayResponse(
                            d.date(), d.total(), d.created(), d.updated(), d.published()))
                    .collect(Collectors.toList());
            SortConfig sortConfig = SortConfig.of(
                    effectiveSort,
                    SortableFieldsConfig.SITE_ACTIVITY_TREND_SORT_OPTIONS);
            PageableResponse<SiteOverviewResponse.ActivityTrendDayResponse> response = PageableResponse.fromMapped(
                    trendPage,
                    days,
                    sortConfig);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException ex) {
            String message = messageSource.getMessage(
                    "site.activity.trend.sort.invalid",
                    new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error getting activity trend", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageSource.getMessage(
                            "site.activity.trend.error", null, Locale.forLanguageTag(languageCode))));
        }
    }

    @GetMapping("/activity")
    @Operation(summary = "Get activity feed", description = "Returns paginated recent site activity")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Activity returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid query parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PageableResponse<SiteOverviewResponse.ActivityDto>>> getRecentActivity(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String sort,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            String effectiveSort = SortParseUtil.getEffectiveSortCode(
                    sort,
                    SortableFieldsConfig.SITE_ACTIVITY_DEFAULT_SORT);
            Sort sortObj = SortParseUtil.parse(
                    effectiveSort,
                    SortableFieldsConfig.SITE_ACTIVITY_ALLOWED_FIELDS,
                    SortableFieldsConfig.SITE_ACTIVITY_DEFAULT_SORT);
            Pageable pageable = PageRequest.of(page, size, sortObj);

            Page<SiteOverviewAppDto.ActivityAppDto> activityPage = siteOverviewService.getRecentActivityPage(pageable);
            List<SiteOverviewResponse.ActivityDto> content = activityPage.getContent().stream()
                    .map(this::toActivityResponse)
                    .collect(Collectors.toList());
            SortConfig sortConfig = SortConfig.of(
                    effectiveSort,
                    SortableFieldsConfig.SITE_ACTIVITY_SORT_OPTIONS);
            PageableResponse<SiteOverviewResponse.ActivityDto> response = PageableResponse.fromMapped(
                    activityPage,
                    content,
                    sortConfig);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException ex) {
            String message = messageSource.getMessage(
                    "site.activity.sort.invalid",
                    new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error getting recent activity page", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get activity feed"));
        }
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
	    SiteOverviewResponse.EntityStatsDto productStats = null;
	    if (dto.stats().products() != null) {
		productStats = new SiteOverviewResponse.EntityStatsDto(
			dto.stats().products().total(),
			dto.stats().products().published(),
			dto.stats().products().draft(),
			Math.toIntExact(dto.stats().products().weeklyChange()));
	    }
            stats = new SiteOverviewResponse.SiteStatsDto(pageStats, componentStats, mediaStats, productStats);
        }

        // Map Recent Activity
        List<SiteOverviewResponse.ActivityDto> recentActivity = null;
        if (dto.recentActivity() != null) {
            recentActivity = dto.recentActivity().stream()
                    .map(this::toActivityResponse)
                    .collect(Collectors.toList());
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

        SiteOverviewResponse.SpotlightDto spotlight = null;
        if (dto.spotlight() != null) {
            SiteOverviewResponse.SpotlightStatusDto spotlightStatus = dto.spotlight().status() == null
                    ? null
                    : new SiteOverviewResponse.SpotlightStatusDto(
                            dto.spotlight().status().tone(),
                            dto.spotlight().status().code());

            spotlight = new SiteOverviewResponse.SpotlightDto(
                    dto.spotlight().operationalScore(),
                    spotlightStatus,
                    dto.spotlight().contextCards().stream()
                            .map(card -> new SiteOverviewResponse.SpotlightContextCardDto(
                                    card.id(),
                                    card.icon(),
                                    card.progress(),
                                    card.tone(),
                                    card.valueCode(),
                                    card.detailCode(),
                                    card.detailDate()))
                            .collect(Collectors.toList()),
                    dto.spotlight().recommendations().stream()
                            .map(recommendation -> new SiteOverviewResponse.SpotlightRecommendationDto(
                                    recommendation.id(),
                                    recommendation.icon(),
                                    recommendation.tone(),
                                    recommendation.count()))
                            .collect(Collectors.toList()));
        }

        return new SiteOverviewResponse(dto.id(), status, stats, recentActivity, actions, spotlight);
    }

    private SiteOverviewResponse.ActivityDto toActivityResponse(SiteOverviewAppDto.ActivityAppDto dto) {
        SiteOverviewResponse.UserDto user = null;
        if (dto.user() != null) {
            user = new SiteOverviewResponse.UserDto(dto.user().id(), dto.user().email(), dto.user().displayName());
        }
        return new SiteOverviewResponse.ActivityDto(
                dto.id(),
                dto.action(),
                dto.entityType(),
                dto.entityId(),
                dto.entityName(),
                dto.description(),
                user,
                dto.createdAt());
    }

    private SiteTechnicalResponse toSiteTechnicalResponse(SiteTechnicalAppDto dto) {
        if (dto == null)
            return null;

        SiteTechnicalResponse.SearchEngineDto searchEngine = null;
        if (dto.searchEngine() != null) {
            searchEngine = new SiteTechnicalResponse.SearchEngineDto(
                    dto.searchEngine().robotsTxt(),
                    dto.searchEngine().sitemapEnabled(),
                    dto.searchEngine().indexingEnabled());
        }

        SiteTechnicalResponse.CookieConsentDto cookieConsent = null;
        if (dto.cookieConsent() != null) {
            cookieConsent = new SiteTechnicalResponse.CookieConsentDto(
                    dto.cookieConsent().enabled(),
                    dto.cookieConsent().texts());
        }

        return SiteTechnicalResponse.builder()
                .searchEngine(searchEngine)
                .cookieConsent(cookieConsent)
                .build();
    }

    private SiteAnalyticsSummaryResponse toSiteAnalyticsSummaryResponse(
            SiteAnalyticsSummaryAppDto dto
    ) {
        if (dto == null) {
            return null;
        }

        return new SiteAnalyticsSummaryResponse(
                dto.status(),
                dto.propertyId(),
                dto.range(),
                dto.cards().stream()
                        .map(card -> new SiteAnalyticsSummaryResponse.MetricCardResponse(
                                card.metric(),
                                card.value(),
                                card.previousValue(),
                                card.deltaPercentage(),
                                card.deltaDirection()))
                        .toList(),
                dto.trend().stream()
                        .map(point -> new SiteAnalyticsSummaryResponse.TrendPointResponse(
                                point.date(),
                                point.value()))
                        .toList(),
                dto.lastSyncedAt());
    }

    private SiteInsightsSummaryResponse toSiteInsightsSummaryResponse(
            SiteInsightsSummaryAppDto dto
    ) {
        if (dto == null) {
            return null;
        }

        SiteInsightsSummaryResponse.InspectionResponse inspection = dto.seo().inspection() == null
                ? null
                : new SiteInsightsSummaryResponse.InspectionResponse(
                        dto.seo().inspection().verdict(),
                        dto.seo().inspection().coverageState(),
                        dto.seo().inspection().robotsTxtState(),
                        dto.seo().inspection().indexingState(),
                        dto.seo().inspection().pageFetchState(),
                        dto.seo().inspection().lastCrawlTime(),
                        dto.seo().inspection().googleCanonical(),
                        dto.seo().inspection().userCanonical(),
                        dto.seo().inspection().sitemaps());

        return new SiteInsightsSummaryResponse(
                dto.resolvedUrl(),
                dto.resolvedOrigin(),
                dto.lastSyncedAt(),
                new SiteInsightsSummaryResponse.SeoResponse(
                        dto.seo().status(),
                        dto.seo().propertyUrl(),
                        dto.seo().range(),
                        dto.seo().cards().stream()
                                .map(card -> new SiteInsightsSummaryResponse.MetricCardResponse(
                                        card.metric(),
                                        card.value(),
                                        card.previousValue(),
                                        card.deltaPercentage(),
                                        card.deltaDirection()))
                                .toList(),
                        dto.seo().trend().stream()
                                .map(point -> new SiteInsightsSummaryResponse.SeoTrendPointResponse(
                                        point.date(),
                                        point.clicks(),
                                        point.impressions()))
                                .toList(),
                        inspection,
                        dto.seo().lastSyncedAt()),
                new SiteInsightsSummaryResponse.PerformanceResponse(
                        dto.performance().status(),
                        dto.performance().targetScope(),
                        dto.performance().target(),
                        dto.performance().formFactor(),
                        dto.performance().score() == null
                                ? null
                                : new SiteInsightsSummaryResponse.ScoreResponse(
                                        dto.performance().score().value(),
                                        dto.performance().score().label()),
                        dto.performance().metrics().stream()
                                .map(metric -> new SiteInsightsSummaryResponse.PerformanceMetricResponse(
                                        metric.metric(),
                                        metric.value(),
                                        metric.displayValue(),
                                        metric.assessment()))
                                .toList(),
                        dto.performance().trend().stream()
                                .map(point -> new SiteInsightsSummaryResponse.PerformanceTrendPointResponse(
                                        point.startDate(),
                                        point.endDate(),
                                        point.lcp(),
                                        point.inp(),
                                        point.cls(),
                                        point.ttfb()))
                                .toList(),
                        dto.performance().lastSyncedAt()));
    }
}
