package com.backend.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.PlatformDashboardService;
import com.backend.presentation.dto.response.PlatformDashboardResponse;
import com.backend.shared.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/platform/dashboard")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class PlatformDashboardController {

    private final PlatformDashboardService service;

    @GetMapping
    public ResponseEntity<ApiResponse<PlatformDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(
                PlatformDashboardResponse.from(service.getDashboardData())));
    }
}
