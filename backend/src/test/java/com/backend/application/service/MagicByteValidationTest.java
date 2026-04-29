package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.backend.application.config.StorageConfigProperties;
import com.backend.application.service.MediaStorageService.ValidationResult;
import com.backend.domain.enums.MediaUploadErrorCode;

/**
 * SEC-111: verifies magic-byte validation denies mismatched and untrusted content.
 */
class MagicByteValidationTest {

    private MediaStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MediaStorageServiceImpl(
                mock(StorageAdapter.class),
                new StorageConfigProperties());
    }

    @Test
    @DisplayName("Valid PNG bytes with image/png content-type → passes")
    void validPng_passes() {
        byte[] pngBytes = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", pngBytes);

        ValidationResult result = service.validate(file);

        assertThat(result.valid()).isTrue();
    }

    @Test
    @DisplayName("SEC-111: text bytes declared as image/jpeg → fails (magic-byte mismatch)")
    void textContentDeclaredAsJpeg_fails() {
        byte[] textBytes = "Hello, world!".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "evil.jpg", "image/jpeg", textBytes);

        ValidationResult result = service.validate(file);

        assertThat(result.valid()).isFalse();
        assertThat(result.primaryIssue().code()).isEqualTo(MediaUploadErrorCode.CONTENT_MISMATCH);
    }

    @Test
    @DisplayName("SEC-111: null content-type → fails (deny-by-default)")
    void nullContentType_fails() {
        byte[] pngBytes = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", null, pngBytes);

        ValidationResult result = service.validate(file);

        assertThat(result.valid()).isFalse();
        assertThat(result.primaryIssue().code()).isEqualTo(MediaUploadErrorCode.MIME_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("Valid WebP bytes (RIFF header) with image/webp → passes")
    void validWebp_passes() {
        // RIFF....WEBP header
        byte[] webpBytes = new byte[] {
            0x52, 0x49, 0x46, 0x46,  // RIFF
            0x00, 0x00, 0x00, 0x00,  // file size (placeholder)
            0x57, 0x45, 0x42, 0x50   // WEBP
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.webp", "image/webp", webpBytes);

        ValidationResult result = service.validate(file);

        assertThat(result.valid()).isTrue();
    }

    @Test
    @DisplayName("SEC-111: JPEG bytes declared as image/png → fails (magic-byte mismatch)")
    void jpegBytesDeclaredAsPng_fails() {
        byte[] jpegBytes = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 };
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", jpegBytes);

        ValidationResult result = service.validate(file);

        assertThat(result.valid()).isFalse();
        assertThat(result.primaryIssue().code()).isEqualTo(MediaUploadErrorCode.CONTENT_MISMATCH);
    }
}
