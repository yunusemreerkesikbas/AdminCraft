package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.PlatformOutreachService;
import com.backend.presentation.dto.request.outreach.CreateOutreachCampaignRequest;
import com.backend.presentation.dto.request.outreach.CreateOutreachContactRequest;
import com.backend.presentation.dto.request.outreach.CreateOutreachTemplateRequest;
import com.backend.presentation.dto.request.outreach.UpdateOutreachContactRequest;
import com.backend.presentation.dto.request.outreach.UpdateOutreachTemplateRequest;
import com.backend.presentation.dto.response.outreach.OutreachCampaignOutboxEntryResponse;
import com.backend.presentation.dto.response.outreach.OutreachCampaignResponse;
import com.backend.presentation.dto.response.outreach.OutreachContactResponse;
import com.backend.presentation.dto.response.outreach.OutreachTemplateResponse;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/platform/outreach")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
@Validated
public class PlatformOutreachController {

    private final PlatformOutreachService service;
    private final MessageSource messageSource;

    @GetMapping("/contacts/admin")
    public ResponseEntity<ApiResponse<Page<OutreachContactResponse>>> getContacts(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String search
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.getContacts(page, size, sort, search)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @PostMapping("/contacts/admin")
    public ResponseEntity<ApiResponse<OutreachContactResponse>> createContact(
        @Valid @RequestBody CreateOutreachContactRequest request
    ) {
        try {
            OutreachContactResponse created = service.createContact(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/contacts/admin/{id}")
    public ResponseEntity<ApiResponse<OutreachContactResponse>> getContact(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.getContact(id)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @PutMapping("/contacts/admin/{id}")
    public ResponseEntity<ApiResponse<OutreachContactResponse>> updateContact(
        @PathVariable Long id,
        @Valid @RequestBody UpdateOutreachContactRequest request
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.updateContact(id, request)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @DeleteMapping("/contacts/admin/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable Long id) {
        try {
            service.deleteContact(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<OutreachTemplateResponse>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(service.getTemplates()));
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<OutreachTemplateResponse>> createTemplate(
        @Valid @RequestBody CreateOutreachTemplateRequest request
    ) {
        try {
            OutreachTemplateResponse created = service.createTemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<OutreachTemplateResponse>> getTemplate(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.getTemplate(id)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<OutreachTemplateResponse>> updateTemplate(
        @PathVariable Long id,
        @Valid @RequestBody UpdateOutreachTemplateRequest request
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.updateTemplate(id, request)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        try {
            service.deleteTemplate(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<Page<OutreachCampaignResponse>>> getCampaigns(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(required = false) String sort
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.getCampaigns(page, size, sort)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @PostMapping("/campaigns")
    public ResponseEntity<ApiResponse<OutreachCampaignResponse>> createCampaign(
        @Valid @RequestBody CreateOutreachCampaignRequest request
    ) {
        try {
            OutreachCampaignResponse created = service.createCampaign(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<OutreachCampaignResponse>> getCampaign(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.getCampaign(id)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @PostMapping("/campaigns/{id}/send")
    public ResponseEntity<ApiResponse<OutreachCampaignResponse>> sendCampaign(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.sendCampaign(id)));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/campaigns/{id}/outbox")
    public ResponseEntity<ApiResponse<Page<OutreachCampaignOutboxEntryResponse>>> getCampaignOutbox(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.getCampaignOutbox(id, page, size)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    private String message(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, key, locale);
    }
}
