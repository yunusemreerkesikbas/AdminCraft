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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/media/migration")
@ConditionalOnProperty(name = "admincraft.storage.provider", havingValue = "s3")
@RequiredArgsConstructor
public class MediaMigrationController {

    private final MediaMigrationService migrationService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> startMigration() {
        migrationService.startMigration();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<MigrationStatus> getMigrationStatus() {
        return ResponseEntity.ok(migrationService.getMigrationStatus());
    }
}
