package com.backend.presentation.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.service.MediaMigrationService;
import com.backend.application.service.MediaMigrationService.MigrationStatus;
import com.backend.shared.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/media/migration")
@ConditionalOnProperty(name = "craftive.storage.provider", havingValue = "s3")
@RequiredArgsConstructor
public class MediaMigrationController {

    private final MediaMigrationService migrationService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> startMigration() {
        migrationService.startMigration();
        return ResponseEntity.accepted().body(ApiResponse.success("Media migration started", null));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<MigrationStatus>> getMigrationStatus() {
        return ResponseEntity.ok(ApiResponse.success(migrationService.getMigrationStatus()));
    }
}
