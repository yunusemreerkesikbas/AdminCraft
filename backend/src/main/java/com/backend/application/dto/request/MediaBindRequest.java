package com.backend.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MediaBindRequest(
    @NotNull TargetType targetType,
    @NotNull @Min(1) Long targetId,
    ResponsiveTarget responsiveTarget) {

  public enum TargetType {
    COMPONENT,
    ENTRY
  }

  public enum ResponsiveTarget {
    DESKTOP,
    MOBILE,
    BOTH
  }
}
