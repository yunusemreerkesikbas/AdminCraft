package com.backend.application.dto.impex;

import java.time.LocalDateTime;
import java.util.List;

public record ImpExResult(
    String status,
    int totalStatements,
    int executedStatements,
    int failedStatements,
    List<StatementResult> results,
    LocalDateTime executedAt
) {}
