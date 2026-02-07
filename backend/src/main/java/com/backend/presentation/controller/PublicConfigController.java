package com.backend.presentation.controller;

import com.backend.application.dto.PublicTenantConfigResult;
import com.backend.application.service.PublicTenantConfigService;
import com.backend.presentation.dto.response.PublicTenantConfigResponse;
import com.backend.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Slf4j
public class PublicConfigController {

    private final PublicTenantConfigService publicTenantConfigService;

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<PublicTenantConfigResponse>> getPublicConfig() {
        try {
            PublicTenantConfigResult result = publicTenantConfigService.getPublicConfig();
            PublicTenantConfigResponse response = PublicTenantConfigResponse.from(result);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception ex) {
            log.error("Error getting public tenant config", ex);
            return ResponseEntity.ok(
                ApiResponse.success(PublicTenantConfigResponse.from(PublicTenantConfigResult.disabled()))
            );
        }
    }
}
