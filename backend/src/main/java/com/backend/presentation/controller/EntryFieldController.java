package com.backend.presentation.controller;

import com.backend.application.command.CreateEntryFieldCommand;
import com.backend.application.query.GetEntryFieldsByTypeQuery;
import com.backend.application.service.EntryFieldService;
import com.backend.presentation.dto.request.CreateEntryFieldRequest;
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

                CreateEntryFieldCommand command = new CreateEntryFieldCommand(
                                typeId,
                                request.fieldKey(),
                                request.fieldType(),
                                request.isRequired(),
                                request.maxLength(),
                                request.minValue(),
                                request.maxValue());

                EntryFieldDefinitionResponse response = entryFieldService.addField(command);

                String successMessage = messageSource.getMessage("entry.field.create.success",
                                null, Locale.forLanguageTag(lang));
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(successMessage, response));
        }

        @GetMapping("/{typeId}/entry-fields")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<List<EntryFieldDefinitionResponse>>> getFields(
                        @PathVariable Long typeId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {

                GetEntryFieldsByTypeQuery query = new GetEntryFieldsByTypeQuery(typeId);
                List<EntryFieldDefinitionResponse> responses = entryFieldService.getFieldsByType(query);

                String successMessage = messageSource.getMessage("entry.field.list.success",
                                null, Locale.forLanguageTag(lang));
                return ResponseEntity.ok(ApiResponse.success(successMessage, responses));
        }
}
