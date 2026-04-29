package com.backend.domain.exception;

import java.util.Arrays;
import java.util.List;

import com.backend.domain.enums.MediaUploadErrorCode;

/**
 * Validation failure for media upload; carries a stable {@link MediaUploadErrorCode} for i18n mapping.
 */
public final class MediaUploadValidationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    private final MediaUploadErrorCode errorCode;
    private final Object[] messageArgs;

    public MediaUploadValidationException(MediaUploadErrorCode errorCode) {
        this(errorCode, List.of());
    }

    /**
     * Preferred when args may be dynamic length (avoid varargs/Object[] ambiguity).
     */
    public MediaUploadValidationException(MediaUploadErrorCode errorCode, List<Object> messageArgsList) {
        super(toDebugMessage(errorCode, normalizeList(messageArgsList)));
        this.errorCode = errorCode;
        Object[] normalized = normalizeList(messageArgsList);
        this.messageArgs = normalized;
    }

    /**
     * Convenience for small call sites ({@code MIME_TYPE_NOT_ALLOWED} with one MIME, etc.).
     */
    public MediaUploadValidationException(MediaUploadErrorCode errorCode, Object... messageArgs) {
        super(toDebugMessage(errorCode, copyVarargs(messageArgs)));
        this.errorCode = errorCode;
        this.messageArgs = copyVarargs(messageArgs);
    }

    public MediaUploadErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getMessageArgs() {
        return Arrays.copyOf(messageArgs, messageArgs.length);
    }

    private static Object[] copyVarargs(Object[] messageArgs) {
        if (messageArgs == null || messageArgs.length == 0) {
            return new Object[0];
        }
        return Arrays.copyOf(messageArgs, messageArgs.length);
    }

    private static Object[] normalizeList(List<Object> messageArgsList) {
        if (messageArgsList == null || messageArgsList.isEmpty()) {
            return new Object[0];
        }
        return messageArgsList.toArray(new Object[0]);
    }

    private static String toDebugMessage(MediaUploadErrorCode code, Object[] args) {
        if (args == null || args.length == 0) {
            return code.name();
        }
        return code.name() + ": " + Arrays.toString(args);
    }
}
