package com.backend.presentation.dto.response;

import java.time.Instant;

/**
 * Response payload for {@code POST /api/cms/preview/tickets}.
 *
 * <p>{@code storefrontBaseUrl} is the origin (no trailing slash) the admin
 * SmartEdit shell should embed in its iframe; the admin appends the page slug
 * and {@code ?preview=&#123;ticket&#125;} query parameter.</p>
 */
public record PreviewTicketResponse(
    String ticket,
    Instant expiresAt,
    String storefrontBaseUrl) {
}
