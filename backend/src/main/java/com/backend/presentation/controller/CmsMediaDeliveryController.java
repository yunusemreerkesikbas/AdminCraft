package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.MediaContainerService;
import com.backend.application.service.MediaService;
import com.backend.domain.enums.MediaStatus;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.presentation.dto.response.MediaResponse;
import com.backend.shared.common.ApiResponse;
import com.google.common.util.concurrent.RateLimiter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Public CMS delivery controller for media assets.
 * No authentication required. Tenant resolution via X-Tenant-Subdomain header.
 * Rate limited: 100 req/min per tenant.
 */
@RestController
@RequestMapping("/cms/media")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "CMS Media Delivery", description = "Public endpoints for media delivery")
public class CmsMediaDeliveryController {

  private final MediaService mediaService;
  private final MediaContainerService containerService;
  private final TenantContext tenantContext;
  private final MessageSource messageSource;

  private static final int MAX_BATCH_SIZE = 50;
  private static final double PERMITS_PER_MINUTE = 100.0;
  private static final double PERMITS_PER_SECOND = PERMITS_PER_MINUTE / 60.0;
  private final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

  /**
   * Get public media by UID.
   * Optionally returns a specific format variant.
   */
  @GetMapping("/{uid}")
  @Operation(summary = "Get media by UID", description = "Retrieves a public media file by its UID. Optionally allows specifying a format variant.")
  public ResponseEntity<ApiResponse<MediaResponse>> getMediaByUid(
      @Parameter(description = "Media UID") @PathVariable String uid,
      @Parameter(description = "Optional format code (e.g., 'thumbnail')") @RequestParam(required = false) String format,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    Locale locale = Locale.forLanguageTag(acceptLanguage);
    ResponseEntity<ApiResponse<MediaResponse>> rateLimitResponse = checkRateLimit(locale);
    if (rateLimitResponse != null) {
      return rateLimitResponse;
    }

    setupMdc();
    log.debug("CMS media request: uid={}, format={}", uid, format);

    return mediaService.findByUid(uid)
        .filter(media -> Boolean.TRUE.equals(media.getIsPublic()))
        .filter(media -> media.getStatus() == MediaStatus.ACTIVE)
        .map(media -> {
          // If format requested, return variant from container
          if (format != null && !format.isBlank()) {
            return containerService.getVariant(media.getId(), format.toUpperCase())
                .map(variant -> ResponseEntity.ok(ApiResponse.success(MediaResponse.from(variant))))
                .orElseGet(() -> {
                  log.debug("Format variant not found, returning master: uid={}, format={}", uid, format);
                  return ResponseEntity.ok(ApiResponse.success(MediaResponse.from(media)));
                });
          }
          return ResponseEntity.ok(ApiResponse.success(MediaResponse.from(media)));
        })
        .orElseGet(() -> {
          log.debug("Media not found or not public: uid={}", uid);
          return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("Media not found"));
        });
  }

  /**
   * Get multiple media by UIDs (batch request).
   * Maximum 50 UIDs per request.
   * Uses batch query to avoid N+1.
   */
  @GetMapping
  @Operation(summary = "Get multiple media by UIDs", description = "Retrieves a list of public media files by their UIDs.")
  public ResponseEntity<ApiResponse<List<MediaResponse>>> getMediaByUids(
      @Parameter(description = "Comma-separated list of UIDs") @RequestParam @Size(min = 1, message = "At least one UID required") List<String> uids,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String acceptLanguage) {

    setupMdc();
    Locale locale = Locale.forLanguageTag(acceptLanguage);

    ResponseEntity<ApiResponse<List<MediaResponse>>> rateLimitResponse = checkRateLimit(locale);
    if (rateLimitResponse != null) {
      return rateLimitResponse;
    }

    if (uids.size() > MAX_BATCH_SIZE) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Maximum " + MAX_BATCH_SIZE + " UIDs allowed per request"));
    }

    log.debug("CMS batch media request: count={}", uids.size());

    // Use batch query to avoid N+1
    List<String> distinctUids = uids.stream().distinct().toList();
    List<MediaResponse> responses = mediaService.findByUids(distinctUids).stream()
        .filter(media -> Boolean.TRUE.equals(media.getIsPublic()))
        .filter(media -> media.getStatus() == MediaStatus.ACTIVE)
        .map(MediaResponse::from)
        .toList();

    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  private void setupMdc() {
    MDC.put("tenantId", String.valueOf(tenantContext.getTenantId()));
    MDC.put("correlationId", UUID.randomUUID().toString().substring(0, 8));
  }

  private <T> ResponseEntity<ApiResponse<T>> checkRateLimit(Locale locale) {
    String tenantId = tenantContext.getTenantId();
    if (tenantId == null) {
      return null;
    }

    RateLimiter rateLimiter = rateLimiters.computeIfAbsent(tenantId,
        id -> RateLimiter.create(PERMITS_PER_SECOND));

    if (!rateLimiter.tryAcquire()) {
      log.warn("CMS Media Delivery: Rate limit exceeded for tenant {}", tenantId);
      String message = messageSource.getMessage("cms.rate.limit.exceeded", null, "Rate limit exceeded", locale);
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
          ApiResponse.error(message));
    }

    return null;
  }
}
