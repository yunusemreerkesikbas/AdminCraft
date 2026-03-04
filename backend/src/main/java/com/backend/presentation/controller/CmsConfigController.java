package com.backend.presentation.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.CmsConfigService;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.shared.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cms/config")
@RequiredArgsConstructor
public class CmsConfigController {

    private final CmsConfigService cmsConfigService;
    private final TenantContext tenantContext;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getPublicConfig() {
        Long tenantId = Long.parseLong(tenantContext.getTenantId());
        String tenantDbName = tenantContext.getTenantDbName();
        Map<String, String> config = cmsConfigService.getPublicConfig(tenantId, tenantDbName);
        return ResponseEntity.ok(ApiResponse.success(config));
    }
}
