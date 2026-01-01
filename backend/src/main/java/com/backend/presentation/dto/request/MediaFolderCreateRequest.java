package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a media folder.
 */
public record MediaFolderCreateRequest(
    @NotBlank(message = "Folder code is required") @Size(max = 100, message = "Code must be at most 100 characters") String code,

    @NotBlank(message = "Folder name is required") @Size(max = 255, message = "Name must be at most 255 characters") String name,

    Long parentId // nullable for root folder
) {
}
