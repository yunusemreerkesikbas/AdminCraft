package com.backend.presentation.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.application.service.MediaService;
import com.backend.domain.entity.Media;
import com.backend.presentation.dto.response.MediaResponse;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Media controller for managing media files.
 * Phase 1 - Core CRUD operations.
 */
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class MediaController {

    private final MediaService mediaService;
    private final MessageSource messageSource;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "text/plain",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "video/mp4", "audio/mpeg", "audio/mp3");

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaResponse>> uploadFile(
            @RequestParam("file") @Valid MultipartFile file,
            @RequestParam("uploadedBy") @NotNull @Min(1) Long uploadedBy,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            validateFileUpload(file);

            Media media = mediaService.uploadFile(file, uploadedBy);
            String message = messageSource.getMessage("media.upload.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(message, MediaResponse.from(media)));
        } catch (Exception ex) {
            log.error("Error uploading file: {}", ex.getMessage());
            String message = messageSource.getMessage("media.upload.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaResponse>> getMediaById(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<Media> media = mediaService.findById(id);
            if (media.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(MediaResponse.from(media.get())));
            } else {
                String message = messageSource.getMessage("media.not.found", new Object[] { id },
                        Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting media {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("media.get.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/uid/{uid}")
    public ResponseEntity<ApiResponse<MediaResponse>> getMediaByUid(
            @PathVariable String uid,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<Media> media = mediaService.findByUid(uid);
            if (media.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(MediaResponse.from(media.get())));
            } else {
                String message = messageSource.getMessage("media.not.found", new Object[] { uid },
                        Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting media by UID {}: {}", uid, ex.getMessage());
            String message = messageSource.getMessage("media.get.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MediaResponse>>> getAllMedia(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<Media> mediaList = mediaService.findAll();
            List<MediaResponse> responseList = mediaList.stream()
                    .map(MediaResponse::from)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success(responseList));
        } catch (Exception ex) {
            log.error("Error getting all media: {}", ex.getMessage());
            String message = messageSource.getMessage("media.list.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @PathVariable @Valid @NotNull @Min(1) Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            mediaService.delete(id);
            String message = messageSource.getMessage("media.delete.success", null,
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting media {}: {}", id, ex.getMessage());
            String message = messageSource.getMessage("media.delete.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<ApiResponse<List<MediaResponse>>> getMediaByFolder(
            @PathVariable @Valid @NotNull @Min(1) Long folderId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<Media> mediaList = mediaService.findByFolderId(folderId);
            List<MediaResponse> responseList = mediaList.stream()
                    .map(MediaResponse::from)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success(responseList));
        } catch (Exception ex) {
            log.error("Error getting media by folder {}: {}", folderId, ex.getMessage());
            String message = messageSource.getMessage("media.list.error", new Object[] { ex.getMessage() },
                    Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
    }

    private void validateFileUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid filename");
        }
        String filename = originalFilename.toLowerCase();
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\") ||
                filename.endsWith(".exe") || filename.endsWith(".bat") || filename.endsWith(".cmd") ||
                filename.endsWith(".scr") || filename.endsWith(".js") || filename.endsWith(".vbs")) {
            throw new IllegalArgumentException("Filename contains invalid characters or dangerous extension");
        }
    }
}