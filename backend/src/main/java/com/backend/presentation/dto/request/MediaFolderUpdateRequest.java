package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a media folder.
 */
public record MediaFolderUpdateRequest(
    @NotBlank(message = "Folder name is required") @Size(max = 255, message = "Name must be at most 255 characters") String name) {
}
