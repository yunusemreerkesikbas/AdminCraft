package com.backend.application.dto.delivery;

import java.time.LocalDateTime;

public record SitemapPageEntry(
    String uid,
    String typeCode,
    String canonicalUrl,
    LocalDateTime updatedAt) {}
