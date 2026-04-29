package com.backend.domain.enums;

/**
 * Stable machine-readable reasons for media upload validation (SEC-111 and controller checks).
 * Lower {@link #displayPriority()} values are chosen first when multiple issues exist.
 */
public enum MediaUploadErrorCode {

    EMPTY_FILE(1),
    FILENAME_REQUIRED(2),
    FILENAME_SECURITY_BLOCKED(3),
    INVALID_FILENAME_PATH(4),
    EXTENSION_BLOCKED(5),
    MIME_TYPE_NOT_ALLOWED(6),
    CONTENT_MISMATCH(7),
    FILE_TOO_LARGE(8),
    READ_FAILED(9),
    /** Composite upload: {@code translations} multipart field is not valid JSON. */
    TRANSLATIONS_JSON_INVALID(10);

    private final int displayPriority;

    MediaUploadErrorCode(int displayPriority) {
        this.displayPriority = displayPriority;
    }

    /**
     * Lower value wins when selecting a single user-facing error from multiple validation issues.
     */
    public int displayPriority() {
        return displayPriority;
    }
}
