package com.backend.application.dto.impex;

public record StatementResult(
    int index,
    String preview,
    boolean success,
    int affectedRows,
    String errorMessage
) {}
