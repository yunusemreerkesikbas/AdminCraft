package com.backend.presentation.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.CmsConfigService;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.shared.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cms/config")
@RequiredArgsConstructor
@Tag(name = "CMS Config", description = "Public CMS runtime config endpoints")
public class CmsConfigController {

    private final CmsConfigService cmsConfigService;
    private final TenantContext tenantContext;

    @GetMapping
    @Operation(summary = "Get public CMS config", description = "Returns tenant-scoped public config values for storefront")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Config returned")
    })
    public ResponseEntity<ApiResponse<Map<String, String>>> getPublicConfig() {
        Long tenantId = Long.parseLong(tenantContext.getTenantId());
        String tenantDbName = tenantContext.getTenantDbName();
        Map<String, String> config = cmsConfigService.getPublicConfig(tenantId, tenantDbName);
        return ResponseEntity.ok(ApiResponse.success(config));
    }
}
