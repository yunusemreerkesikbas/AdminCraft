package com.backend.application.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.backend.application.config.StorageConfigProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaStorageServiceImpl implements MediaStorageService {

    private final StorageAdapter storageAdapter;
    private final StorageConfigProperties properties;

    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
            "image/jpeg", new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF },
            "image/png", new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 },
            "image/gif", new byte[] { 0x47, 0x49, 0x46, 0x38 },
            "application/pdf", new byte[] { 0x25, 0x50, 0x44, 0x46 });

    @Override
    public String getPublicUrl(String filePath) {
        return storageAdapter.getPublicUrl(filePath);
    }

    @Override
    public StoredFileResult store(MultipartFile file, String folder) {
        ValidationResult validation = validate(file);
        if (!validation.valid()) {
            throw new IllegalArgumentException(String.join(", ", validation.errors()));
        }

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String uniqueFileName = generateUniqueFileName(extension);

        try {
            String filePath = storageAdapter.store(file.getInputStream(), uniqueFileName, folder);
            log.info("Stored file: {} -> {}", originalName, filePath);

            return new StoredFileResult(
                    uniqueFileName,
                    filePath,
                    file.getContentType(),
                    file.getSize(),
                    extension);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalName, e);
        }
    }

    @Override
    public StoredFileResult storeProcessedImage(byte[] imageData, String fileName, String folder) {
        String filePath = storageAdapter.store(imageData, fileName, folder);

        return new StoredFileResult(
                fileName,
                filePath,
                detectMimeType(fileName),
                imageData.length,
                getExtension(fileName));
    }

    @Override
    public byte[] retrieve(String filePath) {
        return storageAdapter.retrieve(filePath);
    }

    @Override
    public void delete(String filePath) {
        storageAdapter.delete(filePath);
    }

    @Override
    public ValidationResult validate(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            errors.add("File is empty");
            return ValidationResult.failure(errors);
        }

        if (!isValidFileSize(file.getSize())) {
            errors.add("File size exceeds maximum allowed: " + properties.getMaxFileSize() + " bytes");
        }

        String mimeType = file.getContentType();
        if (!isValidMimeType(mimeType)) {
            errors.add("File type not allowed: " + mimeType);
        }

        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                errors.add("Invalid filename: path traversal detected");
            }

            String extension = getExtension(fileName).toLowerCase();
            if (properties.getBlockedExtensions().contains(extension)) {
                errors.add("File extension blocked: " + extension);
            }
        }

        try {
            if (!validateMagicBytes(file.getBytes(), mimeType)) {
                errors.add("File content does not match declared type");
            }
        } catch (IOException e) {
            errors.add("Failed to read file content");
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    @Override
    public boolean isValidMimeType(String mimeType) {
        return mimeType != null && properties.getAllowedMimeTypes().contains(mimeType.toLowerCase());
    }

    @Override
    public boolean isValidFileSize(long size) {
        return size <= properties.getMaxFileSize();
    }

    private boolean validateMagicBytes(byte[] content, String mimeType) {
        // Reject null or too short content
        if (content == null || content.length < 4) {
            log.debug("Magic bytes validation rejected: content null or too short (length: {})",
                    content == null ? "null" : content.length);
            return false;
        }

        // Skip validation for mime types without defined magic bytes (allow by default)
        if (mimeType == null) {
            log.debug("Magic bytes validation skipped: mimeType is null");
            return true;
        }

        byte[] expectedBytes = MAGIC_BYTES.get(mimeType.toLowerCase());
        if (expectedBytes == null) {
            // No magic bytes defined for this type - allow it (configurable allowlist for
            // future)
            return true;
        }

        // Validate magic bytes match
        for (int i = 0; i < expectedBytes.length; i++) {
            if (content[i] != expectedBytes[i]) {
                log.debug("Magic bytes validation failed for mimeType {}: expected {} at position {}, got {}",
                        mimeType, expectedBytes[i], i, content[i]);
                return false;
            }
        }
        return true;
    }

    private String generateUniqueFileName(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private String detectMimeType(String fileName) {
        String ext = getExtension(fileName).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }
}
