package com.backend.presentation.controller;

import java.time.Duration;
import java.util.Optional;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.MediaService;
import com.backend.domain.entity.Media;
import com.backend.shared.common.SecurityHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Public file delivery under {@code /media/files/**}. Kept separate from {@link MediaController}
 * so class-level {@code @PreAuthorize} does not block anonymous access (SEC-009).
 */
@RestController
@RequestMapping("/media/files")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Media files", description = "Public download of media file bytes")
public class MediaFileController {

    private final MediaService mediaService;
    private final SecurityHelper securityHelper;

    @GetMapping("/{fileName:.+}")
    @Operation(summary = "Download media file", description = "Serves public media without authentication. Private media requires an authenticated request.")
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "File name (UUID)", required = true) @PathVariable("fileName") @NotNull String fileName) {
        try {
            Optional<Media> mediaOpt = mediaService.findByFileName(fileName);
            if (mediaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Media media = mediaOpt.get();

            if (!Boolean.TRUE.equals(media.getIsPublic()) && !securityHelper.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            byte[] content = mediaService.getFileContent(fileName);

            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (media.getMimeType() != null) {
                try {
                    mediaType = MediaType.parseMediaType(media.getMimeType());
                } catch (Exception e) {
                    log.warn("Invalid mime type for file {}: {}", fileName, e.getMessage());
                }
            }

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception ex) {
            log.error("Error downloading file {}: {}", fileName, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
