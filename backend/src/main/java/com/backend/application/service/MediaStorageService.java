package com.backend.application.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {

    record StoredFileResult(
        String fileName,
        String filePath,
        String mimeType,
        long fileSize,
        String extension
    ) {}

    record ValidationResult(
        boolean valid,
        List<String> errors
    ) {
        public static ValidationResult success() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult failure(String... errors) {
            return new ValidationResult(false, List.of(errors));
        }

        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }
    }

    String getPublicUrl(String filePath);

    StoredFileResult store(MultipartFile file, String folder);

    StoredFileResult storeProcessedImage(byte[] imageData, String fileName, String folder);

    byte[] retrieve(String filePath);

    void delete(String filePath);

    ValidationResult validate(MultipartFile file);

    boolean isValidMimeType(String mimeType);

    boolean isValidFileSize(long size);
}
