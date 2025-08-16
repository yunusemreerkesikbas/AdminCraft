package com.backend.presentation.controller;

import com.backend.application.service.ContentService;
import com.backend.domain.entity.Content;
import com.backend.domain.enums.Language;
import com.backend.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
@Slf4j
public class ContentController {

    private final ContentService contentService;
    private final MessageSource messageSource;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Content>> getContentById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            Optional<Content> content = contentService.getContentById(id);
            if (content.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(content.get()));
            } else {
                String message = messageSource.getMessage("content.not.found", new Object[]{id}, Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(message));
            }
        } catch (Exception ex) {
            log.error("Error getting content by id", ex);
            String message = messageSource.getMessage("content.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<Content>> getContentBySlug(
            @PathVariable String slug,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            // TODO: Implement getContentBySlug in ContentService when ready
            String message = messageSource.getMessage("content.slug.not.implemented", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error(message));
        } catch (Exception ex) {
            log.error("Error getting content by slug", ex);
            String message = messageSource.getMessage("content.get.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<List<Content>>> getContentByTenant(
            @PathVariable Long tenantId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<Content> content = contentService.getContentByTenantId(tenantId);
            return ResponseEntity.ok(ApiResponse.success(content));
        } catch (Exception ex) {
            log.error("Error getting content by tenant", ex);
            String message = messageSource.getMessage("content.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Content>>> getAllContent(
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            List<Content> content = contentService.getAllContent();
            return ResponseEntity.ok(ApiResponse.success(content));
        } catch (Exception ex) {
            log.error("Error getting all content", ex);
            String message = messageSource.getMessage("content.list.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
        try {
            contentService.deleteContent(id);
            String message = messageSource.getMessage("content.delete.success", null, Locale.forLanguageTag(languageCode));
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception ex) {
            log.error("Error deleting content", ex);
            String message = messageSource.getMessage("content.delete.error", new Object[]{ex.getMessage()}, Locale.forLanguageTag(languageCode));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
        }
    }
}