package com.backend.presentation.dto.request;

import com.backend.shared.validation.SecureUrl;
import com.backend.shared.validation.SecureUrlType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SiteSettingsGlobalDto(
        @Email(message = "validation.email") String contactEmail,

        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "validation.phone.e164") String contactPhone,

        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "validation.phone.e164") String whatsappPhone,

        @SecureUrl(type = SecureUrlType.CANONICAL, message = "validation.url.canonical") String canonicalBaseUrl,

        @Pattern(regexp = "^(index|noindex),(follow|nofollow)$", message = "validation.robots") @Size(max = 50, message = "validation.length") String robots) {
}
