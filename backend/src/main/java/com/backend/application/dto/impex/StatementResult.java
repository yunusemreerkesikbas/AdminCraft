package com.backend.application.dto.impex;

public record StatementResult(
    int index,
    String preview,
    boolean success,
    int affectedRows,
    String errorMessage
) {
    private static final int MAX_ERROR_LENGTH = 500;

    public static StatementResult success(int index, String preview, int rows) {
        return new StatementResult(index, preview, true, rows, null);
    }

    public static StatementResult failure(int index, String preview, String message) {
        String truncated = (message != null && message.length() > MAX_ERROR_LENGTH)
            ? message.substring(0, MAX_ERROR_LENGTH)
            : message;
        return new StatementResult(index, preview, false, 0, truncated);
    }
}
