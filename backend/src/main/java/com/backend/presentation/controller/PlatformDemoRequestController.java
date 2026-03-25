package com.backend.presentation.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.platform.PlatformDemoRequestAdminDto;
import com.backend.application.service.PlatformDemoRequestService;
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
@RequestMapping("/platform/demo-requests")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
@Validated
public class PlatformDemoRequestController {

    private final PlatformDemoRequestService platformDemoRequestService;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<ApiResponse<PageableResponse<PlatformDemoRequestAdminDto>>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) @Size(max = 200) String search
    ) {
        try {
            String effectiveSort = SortParseUtil.getEffectiveSortCode(sort,
                    SortableFieldsConfig.DEMO_REQUEST_DEFAULT_SORT);
            Sort sortObj = SortParseUtil.parse(
                    effectiveSort,
                    SortableFieldsConfig.DEMO_REQUEST_ALLOWED_FIELDS,
                    SortableFieldsConfig.DEMO_REQUEST_DEFAULT_SORT
            );
            var pageable = PageRequest.of(page, size, sortObj);
            var dataPage = platformDemoRequestService.getPage(pageable, search);
            SortConfig sortConfig = SortConfig.of(effectiveSort, SortableFieldsConfig.DEMO_REQUEST_SORT_OPTIONS);
            PageableResponse<PlatformDemoRequestAdminDto> body =
                    PageableResponse.from(dataPage, sortConfig);
            return ResponseEntity.ok(ApiResponse.success(message("platform.demo.requests.fetched"), body));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message(ex.getMessage())));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message(ex.getMessage())));
        }
    }

    private String message(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, key, locale);
    }
}
