package com.backend.presentation.dto.request.outreach;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateOutreachCampaignRequest(
    @NotBlank String name,
    @NotNull Long templateId,
    @NotEmpty List<Long> contactIds,
    String subjectOverride
) {
}
