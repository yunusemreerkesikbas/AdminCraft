package com.backend.domain.exception;

import java.util.Arrays;
import java.util.List;

import com.backend.domain.enums.MediaUploadErrorCode;

public final class MediaUploadValidationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    private final MediaUploadErrorCode errorCode;
    private final Object[] messageArgs;

    public MediaUploadValidationException(MediaUploadErrorCode errorCode) {
        this(errorCode, List.of());
    }

    public MediaUploadValidationException(MediaUploadErrorCode errorCode, List<Object> messageArgsList) {
        this(errorCode, normalizeList(messageArgsList));
    }

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
