package com.backend.domain.exception;

public final class MediaExceptions {

    private MediaExceptions() {}

    public static class MediaNotFoundException extends RuntimeException {
        public MediaNotFoundException(Long id) {
            super("Media not found: " + id);
        }

        public MediaNotFoundException(String uid) {
            super("Media not found: " + uid);
        }
    }

    public static class InvalidFileException extends RuntimeException {
        public InvalidFileException(String message) {
            super(message);
        }
    }

    public static class MediaProcessingException extends RuntimeException {
        public MediaProcessingException(String message) {
            super(message);
        }

        public MediaProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class UnsupportedFormatException extends RuntimeException {
        public UnsupportedFormatException(String formatCode) {
            super("Unsupported format: " + formatCode);
        }
    }

    public static class ContainerNotFoundException extends RuntimeException {
        public ContainerNotFoundException(Long masterId) {
            super("Container not found for master media: " + masterId);
        }
    }
}
