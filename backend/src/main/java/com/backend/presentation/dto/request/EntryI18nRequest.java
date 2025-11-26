package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Size;
import java.util.Map;

public record EntryI18nRequest(
    @Size(max = 255)
    String title,

    String description,

    @Size(max = 500)
    String imageUrl,

    @Size(max = 100)
    String buttonText,

    @Size(max = 500)
    String buttonUrl,

    Map<String, Object> customFields
) {}



