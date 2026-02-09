package com.backend.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record PatchPlatformSettingsRequest(
    @Size(max = 100) String platformName,
    String defaultLanguage,
    String defaultCurrency,
    @Email @Size(max = 255) String emailFromAddress,
    @Size(max = 100) String emailFromName
) {}
