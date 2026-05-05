package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Min;

/**
 * Optional payload for {@code POST /api/cms/preview/tickets}.
 *
 * <p>When {@code pageId} is supplied the resulting ticket is informational only —
 * preview verification still resolves DRAFT for any page within the tenant.
 * Front-ends should pass it for traceability and to allow future "single-page
 * preview locks" without breaking the wire format.</p>
 */
public record IssuePreviewTicketRequest(
    @Min(1) Long pageId) {
}
