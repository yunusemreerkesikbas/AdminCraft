package com.backend.presentation.controller;

import com.backend.application.service.ComponentEntryI18nService;
import com.backend.application.service.ComponentEntryService;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.CreateComponentEntryRequest;
import com.backend.presentation.dto.request.EntryI18nRequest;
import com.backend.presentation.dto.request.UpdateComponentEntryRequest;
import com.backend.presentation.dto.response.ComponentEntryI18nResponse;
import com.backend.presentation.dto.response.ComponentEntryResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/components")
@RequiredArgsConstructor
@Slf4j
public class ComponentEntryController {

        private final ComponentEntryService entryService;
        private final ComponentEntryI18nService entryI18nService;
        private final MessageSource messageSource;

        @PostMapping("/{componentId}/entries")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EDITOR')")
        public ResponseEntity<ApiResponse<ComponentEntryResponse>> createEntry(
                        @PathVariable Long componentId,
                        @Valid @RequestBody CreateComponentEntryRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        ComponentEntry entry = new ComponentEntry();
                        entry.setComponentId(componentId);
                        entry.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
                        entry.setIsVisible(request.isVisible() != null ? request.isVisible() : true);
                        entry.setStyleClasses(request.styleClasses());
                        entry.setStatus(request.status() != null ? request.status() : ComponentStatus.DRAFT);
                        entry.setCreatedBy(SecurityUtil.getCurrentUserIdOrThrow());
                        entry.setUpdatedBy(SecurityUtil.getCurrentUserIdOrThrow());

                        ComponentEntry created = entryService.createEntry(entry);

                        String successMessage = messageSource.getMessage("component.entry.create.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(successMessage,
                                                        ComponentEntryResponse.from(created)));
                } catch (Exception ex) {
                        log.error("Error creating component entry: {}", ex.getMessage());
                        String msg = messageSource.getMessage("component.entry.create.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @GetMapping("/{componentId}/entries")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EDITOR', 'VIEWER')")
        public ResponseEntity<ApiResponse<List<ComponentEntryResponse>>> listEntries(
                        @PathVariable Long componentId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        List<ComponentEntry> entries = entryService.getEntriesByComponentId(componentId);
                        List<ComponentEntryResponse> responses = entries.stream()
                                        .map(ComponentEntryResponse::from)
                                        .collect(Collectors.toList());

                        String successMessage = messageSource.getMessage("component.entry.list.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, responses));
                } catch (Exception ex) {
                        log.error("Error listing entries: {}", ex.getMessage());
                        String msg = messageSource.getMessage("component.entry.list.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @GetMapping("/entries/{id}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EDITOR', 'VIEWER')")
        public ResponseEntity<ApiResponse<ComponentEntryResponse>> getEntry(
                        @PathVariable Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        ComponentEntry entry = entryService.getEntryById(id);

                        String successMessage = messageSource.getMessage("component.entry.get.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity
                                        .ok(ApiResponse.success(successMessage, ComponentEntryResponse.from(entry)));
                } catch (Exception ex) {
                        log.error("Error getting entry: {}", ex.getMessage());
                        String msg = messageSource.getMessage("component.entry.get.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PutMapping("/entries/{id}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EDITOR')")
        public ResponseEntity<ApiResponse<ComponentEntryResponse>> updateEntry(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateComponentEntryRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        ComponentEntry entry = entryService.getEntryById(id);
                        if (request.sortOrder() != null)
                                entry.setSortOrder(request.sortOrder());
                        if (request.isVisible() != null)
                                entry.setIsVisible(request.isVisible());
                        if (request.styleClasses() != null)
                                entry.setStyleClasses(request.styleClasses());
                        if (request.status() != null)
                                entry.setStatus(request.status());
                        entry.setUpdatedBy(SecurityUtil.getCurrentUserIdOrThrow());

                        ComponentEntry updated = entryService.updateEntry(entry);

                        String successMessage = messageSource.getMessage("component.entry.update.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity
                                        .ok(ApiResponse.success(successMessage, ComponentEntryResponse.from(updated)));
                } catch (Exception ex) {
                        log.error("Error updating entry: {}", ex.getMessage());
                        String msg = messageSource.getMessage("component.entry.update.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @DeleteMapping("/entries/{id}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EDITOR')")
        public ResponseEntity<ApiResponse<Void>> deleteEntry(
                        @PathVariable Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        entryService.deleteEntry(id);

                        String successMessage = messageSource.getMessage("component.entry.delete.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, null));
                } catch (Exception ex) {
                        log.error("Error deleting entry: {}", ex.getMessage());
                        String msg = messageSource.getMessage("component.entry.delete.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PutMapping("/entries/{id}/i18n/{language}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EDITOR')")
        public ResponseEntity<ApiResponse<ComponentEntryI18nResponse>> upsertEntryI18n(
                        @PathVariable Long id,
                        @PathVariable Language language,
                        @Valid @RequestBody EntryI18nRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        Map<String, Object> data = new HashMap<>();
                        data.put("title", request.title());
                        data.put("description", request.description());
                        data.put("imageUrl", request.imageUrl());
                        data.put("buttonText", request.buttonText());
                        data.put("buttonUrl", request.buttonUrl());

                        if (request.customFields() != null) {
                                data.putAll(request.customFields());
                        }

                        ComponentEntryI18n entryI18n = entryI18nService.upsertEntryI18n(id, language, data);

                        String successMessage = messageSource.getMessage("component.entry.i18n.upsert.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage,
                                        ComponentEntryI18nResponse.from(entryI18n, request.customFields())));
                } catch (Exception ex) {
                        log.error("Error upserting entry i18n: {}", ex.getMessage());
                        String msg = messageSource.getMessage("component.entry.i18n.upsert.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @GetMapping("/entries/{id}/i18n/{language}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EDITOR', 'VIEWER')")
        public ResponseEntity<ApiResponse<ComponentEntryI18nResponse>> getEntryI18n(
                        @PathVariable Long id,
                        @PathVariable Language language,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        ComponentEntryI18n entryI18n = entryI18nService.getEntryI18n(id, language);

                        String successMessage = messageSource.getMessage("component.entry.i18n.get.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage,
                                        ComponentEntryI18nResponse.from(entryI18n, Map.of())));
                } catch (Exception ex) {
                        log.error("Error getting entry i18n: {}", ex.getMessage());
                        String msg = messageSource.getMessage("component.entry.i18n.get.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PostMapping("/entries/{id}/publish/{language}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EDITOR')")
        public ResponseEntity<ApiResponse<ComponentEntryI18nResponse>> publishEntry(
                        @PathVariable Long id,
                        @PathVariable Language language,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        ComponentEntryI18n entryI18n = entryI18nService.publishEntryI18n(id, language);

                        String successMessage = messageSource.getMessage("component.entry.publish.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage,
                                        ComponentEntryI18nResponse.from(entryI18n, Map.of())));
                } catch (Exception ex) {
                        log.error("Error publishing entry: {}", ex.getMessage());
                        String msg = messageSource.getMessage("component.entry.publish.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }
}
