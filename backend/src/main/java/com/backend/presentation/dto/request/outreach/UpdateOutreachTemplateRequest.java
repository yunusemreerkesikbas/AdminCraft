package com.backend.presentation.dto.request.outreach;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOutreachTemplateRequest(
    @NotBlank String name,
    @NotBlank @Size(max = 500) String subject,
    @NotBlank String content,
    @NotNull String language,
    Boolean isActive
) {
}
