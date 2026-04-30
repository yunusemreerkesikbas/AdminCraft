package com.backend.application.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.enums.MediaUploadErrorCode;
import com.backend.domain.exception.MediaUploadValidationException;

public interface MediaStorageService {

    record StoredFileResult(
        String fileName,
        String filePath,
        String mimeType,
        long fileSize,
        String extension
    ) {}

    record ValidationIssue(MediaUploadErrorCode code, List<Object> args) {
        public static ValidationIssue of(MediaUploadErrorCode code, Object... messageArgs) {
            if (messageArgs == null || messageArgs.length == 0) {
                return new ValidationIssue(code, List.of());
            }
            // Arrays.asList allows null elements (e.g. null MIME client hint); List.of does not.
            return new ValidationIssue(code, Arrays.asList(messageArgs));
        }
    }

    record ValidationResult(
        boolean valid,
        List<ValidationIssue> issues
    ) {
        public static ValidationResult success() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult failure(List<ValidationIssue> issues) {
            return new ValidationResult(false, List.copyOf(issues));
        }

        public static ValidationResult failure(ValidationIssue... issues) {
            return new ValidationResult(false, List.of(issues));
        }

        public ValidationIssue primaryIssue() {
            return issues().stream()
                    .min(Comparator.comparingInt(i -> i.code().displayPriority()))
                    .orElseThrow(() -> new IllegalStateException("No validation issues"));
        }

        public MediaUploadValidationException toUploadException() {
            if (valid()) {
                throw new IllegalStateException("No validation failure");
            }
            ValidationIssue primary = primaryIssue();
            return new MediaUploadValidationException(primary.code(), primary.args());
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
