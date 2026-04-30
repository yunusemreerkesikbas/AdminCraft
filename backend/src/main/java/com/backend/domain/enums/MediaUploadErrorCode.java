package com.backend.domain.enums;

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
    TRANSLATIONS_JSON_INVALID(10);

    private final int displayPriority;

    MediaUploadErrorCode(int displayPriority) {
        this.displayPriority = displayPriority;
    }

    public int displayPriority() {
        return displayPriority;
    }
}
