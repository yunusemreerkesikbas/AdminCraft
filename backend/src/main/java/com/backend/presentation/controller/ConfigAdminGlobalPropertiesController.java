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
import com.backend.presentation.dto.request.config.UpsertConfigPropertyRequest;
import com.backend.presentation.dto.response.config.ConfigPropertyResponse;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping("/config/admin/global/properties")
@RequiredArgsConstructor
public class ConfigAdminGlobalPropertiesController {

    private final ConfigGlobalPropertiesAdminService globalPropertiesAdminService;
    private final ConfigPrincipalResolver principalResolver;

    @PreAuthorize("hasRole('CONFIG_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConfigPropertyResponse>>> listProperties(Authentication authentication) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        List<ConfigPropertyResponse> items = globalPropertiesAdminService.listProperties(principal)
                .stream()
                .map(ConfigPropertyResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PreAuthorize("hasRole('CONFIG_SUPER_ADMIN')")
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<ConfigPropertyResponse>> getProperty(
            Authentication authentication,
            @PathVariable String key) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        var result = globalPropertiesAdminService.getProperty(principal, key);
        return ResponseEntity.ok(ApiResponse.success(ConfigPropertyResponse.from(result)));
    }

    @PreAuthorize("hasRole('CONFIG_SUPER_ADMIN')")
    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<ConfigPropertyResponse>> upsertProperty(
            Authentication authentication,
            @PathVariable String key,
            @Valid @RequestBody UpsertConfigPropertyRequest request) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        var result = globalPropertiesAdminService.upsertProperty(
                principal,
                key,
                request.value(),
                request.secret(),
                request.reason());
        return ResponseEntity.ok(ApiResponse.success("config.property.upsert.success", ConfigPropertyResponse.from(result)));
    }

    @PreAuthorize("hasRole('CONFIG_SUPER_ADMIN')")
    @DeleteMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            Authentication authentication,
            @PathVariable String key,
            @RequestParam(value = "reason") @NotBlank String reason) {
        ConfigPrincipal principal = principalResolver.resolve(authentication);
        globalPropertiesAdminService.deleteProperty(principal, key, reason);
        return ResponseEntity.ok(ApiResponse.success("config.property.delete.success", null));
    }
}
