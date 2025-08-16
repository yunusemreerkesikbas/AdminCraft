package com.backend.presentation.controller;

import com.backend.application.service.MediaService;
import com.backend.domain.entity.MediaFile;
import com.backend.domain.enums.Language;
import com.backend.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;
    private final MessageSource messageSource;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaFile>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tenantId") Long tenantId,
            @RequestParam("uploadedBy") Long uploadedBy,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            MediaFile mediaFile = mediaService.uploadFile(file, tenantId, uploadedBy);
            String message = messageSource.getMessage("media.upload.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, mediaFile));
        } catch (Exception ex) {
            log.error("Error uploading file", ex);
            String message = messageSource.getMessage("media.upload.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaFile>> getMediaFile(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<MediaFile> mediaFile = mediaService.getMediaFileById(id);
            if (mediaFile.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(mediaFile.get()));
            } else {
                String message = messageSource.getMessage("media.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting media file", ex);
            String message = messageSource.getMessage("media.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<MediaFile>>> getMediaFilesByTenant(
            @PathVariable Long tenantId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<MediaFile> mediaFiles = mediaService.getMediaFilesByTenantId(tenantId);
            return ResponseEntity.ok(ApiResponse.success(mediaFiles));
        } catch (Exception ex) {
            log.error("Error getting media files for tenant", ex);
            String message = messageSource.getMessage("media.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMediaFile(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            mediaService.deleteMediaFile(id);
            String message = messageSource.getMessage("media.delete.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting media file", ex);
            String message = messageSource.getMessage("media.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MediaFile>>> getAllMediaFiles(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<MediaFile> mediaFiles = mediaService.getAllMediaFiles();
            return ResponseEntity.ok(ApiResponse.success(mediaFiles));
        } catch (Exception ex) {
            log.error("Error getting all media files", ex);
            String message = messageSource.getMessage("media.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }
}