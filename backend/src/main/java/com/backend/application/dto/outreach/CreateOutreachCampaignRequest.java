package com.backend.application.dto.outreach;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOutreachCampaignRequest(
    @NotBlank String name,
    @Positive Long templateId,
    @NotEmpty @Size(max = 500) List<@NotNull @Positive Long> contactIds,
    String subjectOverride
) {
}
