package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Min;

public record PreviewTicketIssueRequest(@Min(1) Long pageId) {}
