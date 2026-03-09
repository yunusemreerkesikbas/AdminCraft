package com.backend.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.service.config.ConfigGlobalPropertiesAdminService;
import com.backend.application.service.config.ConfigPropertiesAdminService;
import com.backend.presentation.dto.request.config.UpsertConfigPropertyRequest;
import com.backend.presentation.dto.response.config.ConfigPropertyResponse;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/config/admin/properties")
@RequiredArgsConstructor
public class ConfigAdminPropertiesController {

    private final ConfigPropertiesAdminService propertiesAdminService;
    private final ConfigGlobalPropertiesAdminService globalPropertiesAdminService;
    private final ConfigPrincipalResolver principalResolver;

    @PreAuthorize("hasAnyRole('CONFIG_TENANT_ADMIN','CONFIG_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConfigPropertyResponse>>> listProperties(Authentication authentication) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        List<ConfigPropertyResponse> items = (principal.isConfigSuperAdmin()
                ? globalPropertiesAdminService.listProperties(principal)
                : propertiesAdminService.listProperties(principal))
                .stream()
                .map(ConfigPropertyResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PreAuthorize("hasAnyRole('CONFIG_TENANT_ADMIN','CONFIG_SUPER_ADMIN')")
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<ConfigPropertyResponse>> getProperty(
            Authentication authentication,
            @PathVariable String key) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        var result = principal.isConfigSuperAdmin()
                ? globalPropertiesAdminService.getProperty(principal, key)
                : propertiesAdminService.getProperty(principal, key);
        return ResponseEntity.ok(ApiResponse.success(ConfigPropertyResponse.from(result)));
    }

    @PreAuthorize("hasAnyRole('CONFIG_TENANT_ADMIN','CONFIG_SUPER_ADMIN')")
    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<ConfigPropertyResponse>> upsertProperty(
            Authentication authentication,
            @PathVariable String key,
            @Valid @RequestBody UpsertConfigPropertyRequest request) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        var result = principal.isConfigSuperAdmin()
                ? globalPropertiesAdminService.upsertProperty(principal, key, request.value(), request.secret(), request.reason())
                : propertiesAdminService.upsertProperty(principal, key, request.value(), request.secret(), request.reason());
        return ResponseEntity.ok(ApiResponse.success("config.property.upsert.success", ConfigPropertyResponse.from(result)));
    }

    @PreAuthorize("hasAnyRole('CONFIG_TENANT_ADMIN','CONFIG_SUPER_ADMIN')")
    @DeleteMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            Authentication authentication,
            @PathVariable String key,
            @RequestParam(value = "reason") String reason) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        if (principal.isConfigSuperAdmin()) {
            globalPropertiesAdminService.deleteProperty(principal, key, reason);
        } else {
            propertiesAdminService.deleteProperty(principal, key, reason);
        }
        return ResponseEntity.ok(ApiResponse.success("config.property.delete.success", null));
    }
}
