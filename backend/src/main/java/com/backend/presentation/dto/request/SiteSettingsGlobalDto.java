package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SiteSettingsGlobalDto(
    @Email(message = "validation.email") String contactEmail,

    @Size(max = 30, message = "validation.phone.length") String contactPhone,

    @Size(max = 30, message = "validation.phone.length") String whatsappPhone,

    String address, // JSON string (validated in service)

    String businessHours, // JSON string

    String social, // JSON string

    @Pattern(regexp = "^(https?://).*$", message = "validation.url") String canonicalBaseUrl,

    @Size(max = 50, message = "validation.length") String robots) {
}
