package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.Objects;

public record NavbarItemRequest(
    @NotBlank @Size(max = 100) @Pattern(regexp = "^[a-z0-9._-]+$") String uid,
    Long parentId,
    Boolean visible,
    Integer sortOrder,
    @Size(max = 3) Map<String, I18nPayload> translations) {

  public NavbarItemRequest {
    Objects.requireNonNull(uid, "uid must not be null");
    if (translations != null && translations.size() > 5) {
      throw new IllegalArgumentException("too.many.languages");
    }
  }

  public static record I18nPayload(
      @Size(max = 200) String title,
      String subtitle,
      @Size(max = 255) String url,
      @Size(max = 60) String seoTitle,
      @Size(max = 160) String seoDescription,
      @Size(max = 255) String seoKeywords) {
  }
}

