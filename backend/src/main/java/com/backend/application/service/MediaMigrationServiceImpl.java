package com.backend.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.application.config.StorageConfigProperties;
import com.backend.domain.entity.Media;
import com.backend.domain.enums.StorageProvider;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.MediaRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(name = "admincraft.storage.provider", havingValue = "s3")
@Slf4j
public class MediaMigrationServiceImpl implements MediaMigrationService {

    private final MediaRepository mediaRepository;
    private final StorageAdapter s3Adapter;
    private final TenantContextPort tenantContext;
    private final Executor taskExecutor;
    private final TransactionTemplate transactionTemplate;
    private final StorageConfigProperties storageConfig;

    // keyed by tenantSubdomain
    private final ConcurrentHashMap<String, MigrationStatus> statusMap = new ConcurrentHashMap<>();

    public MediaMigrationServiceImpl(
            MediaRepository mediaRepository,
            @Qualifier("s3StorageAdapter") StorageAdapter s3Adapter,
            TenantContextPort tenantContext,
            @Qualifier("taskExecutor") Executor taskExecutor,
            TransactionTemplate transactionTemplate,
            StorageConfigProperties storageConfig) {
        this.mediaRepository = mediaRepository;
        this.s3Adapter = s3Adapter;
        this.tenantContext = tenantContext;
        this.taskExecutor = taskExecutor;
        this.transactionTemplate = transactionTemplate;
        this.storageConfig = storageConfig;
    }

    @Override
    public void startMigration() {
        String subdomain = tenantContext.getSubdomain();
        MigrationStatus current = statusMap.get(subdomain);
        if (current != null && current.state() == MigrationState.RUNNING) {
            log.warn("Migration already running for tenant: {}", subdomain);
            return;
        }
        statusMap.put(subdomain, new MigrationStatus(subdomain, 0, 0, 0, List.of(), MigrationState.RUNNING));
        // Submitting to taskExecutor — TenantContextTaskDecorator propagates tenant context
        taskExecutor.execute(() -> runMigration(subdomain));
    }

    private void runMigration(String subdomain) {
        log.info("Starting media migration for tenant: {}", subdomain);

        List<Media> localMedia = mediaRepository.findByStorageProvider(StorageProvider.LOCAL);
        int total = localMedia.size();
        AtomicInteger migrated = new AtomicInteger(0);
        List<String> failedFileNames = new ArrayList<>();

        for (Media media : localMedia) {
            try {
                byte[] content = Files.readAllBytes(Paths.get(media.getFilePath()));
                String objectKey = s3Adapter.store(content, media.getFileName(), "media");
                String cdnUrl = s3Adapter.getPublicUrl(objectKey);

                String localFilePath = media.getFilePath();
                transactionTemplate.execute(status -> {
                    media.setExternalUrl(cdnUrl);
                    media.setFilePath(objectKey);
                    media.setStorageProvider(StorageProvider.S3);
                    return mediaRepository.save(media);
                });

                if (storageConfig.getS3().isDeleteLocalAfterMigration()) {
                    try {
                        Files.deleteIfExists(Paths.get(localFilePath));
                    } catch (IOException e) {
                        log.warn("Could not delete local file after migration: {}", localFilePath);
                    }
                }

                migrated.incrementAndGet();
                log.debug("Migrated media {}: {}", media.getFileName(), cdnUrl);
            } catch (IOException e) {
                log.error("Failed to read local file for media {}: {}", media.getFileName(), e.getMessage());
                failedFileNames.add(media.getFileName());
            } catch (Exception e) {
                log.error("Failed to migrate media {}: {}", media.getFileName(), e.getMessage());
                failedFileNames.add(media.getFileName());
            }
        }

        MigrationState finalState = failedFileNames.isEmpty()
                ? MigrationState.COMPLETED
                : MigrationState.PARTIAL_FAILURE;

        statusMap.put(subdomain, new MigrationStatus(
                subdomain, total, migrated.get(), failedFileNames.size(), failedFileNames, finalState));

        log.info("Migration completed for tenant {}: {}/{} migrated, {} failed",
                subdomain, migrated.get(), total, failedFileNames.size());
    }

    @Override
    public MigrationStatus getMigrationStatus() {
        String subdomain = tenantContext.getSubdomain();
        return statusMap.getOrDefault(subdomain,
                new MigrationStatus(subdomain, 0, 0, 0, List.of(), MigrationState.IDLE));
    }
}
