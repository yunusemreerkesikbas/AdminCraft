package com.backend.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.dto.request.PatchPlatformSettingsRequest;
import com.backend.application.service.PlatformSettingsService;
import com.backend.presentation.dto.response.PlatformSettingsResponse;
import com.backend.shared.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/platform/settings")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class PlatformSettingsController {

    private final PlatformSettingsService service;

    @GetMapping
    public ResponseEntity<ApiResponse<PlatformSettingsResponse>> getSettings() {
        return ResponseEntity.ok(ApiResponse.success(service.getSettings()));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<PlatformSettingsResponse>> patchSettings(
            @Valid @RequestBody PatchPlatformSettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.patchSettings(request)));
    }
}
