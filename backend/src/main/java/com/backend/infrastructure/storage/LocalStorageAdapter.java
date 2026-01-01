package com.backend.infrastructure.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.backend.application.config.StorageConfigProperties;
import com.backend.application.service.StorageAdapter;
import com.backend.domain.enums.StorageProvider;
import com.backend.infrastructure.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "admincraft.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageAdapter implements StorageAdapter {

    private final StorageConfigProperties properties;
    private final TenantContext tenantContext;

    @Override
    public String store(InputStream inputStream, String fileName, String subPath) {
        try {
            Path targetPath = resolvePath(subPath, fileName);
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Stored file: {}", targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + fileName, e);
        }
    }

    @Override
    public String store(byte[] content, String fileName, String subPath) {
        return store(new ByteArrayInputStream(content), fileName, subPath);
    }

    @Override
    public byte[] retrieve(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new StorageException("File not found: " + filePath);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new StorageException("Failed to read file: " + filePath, e);
        }
    }

    @Override
    public InputStream retrieveAsStream(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new StorageException("File not found: " + filePath);
            }
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new StorageException("Failed to open stream: " + filePath, e);
        }
    }

    @Override
    public boolean delete(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    @Override
    public long getSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            throw new StorageException("Failed to get file size: " + filePath, e);
        }
    }

    @Override
    public String getPublicUrl(String filePath) {
        // Preserve relative path from base for proper routing
        Path basePath = Paths.get(properties.getBasePath()).toAbsolutePath().normalize();
        Path fullPath = Paths.get(filePath).toAbsolutePath().normalize();

        if (fullPath.startsWith(basePath)) {
            Path relativePath = basePath.relativize(fullPath);
            return "/api/media/files/" + relativePath.toString().replace("\\", "/");
        }

        // Fallback for paths outside base (shouldn't happen in normal flow)
        return "/api/media/files/" + fullPath.getFileName().toString();
    }

    @Override
    public StorageProvider getProviderType() {
        return StorageProvider.LOCAL;
    }

    // Path: {basePath}/{tenantId}/{subPath}/{fileName}
    private Path resolvePath(String subPath, String fileName) {
        // Validate and sanitize fileName
        if (fileName == null || fileName.isEmpty()) {
            throw new StorageException("Invalid filename: null or empty");
        }

        // Extract just the filename to strip any directory segments
        Path fileNamePath = Paths.get(fileName).getFileName();
        if (fileNamePath == null) {
            throw new StorageException("Invalid filename: cannot extract filename from path");
        }
        String sanitizedFileName = fileNamePath.toString();

        // Check for null bytes and invalid characters
        if (sanitizedFileName.contains("\0") || sanitizedFileName.contains("..")) {
            throw new StorageException("Invalid filename: contains illegal characters");
        }

        String tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = "default";
        }

        // Sanitize subPath as well
        String sanitizedSubPath = (subPath != null) ? subPath.replace("..", "") : "";

        // Build and normalize the target path
        Path storageRoot = Paths.get(properties.getBasePath()).toAbsolutePath().normalize();
        Path targetPath = storageRoot.resolve(tenantId).resolve(sanitizedSubPath).resolve(sanitizedFileName)
                .normalize();

        // Validate the resolved path is still within storage root
        if (!targetPath.startsWith(storageRoot)) {
            log.error("Path traversal attempt detected: {} escapes root {}", targetPath, storageRoot);
            throw new StorageException("Invalid path: path traversal detected");
        }

        return targetPath;
    }
}
