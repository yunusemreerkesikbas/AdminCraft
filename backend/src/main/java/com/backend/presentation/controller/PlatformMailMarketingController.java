package com.backend.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.context.i18n.LocaleContextHolder;
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

import com.backend.application.dto.mail.MailCampaignDto;
import com.backend.application.dto.mail.MailOutboxEntryDto;
import com.backend.application.dto.mail.MailSubscriberAdminDto;
import com.backend.application.dto.mail.MailSubscriberDto;
import com.backend.application.dto.mail.MailSubscriberSubscriptionDto;
import com.backend.application.dto.mail.MailTemplateDto;
import com.backend.application.dto.mail.MailTemplateTypeDetailDto;
import com.backend.application.dto.mail.MailTemplateTypeSummaryDto;
import com.backend.application.service.PlatformMailMarketingService;
import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/platform/mail")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
@Validated
public class PlatformMailMarketingController {

    private final PlatformMailMarketingService service;
    private final MessageSource messageSource;

    @GetMapping("/templates/types")
    public ResponseEntity<ApiResponse<List<MailTemplateTypeSummaryDto>>> getTemplateTypes() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                message("mail.marketing.template.types.fetched"),
                service.getTemplateTypes()
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/templates/types/{templateType}")
    public ResponseEntity<ApiResponse<MailTemplateTypeDetailDto>> getTemplateTypeDetail(@PathVariable String templateType) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                message("mail.marketing.template.type.detail.fetched"),
                service.getTemplateTypeDetail(templateType)
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @PutMapping("/templates/types/{templateType}/translations/{language}")
    public ResponseEntity<ApiResponse<MailTemplateDto>> upsertTemplateTranslation(
        @PathVariable String templateType,
        @PathVariable String language,
        @Valid @RequestBody UpdateTemplateTranslationRequest request
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                message("mail.marketing.template.updated"),
                service.upsertTemplateTranslation(
                    templateType,
                    language,
                    request.subject(),
                    request.content(),
                    request.active()
                )
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping(value = "/subscribers", params = "templateType")
    public ResponseEntity<ApiResponse<List<MailSubscriberDto>>> getSubscribersByTemplateType(
        @RequestParam("templateType") String templateType
    ) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                message("mail.marketing.subscribers.fetched"),
                service.getSubscribersByTemplateType(templateType)
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/subscribers/admin")
    public ResponseEntity<ApiResponse<PageableResponse<MailSubscriberAdminDto>>> getSubscriberPage(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) MailSubscriberStatus status,
        @RequestParam(required = false) String templateType,
        @RequestParam(required = false) Boolean permission
    ) {
        try {
            String effectiveSort = SortParseUtil.getEffectiveSortCode(sort, SortableFieldsConfig.MAIL_SUBSCRIBER_DEFAULT_SORT);
            Sort sortObj = SortParseUtil.parse(
                effectiveSort,
                SortableFieldsConfig.MAIL_SUBSCRIBER_ALLOWED_FIELDS,
                SortableFieldsConfig.MAIL_SUBSCRIBER_DEFAULT_SORT
            );
            Pageable pageable = PageRequest.of(page, size, sortObj);
            var dataPage = service.getSubscriberPage(pageable, search, status, templateType, permission);
            SortConfig sortConfig = SortConfig.of(effectiveSort, SortableFieldsConfig.MAIL_SUBSCRIBER_SORT_OPTIONS);
            PageableResponse<MailSubscriberAdminDto> response = PageableResponse.fromMapped(dataPage, dataPage.getContent(), sortConfig);
            return ResponseEntity.ok(ApiResponse.success(message("mail.marketing.subscribers.admin.fetched"), response));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/subscribers/admin/{id}")
    public ResponseEntity<ApiResponse<MailSubscriberAdminDto>> getSubscriberById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                message("mail.marketing.subscriber.fetched"),
                service.getSubscriber(id)
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @PostMapping("/subscribers/admin")
    public ResponseEntity<ApiResponse<MailSubscriberAdminDto>> createSubscriber(
        @Valid @RequestBody UpsertMailSubscriberRequest request
    ) {
        try {
            MailSubscriberAdminDto created = service.createSubscriber(
                request.email(),
                request.status(),
                request.subscriptions()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message("mail.marketing.subscriber.created"), created));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @PutMapping("/subscribers/admin/{id}")
    public ResponseEntity<ApiResponse<MailSubscriberAdminDto>> updateSubscriber(
        @PathVariable Long id,
        @Valid @RequestBody UpsertMailSubscriberRequest request
    ) {
        try {
            MailSubscriberAdminDto updated = service.updateSubscriber(
                id,
                request.email(),
                request.status(),
                request.subscriptions()
            );
            return ResponseEntity.ok(ApiResponse.success(message("mail.marketing.subscriber.updated"), updated));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @DeleteMapping("/subscribers/admin/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscriber(@PathVariable Long id) {
        try {
            service.deleteSubscriber(id);
            return ResponseEntity.ok(ApiResponse.success(message("mail.marketing.subscriber.deleted"), null));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @PostMapping("/campaigns/send")
    public ResponseEntity<ApiResponse<MailCampaignDto>> sendCampaign(@Valid @RequestBody SendCampaignRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                message("mail.marketing.campaign.sent"),
                service.sendCampaign(request.templateType())
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<List<MailCampaignDto>>> getCampaignList(
            @RequestParam String templateType,
            @RequestParam(defaultValue = "5") int size) {
        try {
            return ResponseEntity.ok(ApiResponse.success(null, service.getCampaignList(templateType, size)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<MailCampaignDto>> getCampaign(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                message("mail.marketing.campaign.fetched"),
                service.getCampaign(id)
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/campaigns/{id}/outbox")
    public ResponseEntity<ApiResponse<List<MailOutboxEntryDto>>> getCampaignOutbox(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(null, service.getOutboxEntries(id)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    @GetMapping("/subscribers/admin/export")
    public ResponseEntity<byte[]> exportSubscribers(@RequestParam(required = false) String templateType) {
        String csv = service.exportSubscribersCsv(templateType);
        String filename = "subscribers" + (templateType != null ? "-" + templateType.toLowerCase() : "") + ".csv";
        return ResponseEntity.ok()
            .header("Content-Type", "text/csv; charset=UTF-8")
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String message(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, key, locale);
    }

    public record UpdateTemplateTranslationRequest(
        @NotBlank String subject,
        @NotBlank String content,
        Boolean active
    ) {
    }

    public record SendCampaignRequest(
        @NotBlank String templateType
    ) {
    }

    public record UpsertMailSubscriberRequest(
        @Email @NotBlank String email,
        MailSubscriberStatus status,
        @Valid List<MailSubscriberSubscriptionDto> subscriptions
    ) {
    }
}
