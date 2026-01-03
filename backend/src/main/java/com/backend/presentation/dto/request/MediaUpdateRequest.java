package com.backend.presentation.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating media metadata.
 */
public record MediaUpdateRequest(
    Boolean isPublic,
    @Size(max = 20, message = "Maximum 20 tags allowed") List<@NotBlank(message = "Tag cannot be blank") String> tags) {
}
