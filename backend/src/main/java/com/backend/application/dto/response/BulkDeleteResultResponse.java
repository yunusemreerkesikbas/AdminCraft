package com.backend.application.dto.response;

import java.util.List;

public record BulkDeleteResultResponse(
        int requestedCount,
        List<Long> deletedIds,
        List<Long> failedIds,
        List<BulkDeleteError> errors) {

    public record BulkDeleteError(
            Long id,
            String code,
            String message) {
    }
}
