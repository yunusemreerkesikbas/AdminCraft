package com.backend.presentation.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.backend.application.dto.request.CreateComponentEntryRequest;
import com.backend.application.dto.request.UpdateComponentEntryRequest;
import com.backend.application.service.ComponentEntryI18nService;
import com.backend.application.service.ComponentEntryService;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.EntryI18nRequest;
import com.backend.presentation.dto.response.ComponentEntryI18nResponse;
import com.backend.presentation.dto.response.ComponentEntryResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SecurityUtil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/components")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ComponentEntryController {

        private final ComponentEntryService entryService;
        private final ComponentEntryI18nService entryI18nService;
        private final MessageSource messageSource;

        @PostMapping("/{componentId}/entries")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
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
                        log.error("Error creating component entry for componentId={}", componentId, ex);
                        String msg = messageSource.getMessage("component.entry.create.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @GetMapping("/{componentId}/entries")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
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
                        log.error("Error listing component entries for componentId={}", componentId, ex);
                        String msg = messageSource.getMessage("component.entry.list.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @GetMapping("/entries/{id}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
        public ResponseEntity<ApiResponse<?>> getEntry(
                        @PathVariable Long id,
                        @org.springframework.web.bind.annotation.RequestParam(required = false) String include,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        String successMessage = messageSource.getMessage("component.entry.get.success",
                                        null, Locale.forLanguageTag(lang));

                        if ("translations".equalsIgnoreCase(include)) {
                                var composite = entryService.getEntryWithTranslations(id);
                                return ResponseEntity.ok(ApiResponse.success(successMessage, composite));
                        }

                        ComponentEntry entry = entryService.getEntryById(id);
                        return ResponseEntity
                                        .ok(ApiResponse.success(successMessage, ComponentEntryResponse.from(entry)));
                } catch (Exception ex) {
                        log.error("Error getting component entry id={}", id, ex);
                        String msg = messageSource.getMessage("component.entry.get.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PutMapping("/entries/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
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
                        log.error("Error updating component entry id={}", id, ex);
                        String msg = messageSource.getMessage("component.entry.update.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @DeleteMapping("/entries/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteEntry(
                        @PathVariable Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        entryService.deleteEntry(id);

                        String successMessage = messageSource.getMessage("component.entry.delete.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, null));
                } catch (Exception ex) {
                        log.error("Error deleting component entry id={}", id, ex);
                        String msg = messageSource.getMessage("component.entry.delete.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PostMapping("/entries/composite")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public ResponseEntity<ApiResponse<com.backend.application.dto.response.ComponentEntryCompositeResponse>> createEntryComposite(
                        @Valid @RequestBody com.backend.application.dto.request.CreateComponentEntryCompositeRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        var created = entryService.createComposite(request);
                        String successMessage = messageSource.getMessage("component.entry.create.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(successMessage, created));
                } catch (Exception ex) {
                        log.error("Error creating component entry composite", ex);
                        String msg = messageSource.getMessage("component.entry.create.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PutMapping("/entries/{id}/composite")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public ResponseEntity<ApiResponse<com.backend.application.dto.response.ComponentEntryCompositeResponse>> updateEntryComposite(
                        @PathVariable @NotNull @Min(1) Long id,
                        @Valid @RequestBody com.backend.application.dto.request.UpdateComponentEntryCompositeRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        var updated = entryService.updateComposite(id, request);
                        String successMessage = messageSource.getMessage("component.entry.update.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, updated));
                } catch (Exception ex) {
                        log.error("Error updating component entry composite id={}", id, ex);
                        String msg = messageSource.getMessage("component.entry.update.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PutMapping("/entries/{id}/composite/draft")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public ResponseEntity<ApiResponse<com.backend.application.dto.response.ComponentEntryCompositeResponse>> updateEntryCompositeDraft(
                        @PathVariable @NotNull @Min(1) Long id,
                        @Valid @RequestBody com.backend.application.dto.request.UpdateComponentEntryCompositeRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        var updated = entryService.updateDraftComposite(id, request, SecurityUtil.getCurrentUserIdOrThrow());
                        String successMessage = messageSource.getMessage("component.entry.update.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, updated));
                } catch (Exception ex) {
                        log.error("Error updating component entry composite draft id={}", id, ex);
                        String msg = messageSource.getMessage("component.entry.update.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PutMapping("/entries/{id}/i18n/{language}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
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
                        log.error("Error upserting entry i18n for entryId={} language={}", id, language, ex);
                        String msg = messageSource.getMessage("component.entry.i18n.upsert.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @GetMapping("/entries/{id}/i18n/{language}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
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
                        log.error("Error getting entry i18n for entryId={} language={}", id, language, ex);
                        String msg = messageSource.getMessage("component.entry.i18n.get.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }

        @PostMapping("/entries/{id}/publish/{language}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
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
                        log.error("Error publishing entry i18n for entryId={} language={}", id, language, ex);
                        String msg = messageSource.getMessage("component.entry.publish.error",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(msg));
                }
        }
}
