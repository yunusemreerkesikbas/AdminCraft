package com.backend.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatchPlatformSettingsRequest(
    @Size(max = 100) String platformName,
    @Size(min = 2, max = 10)
    @Pattern(regexp = "^[A-Za-z-]{2,10}$")
    String defaultLanguage,
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Za-z]{3}$")
    String defaultCurrency,
    @Email @Size(max = 255) String emailFromAddress,
    @Size(max = 100) String emailFromName
) {}
