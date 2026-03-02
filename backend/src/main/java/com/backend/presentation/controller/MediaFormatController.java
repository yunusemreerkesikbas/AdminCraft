package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.MediaFormatService;
import com.backend.domain.entity.MediaFormat;
import com.backend.domain.enums.CropMode;
import com.backend.presentation.dto.request.MediaFormatCreateRequest;
import com.backend.presentation.dto.request.MediaFormatUpdateRequest;
import com.backend.presentation.dto.response.MediaFormatResponse;
import com.backend.shared.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for managing media formats.
 * Handles format definitions for image variant generation.
 * System formats are protected and cannot be modified or deleted.
 */
@RestController
@RequestMapping("/media/formats")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
@Tag(name = "Media Format", description = "Endpoints for managing media formats (image variants)")
public class MediaFormatController {

  private final MediaFormatService formatService;
  private final MessageSource messageSource;

  /**
   * Get all formats (system and custom).
   */
  @GetMapping
  @Operation(summary = "Get all formats", description = "Retrieves all media formats, including system and custom defined ones.")
  public ResponseEntity<ApiResponse<List<MediaFormatResponse>>> getAllFormats(
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      List<MediaFormat> formats = formatService.findAll();
      List<MediaFormatResponse> responseList = formats.stream()
          .map(MediaFormatResponse::from)
          .toList();
      return ResponseEntity.ok(ApiResponse.success(responseList));
    } catch (Exception ex) {
      log.error("Error getting all formats: {}", ex.getMessage());
      String message = messageSource.getMessage("media.format.list.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Get system formats only.
   */
  @GetMapping("/system")
  @Operation(summary = "Get system formats", description = "Retrieves only system-defined media formats (read-only).")
  public ResponseEntity<ApiResponse<List<MediaFormatResponse>>> getSystemFormats(
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      List<MediaFormat> formats = formatService.findSystemFormats();
      List<MediaFormatResponse> responseList = formats.stream()
          .map(MediaFormatResponse::from)
          .toList();
      return ResponseEntity.ok(ApiResponse.success(responseList));
    } catch (Exception ex) {
      log.error("Error getting system formats: {}", ex.getMessage());
      String message = messageSource.getMessage("media.format.list.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Get custom formats only.
   */
  @GetMapping("/custom")
  @Operation(summary = "Get custom formats", description = "Retrieves only custom-defined media formats.")
  public ResponseEntity<ApiResponse<List<MediaFormatResponse>>> getCustomFormats(
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      List<MediaFormat> formats = formatService.findCustomFormats();
      List<MediaFormatResponse> responseList = formats.stream()
          .map(MediaFormatResponse::from)
          .toList();
      return ResponseEntity.ok(ApiResponse.success(responseList));
    } catch (Exception ex) {
      log.error("Error getting custom formats: {}", ex.getMessage());
      String message = messageSource.getMessage("media.format.list.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Get format by ID.
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get format by ID", description = "Retrieves a specific media format by its ID.")
  public ResponseEntity<ApiResponse<MediaFormatResponse>> getFormatById(
      @Parameter(description = "Format ID") @PathVariable @Valid @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      return formatService.findById(id)
          .map(format -> ResponseEntity.ok(ApiResponse.success(MediaFormatResponse.from(format))))
          .orElseGet(() -> {
            String message = messageSource.getMessage("media.format.not.found",
                new Object[] { id }, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
          });
    } catch (Exception ex) {
      log.error("Error getting format {}: {}", id, ex.getMessage());
      String message = messageSource.getMessage("media.format.get.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Get format by UID.
   */
  @GetMapping("/uid/{uid}")
  @Operation(summary = "Get format by UID", description = "Retrieves a specific media format by its UID (code).")
  public ResponseEntity<ApiResponse<MediaFormatResponse>> getFormatByUid(
      @Parameter(description = "Format UID/Code") @PathVariable String uid,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      return formatService.findByUid(uid)
          .map(format -> ResponseEntity.ok(ApiResponse.success(MediaFormatResponse.from(format))))
          .orElseGet(() -> {
            String message = messageSource.getMessage("media.format.not.found",
                new Object[] { uid }, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
          });
    } catch (Exception ex) {
      log.error("Error getting format by UID {}: {}", uid, ex.getMessage());
      String message = messageSource.getMessage("media.format.get.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Create a new custom format.
   */
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping
  @Operation(summary = "Create custom format", description = "Creates a new custom media format.")
  public ResponseEntity<ApiResponse<MediaFormatResponse>> createFormat(
      @RequestBody @Valid MediaFormatCreateRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      Long userId = getCurrentUserId();
      CropMode cropMode = request.cropMode() != null ? request.cropMode() : CropMode.FIT;
      Integer quality = request.quality() != null ? request.quality() : 80;

      MediaFormat format = formatService.create(
          request.code(),
          request.name(),
          request.width(),
          request.height(),
          quality,
          cropMode,
          userId);
      String message = messageSource.getMessage("media.format.create.success", null,
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success(message, MediaFormatResponse.from(format)));
    } catch (IllegalArgumentException ex) {
      log.warn("Format creation validation error: {}", ex.getMessage());
      String message = messageSource.getMessage("media.format.create.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error creating format: {}", ex.getMessage());
      String message = messageSource.getMessage("media.format.create.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Update a custom format. System formats cannot be updated.
   */
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PutMapping("/{id}")
  @Operation(summary = "Update custom format", description = "Updates a custom media format. System formats cannot be updated.")
  public ResponseEntity<ApiResponse<MediaFormatResponse>> updateFormat(
      @Parameter(description = "Format ID") @PathVariable @Valid @NotNull @Min(1) Long id,
      @RequestBody @Valid MediaFormatUpdateRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      Long userId = getCurrentUserId();
      MediaFormat format = formatService.update(
          id,
          request.name(),
          request.width(),
          request.height(),
          request.quality(),
          request.cropMode(),
          userId);
      String message = messageSource.getMessage("media.format.update.success", null,
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.ok(ApiResponse.success(message, MediaFormatResponse.from(format)));
    } catch (IllegalArgumentException ex) {
      log.warn("Format update validation error: {}", ex.getMessage());
      String message = messageSource.getMessage("media.format.update.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (IllegalStateException ex) {
      log.warn("Format update state error (system format): {}", ex.getMessage());
      String message = messageSource.getMessage("media.format.system.protected", null,
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error updating format {}: {}", id, ex.getMessage());
      String message = messageSource.getMessage("media.format.update.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Delete a custom format. System formats cannot be deleted.
   */
  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete custom format", description = "Deletes a custom media format. System formats cannot be deleted.")
  public ResponseEntity<ApiResponse<Void>> deleteFormat(
      @Parameter(description = "Format ID") @PathVariable @Valid @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      formatService.delete(id);
      String message = messageSource.getMessage("media.format.delete.success", null,
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.ok(ApiResponse.success(message, null));
    } catch (IllegalArgumentException ex) {
      log.warn("Format delete validation error: {}", ex.getMessage());
      String message = messageSource.getMessage("media.format.delete.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (IllegalStateException ex) {
      log.warn("Format delete state error (system format): {}", ex.getMessage());
      String message = messageSource.getMessage("media.format.system.protected", null,
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error deleting format {}: {}", id, ex.getMessage());
      String message = messageSource.getMessage("media.format.delete.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Get current user ID from security context.
   */
  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof Long userId) {
      return userId;
    }
    // This should never happen as endpoints require TENANT_ADMIN role
    throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
  }
}
