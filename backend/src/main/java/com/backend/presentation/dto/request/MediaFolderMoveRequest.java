package com.backend.presentation.dto.request;

/**
 * Request DTO for moving a media folder to a new parent.
 */
public record MediaFolderMoveRequest(
    Long newParentId // nullable to move to root
) {
}
