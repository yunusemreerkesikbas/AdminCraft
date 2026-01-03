package com.backend.presentation.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for batch format generation.
 * Allows generating multiple format variants in a single request.
 */
public record GenerateFormatsRequest(
    @NotEmpty(message = "validation.media.formats.required") @Size(max = 10, message = "validation.media.formats.max") List<@Valid GenerateFormatRequest> formats) {
}
