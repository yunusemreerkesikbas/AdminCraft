package com.backend.application.cms.preview;

import java.time.Instant;

public record PreviewTicketResult(
    String ticket,
    Instant expiresAt,
    String storefrontBaseUrl) {}
