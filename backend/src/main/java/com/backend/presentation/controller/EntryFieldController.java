package com.backend.presentation.controller;

import com.backend.application.query.GetEntryFieldsByTypeQuery;
import com.backend.application.service.EntryFieldService;
import com.backend.application.dto.request.CreateEntryFieldRequest;
import com.backend.presentation.dto.response.EntryFieldDefinitionResponse;
import com.backend.shared.common.ApiResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/components/types")
@RequiredArgsConstructor
@Slf4j
public class EntryFieldController {

        private final EntryFieldService entryFieldService;
        private final MessageSource messageSource;

        @PostMapping("/{typeId}/entry-fields")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
        @RateLimiter(name = "entryFieldDefinition")
        public ResponseEntity<ApiResponse<EntryFieldDefinitionResponse>> addField(
                        @PathVariable Long typeId,
                        @Valid @RequestBody CreateEntryFieldRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        EntryFieldDefinitionResponse response = EntryFieldDefinitionResponse.from(
                                        entryFieldService.addField(typeId, request));

                        String successMessage = messageSource.getMessage("entry.field.create.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponse.success(successMessage, response));
                } catch (Exception ex) {
                        log.error("Error adding entry field for componentTypeId={}", typeId, ex);
                        String message = messageSource.getMessage("entry.field.create.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }

        @GetMapping("/{typeId}/entry-fields")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN', 'VIEWER')")
        public ResponseEntity<ApiResponse<List<EntryFieldDefinitionResponse>>> getFields(
                        @PathVariable Long typeId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
                try {
                        GetEntryFieldsByTypeQuery query = new GetEntryFieldsByTypeQuery(typeId);
                        List<EntryFieldDefinitionResponse> responses = entryFieldService.getFieldsByType(query)
                                        .stream()
                                        .map(EntryFieldDefinitionResponse::from)
                                        .toList();

                        String successMessage = messageSource.getMessage("entry.field.list.success",
                                        null, Locale.forLanguageTag(lang));
                        return ResponseEntity.ok(ApiResponse.success(successMessage, responses));
                } catch (Exception ex) {
                        log.error("Error listing entry fields for componentTypeId={}", typeId, ex);
                        String message = messageSource.getMessage("entry.field.list.error",
                                        new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error(message));
                }
        }
}
