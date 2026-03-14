package com.backend.presentation.dto.request;

import com.backend.domain.enums.RobotsMetaTag;
import com.backend.shared.constants.ValidationConstants;
import com.backend.shared.validation.SecureUrl;
import com.backend.shared.validation.SecureUrlType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SiteSettingsGlobalDto(
        @Email(message = "validation.email") String contactEmail,

        @Pattern(regexp = ValidationConstants.PHONE_GLOBAL_PATTERN, message = "validation.phone.pattern") String contactPhone,

        @Pattern(regexp = ValidationConstants.PHONE_GLOBAL_PATTERN, message = "validation.phone.pattern") String whatsappPhone,

        @SecureUrl(type = SecureUrlType.CANONICAL, message = "validation.url.canonical") String canonicalBaseUrl,

        RobotsMetaTag robots,

        AddressDto address,

        SocialDto social,

        @Size(max = 255) String logoMediaUid,

        @Size(max = 255) String logoDarkMediaUid) {

    public SiteSettingsGlobalDto {
        contactEmail = sanitize(contactEmail);
        contactPhone = sanitize(contactPhone);
        whatsappPhone = sanitize(whatsappPhone);
        canonicalBaseUrl = sanitize(canonicalBaseUrl);
        logoMediaUid = sanitize(logoMediaUid);
        logoDarkMediaUid = sanitize(logoDarkMediaUid);
    }

    private static String sanitize(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record AddressDto(
            @Size(max = 200) String line1,
            @Size(max = 200) String line2,
            @Size(max = 100) String city,
            @Size(max = 100) String state,
            @Size(max = 20) String postalCode,
            @Size(max = 100) String country,
            @Size(max = 500) String mapEmbedUrl) {

        public AddressDto {
            line1 = sanitize(line1);
            line2 = sanitize(line2);
            city = sanitize(city);
            state = sanitize(state);
            postalCode = sanitize(postalCode);
            country = sanitize(country);
            mapEmbedUrl = sanitize(mapEmbedUrl);
        }
    }

    public record SocialDto(
            @Size(max = 200) String facebook,
            @Size(max = 200) String instagram,
            @Size(max = 200) String x,
            @Size(max = 200) String linkedin,
            @Size(max = 200) String youtube,
            @Size(max = 200) String tiktok) {

        public SocialDto {
            facebook = sanitize(facebook);
            instagram = sanitize(instagram);
            x = sanitize(x);
            linkedin = sanitize(linkedin);
            youtube = sanitize(youtube);
            tiktok = sanitize(tiktok);
        }
    }
}
