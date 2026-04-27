package com.backend.presentation.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.contact.ContactRequestAdminDto;
import com.backend.application.service.ContactRequestService;
import com.backend.presentation.dto.response.PageableResponse;
import com.backend.presentation.dto.response.SortConfig;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.SortParseUtil;
import com.backend.shared.config.SortableFieldsConfig;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/contact-requests")
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'VIEWER')")
@RequiredArgsConstructor
@Validated
public class ContactRequestController {

    private final ContactRequestService contactRequestService;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<ApiResponse<PageableResponse<ContactRequestAdminDto>>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) @Size(max = 200) String search) {
        String effectiveSort = SortParseUtil.getEffectiveSortCode(
                sort,
                SortableFieldsConfig.CONTACT_REQUEST_DEFAULT_SORT);
        Sort sortObj = SortParseUtil.parse(
                effectiveSort,
                SortableFieldsConfig.CONTACT_REQUEST_ALLOWED_FIELDS,
                SortableFieldsConfig.CONTACT_REQUEST_DEFAULT_SORT);
        var pageable = PageRequest.of(page, size, sortObj);
        var dataPage = contactRequestService.getPage(pageable, search);
        SortConfig sortConfig = SortConfig.of(effectiveSort, SortableFieldsConfig.CONTACT_REQUEST_SORT_OPTIONS);
        PageableResponse<ContactRequestAdminDto> body = PageableResponse.from(dataPage, sortConfig);
        return ResponseEntity.ok(ApiResponse.success(message("contact.requests.fetched"), body));
    }

    private String message(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, key, locale);
    }
}
