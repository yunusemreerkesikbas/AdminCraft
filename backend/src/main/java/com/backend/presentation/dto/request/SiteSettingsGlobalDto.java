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

    SocialDto social) {

  public SiteSettingsGlobalDto {
    contactEmail = contactEmail != null ? contactEmail.trim() : null;
    contactPhone = contactPhone != null ? contactPhone.trim() : null;
    whatsappPhone = whatsappPhone != null ? whatsappPhone.trim() : null;
    canonicalBaseUrl = canonicalBaseUrl != null ? canonicalBaseUrl.trim() : null;
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
      line1 = line1 != null ? line1.trim() : null;
      line2 = line2 != null ? line2.trim() : null;
      city = city != null ? city.trim() : null;
      state = state != null ? state.trim() : null;
      postalCode = postalCode != null ? postalCode.trim() : null;
      country = country != null ? country.trim() : null;
      mapEmbedUrl = mapEmbedUrl != null ? mapEmbedUrl.trim() : null;
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
      facebook = facebook != null ? facebook.trim() : null;
      instagram = instagram != null ? instagram.trim() : null;
      x = x != null ? x.trim() : null;
      linkedin = linkedin != null ? linkedin.trim() : null;
      youtube = youtube != null ? youtube.trim() : null;
      tiktok = tiktok != null ? tiktok.trim() : null;
    }
  }
}
