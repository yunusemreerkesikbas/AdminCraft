package com.backend.presentation.controller;

import com.backend.application.service.EntryFieldService;
import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.presentation.dto.request.CreateEntryFieldRequest;
import com.backend.presentation.dto.response.EntryFieldDefinitionResponse;
import com.backend.shared.common.ApiResponse;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/components/types")
@RequiredArgsConstructor
@Slf4j
public class EntryFieldController {

    private final EntryFieldService entryFieldService;
    private final MessageSource messageSource;

    @PostMapping("/{typeId}/entry-fields")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EntryFieldDefinitionResponse>> addField(
            @PathVariable Long typeId,
            @Valid @RequestBody CreateEntryFieldRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            EntryFieldDefinition field = new EntryFieldDefinition();
            field.setFieldKey(request.fieldKey());
            field.setFieldType(request.fieldType());
            field.setLabelTr(request.labelTr());
            field.setLabelEn(request.labelEn());
            field.setIsRequired(request.isRequired() != null ? request.isRequired() : false);
            field.setMaxLength(request.maxLength());
            field.setMinValue(request.minValue());
            field.setMaxValue(request.maxValue());

            EntryFieldDefinition created = entryFieldService.addField(typeId, field);
            
            String successMessage = messageSource.getMessage("entry.field.create.success",
                    null, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(successMessage, EntryFieldDefinitionResponse.from(created)));
        } catch (Exception ex) {
            log.error("Error adding entry field: {}", ex.getMessage());
            String msg = messageSource.getMessage("entry.field.create.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(msg));
        }
    }

    @GetMapping("/{typeId}/entry-fields")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<EntryFieldDefinitionResponse>>> getFields(
            @PathVariable Long typeId,
            @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        try {
            List<EntryFieldDefinition> fields = entryFieldService.getFieldsByTypeId(typeId);
            List<EntryFieldDefinitionResponse> responses = fields.stream()
                    .map(EntryFieldDefinitionResponse::from)
                    .collect(Collectors.toList());
            
            String successMessage = messageSource.getMessage("entry.field.list.success",
                    null, Locale.forLanguageTag(lang));
            return ResponseEntity.ok(ApiResponse.success(successMessage, responses));
        } catch (Exception ex) {
            log.error("Error fetching entry fields: {}", ex.getMessage());
            String msg = messageSource.getMessage("entry.field.list.error",
                    new Object[] { ex.getMessage() }, Locale.forLanguageTag(lang));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(msg));
        }
    }
}

