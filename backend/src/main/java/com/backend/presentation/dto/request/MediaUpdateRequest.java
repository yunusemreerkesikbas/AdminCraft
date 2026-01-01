package com.backend.presentation.dto.request;

import java.util.List;

/**
 * Request DTO for updating media metadata.
 */
public record MediaUpdateRequest(
    Long folderId,
    Boolean isPublic,
    List<String> tags) {
}
