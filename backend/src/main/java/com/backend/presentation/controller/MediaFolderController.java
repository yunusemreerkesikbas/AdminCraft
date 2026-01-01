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

import com.backend.application.service.MediaFolderService;
import com.backend.application.service.MediaService;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.MediaFolder;
import com.backend.presentation.dto.request.MediaFolderCreateRequest;
import com.backend.presentation.dto.request.MediaFolderMoveRequest;
import com.backend.presentation.dto.request.MediaFolderUpdateRequest;
import com.backend.presentation.dto.response.MediaFolderResponse;
import com.backend.presentation.dto.response.MediaResponse;
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
 * Controller for managing media folders.
 * Handles hierarchical folder organization for media files.
 */
@RestController
@RequestMapping("/media/folders")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Media Folder", description = "Endpoints for managing media folder hierarchy")
public class MediaFolderController {

  private final MediaFolderService folderService;
  private final MediaService mediaService;
  private final MessageSource messageSource;

  /**
   * Get all folders.
   */
  @GetMapping
  @Operation(summary = "Get all folders", description = "Retrieves a flat list of all media folders.")
  public ResponseEntity<ApiResponse<List<MediaFolderResponse>>> getAllFolders(
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      List<MediaFolder> folders = folderService.findAll();
      List<MediaFolderResponse> responseList = folders.stream()
          .map(MediaFolderResponse::from)
          .toList();
      return ResponseEntity.ok(ApiResponse.success(responseList));
    } catch (Exception ex) {
      log.error("Error getting all folders: {}", ex.getMessage());
      String message = messageSource.getMessage("media.folder.list.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Get root folders only.
   */
  @GetMapping("/roots")
  @Operation(summary = "Get root folders", description = "Retrieves only top-level folders (folders with no parent).")
  public ResponseEntity<ApiResponse<List<MediaFolderResponse>>> getRootFolders(
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      List<MediaFolder> folders = folderService.findRootFolders();
      List<MediaFolderResponse> responseList = folders.stream()
          .map(MediaFolderResponse::from)
          .toList();
      return ResponseEntity.ok(ApiResponse.success(responseList));
    } catch (Exception ex) {
      log.error("Error getting root folders: {}", ex.getMessage());
      String message = messageSource.getMessage("media.folder.list.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Get folder by ID.
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get folder by ID", description = "Retrieves a specific media folder by its ID.")
  public ResponseEntity<ApiResponse<MediaFolderResponse>> getFolderById(
      @Parameter(description = "Folder ID") @PathVariable @Valid @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      return folderService.findById(id)
          .map(folder -> ResponseEntity.ok(ApiResponse.success(MediaFolderResponse.from(folder))))
          .orElseGet(() -> {
            String message = messageSource.getMessage("media.folder.not.found",
                new Object[] { id }, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
          });
    } catch (Exception ex) {
      log.error("Error getting folder {}: {}", id, ex.getMessage());
      String message = messageSource.getMessage("media.folder.get.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Get folder by UID.
   */
  @GetMapping("/uid/{uid}")
  @Operation(summary = "Get folder by UID", description = "Retrieves a specific media folder by its UID.")
  public ResponseEntity<ApiResponse<MediaFolderResponse>> getFolderByUid(
      @Parameter(description = "Folder UID") @PathVariable String uid,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      return folderService.findByUid(uid)
          .map(folder -> ResponseEntity.ok(ApiResponse.success(MediaFolderResponse.from(folder))))
          .orElseGet(() -> {
            String message = messageSource.getMessage("media.folder.not.found",
                new Object[] { uid }, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
          });
    } catch (Exception ex) {
      log.error("Error getting folder by UID {}: {}", uid, ex.getMessage());
      String message = messageSource.getMessage("media.folder.get.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Get child folders of a parent.
   */
  @GetMapping("/{id}/children")
  @Operation(summary = "Get child folders", description = "Retrieves immediate children of a specific folder.")
  public ResponseEntity<ApiResponse<List<MediaFolderResponse>>> getChildren(
      @Parameter(description = "Parent Folder ID") @PathVariable @Valid @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      List<MediaFolder> children = folderService.findChildren(id);
      List<MediaFolderResponse> responseList = children.stream()
          .map(MediaFolderResponse::from)
          .toList();
      return ResponseEntity.ok(ApiResponse.success(responseList));
    } catch (Exception ex) {
      log.error("Error getting children for folder {}: {}", id, ex.getMessage());
      String message = messageSource.getMessage("media.folder.list.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Get media files in a folder.
   */
  @GetMapping("/{id}/media")
  @Operation(summary = "Get media in folder", description = "Retrieves all media files directly within a specific folder.")
  public ResponseEntity<ApiResponse<List<MediaResponse>>> getMediaByFolder(
      @Parameter(description = "Folder ID") @PathVariable @Valid @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      List<Media> mediaList = mediaService.findByFolderId(id);
      List<MediaResponse> responseList = mediaList.stream()
          .map(MediaResponse::from)
          .toList();
      return ResponseEntity.ok(ApiResponse.success(responseList));
    } catch (Exception ex) {
      log.error("Error getting media for folder {}: {}", id, ex.getMessage());
      String message = messageSource.getMessage("media.list.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Create a new folder.
   */
  @PostMapping
  @Operation(summary = "Create folder", description = "Creates a new media folder.")
  public ResponseEntity<ApiResponse<MediaFolderResponse>> createFolder(
      @RequestBody @Valid MediaFolderCreateRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      Long userId = getCurrentUserId();
      MediaFolder folder = folderService.create(
          request.code(),
          request.name(),
          request.parentId(),
          userId);
      String message = messageSource.getMessage("media.folder.create.success", null,
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success(message, MediaFolderResponse.from(folder)));
    } catch (IllegalArgumentException ex) {
      log.warn("Folder creation validation error: {}", ex.getMessage());
      String message = messageSource.getMessage("media.folder.create.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error creating folder: {}", ex.getMessage());
      String message = messageSource.getMessage("media.folder.create.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Update a folder.
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update folder", description = "Updates an existing media folder (e.g., rename).")
  public ResponseEntity<ApiResponse<MediaFolderResponse>> updateFolder(
      @Parameter(description = "Folder ID") @PathVariable @Valid @NotNull @Min(1) Long id,
      @RequestBody @Valid MediaFolderUpdateRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      Long userId = getCurrentUserId();
      MediaFolder folder = folderService.update(id, request.name(), userId);
      String message = messageSource.getMessage("media.folder.update.success", null,
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.ok(ApiResponse.success(message, MediaFolderResponse.from(folder)));
    } catch (IllegalArgumentException ex) {
      log.warn("Folder update validation error: {}", ex.getMessage());
      String message = messageSource.getMessage("media.folder.update.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error updating folder {}: {}", id, ex.getMessage());
      String message = messageSource.getMessage("media.folder.update.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Delete a folder. Only empty folders can be deleted.
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete folder", description = "Deletes a media folder. Folder must be empty.")
  public ResponseEntity<ApiResponse<Void>> deleteFolder(
      @Parameter(description = "Folder ID") @PathVariable @Valid @NotNull @Min(1) Long id,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      folderService.delete(id);
      String message = messageSource.getMessage("media.folder.delete.success", null,
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.ok(ApiResponse.success(message, null));
    } catch (IllegalArgumentException ex) {
      log.warn("Folder delete validation error: {}", ex.getMessage());
      String message = messageSource.getMessage("media.folder.delete.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (IllegalStateException ex) {
      log.warn("Folder delete state error: {}", ex.getMessage());
      String message = messageSource.getMessage("media.folder.not.empty",
          new Object[] { id }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error deleting folder {}: {}", id, ex.getMessage());
      String message = messageSource.getMessage("media.folder.delete.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(message));
    }
  }

  /**
   * Move a folder to a new parent.
   */
  @PutMapping("/{id}/move")
  @Operation(summary = "Move folder", description = "Moves a folder to a different parent folder.")
  public ResponseEntity<ApiResponse<MediaFolderResponse>> moveFolder(
      @Parameter(description = "Folder ID") @PathVariable @Valid @NotNull @Min(1) Long id,
      @RequestBody @Valid MediaFolderMoveRequest request,
      @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
    try {
      MediaFolder folder = folderService.move(id, request.newParentId());
      String message = messageSource.getMessage("media.folder.move.success", null,
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.ok(ApiResponse.success(message, MediaFolderResponse.from(folder)));
    } catch (IllegalArgumentException ex) {
      log.warn("Folder move validation error: {}", ex.getMessage());
      String message = messageSource.getMessage("media.folder.move.error",
          new Object[] { ex.getMessage() }, Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(message));
    } catch (IllegalStateException ex) {
      log.warn("Folder move state error (circular reference): {}", ex.getMessage());
      String message = messageSource.getMessage("media.folder.circular.reference", null,
          Locale.forLanguageTag(languageCode));
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(ApiResponse.error(message));
    } catch (Exception ex) {
      log.error("Error moving folder {}: {}", id, ex.getMessage());
      String message = messageSource.getMessage("media.folder.move.error",
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
