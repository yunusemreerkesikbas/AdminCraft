package com.backend.presentation.controller;

import java.util.List;

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

import com.backend.application.dto.outreach.CreateOutreachCampaignRequest;
import com.backend.application.dto.outreach.CreateOutreachContactRequest;
import com.backend.application.dto.outreach.CreateOutreachTemplateRequest;
import com.backend.application.dto.outreach.OutreachCampaignOutboxEntryResponse;
import com.backend.application.dto.outreach.OutreachCampaignResponse;
import com.backend.application.dto.outreach.OutreachContactResponse;
import com.backend.application.dto.outreach.OutreachTemplateResponse;
import com.backend.application.dto.outreach.UpdateOutreachContactRequest;
import com.backend.application.dto.outreach.UpdateOutreachTemplateRequest;
import com.backend.application.service.PlatformOutreachService;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/platform/outreach")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
@Validated
public class PlatformOutreachController {

    private final PlatformOutreachService service;

    @GetMapping("/contacts")
    public ResponseEntity<ApiResponse<Page<OutreachContactResponse>>> getContacts(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getContacts(page, size, sort, search)));
    }

    @PostMapping("/contacts")
    public ResponseEntity<ApiResponse<OutreachContactResponse>> createContact(
        @Valid @RequestBody CreateOutreachContactRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createContact(request)));
    }

    @GetMapping("/contacts/{id}")
    public ResponseEntity<ApiResponse<OutreachContactResponse>> getContact(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getContact(id)));
    }

    @PutMapping("/contacts/{id}")
    public ResponseEntity<ApiResponse<OutreachContactResponse>> updateContact(
        @PathVariable @Positive Long id,
        @Valid @RequestBody UpdateOutreachContactRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.updateContact(id, request)));
    }

    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<ApiResponse<OutreachContactResponse>> deleteContact(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.deleteContact(id)));
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<OutreachTemplateResponse>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.success(service.getTemplates()));
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<OutreachTemplateResponse>> createTemplate(
        @Valid @RequestBody CreateOutreachTemplateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createTemplate(request)));
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<OutreachTemplateResponse>> getTemplate(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getTemplate(id)));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<OutreachTemplateResponse>> updateTemplate(
        @PathVariable @Positive Long id,
        @Valid @RequestBody UpdateOutreachTemplateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.updateTemplate(id, request)));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable @Positive Long id) {
        service.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<Page<OutreachCampaignResponse>>> getCampaigns(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getCampaigns(page, size, sort)));
    }

    @PostMapping("/campaigns")
    public ResponseEntity<ApiResponse<OutreachCampaignResponse>> createCampaign(
        @Valid @RequestBody CreateOutreachCampaignRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createCampaign(request)));
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<OutreachCampaignResponse>> getCampaign(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getCampaign(id)));
    }

    @PostMapping("/campaigns/{id}/send")
    public ResponseEntity<ApiResponse<OutreachCampaignResponse>> sendCampaign(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.sendCampaign(id)));
    }

    @GetMapping("/campaigns/{id}/outbox")
    public ResponseEntity<ApiResponse<Page<OutreachCampaignOutboxEntryResponse>>> getCampaignOutbox(
        @PathVariable @Positive Long id,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getCampaignOutbox(id, page, size)));
    }
}
