package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.Objects;

public record ComponentRequest(
    @NotNull Long tenantId,
    @NotNull ComponentType type,
    @NotBlank @Size(max = 100) @Pattern(regexp = "^[a-z0-9._-]+$") String key,
    ComponentStatus status,
    Boolean visible,
    Integer sortOrder,
    @Size(max = 255) String styleClasses,
    @NotNull Map<String, I18nPayload> translations) {

  public ComponentRequest {
    Objects.requireNonNull(tenantId, "tenantId must not be null");
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(translations, "translations must not be null");
  }

  public static record I18nPayload(
      @Size(max = 200) String title,
      @Size(max = 300) String subtitle,
      String data) {
  }
}
