package com.backend.application.dto.outreach;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateOutreachTemplateRequest(
    @NotBlank String name,
    @NotBlank @Size(max = 500) String subject,
    @NotBlank String content,
    @NotBlank @Pattern(regexp = "(?i)(TR|EN)") String language,
    Boolean isActive
) {
}
