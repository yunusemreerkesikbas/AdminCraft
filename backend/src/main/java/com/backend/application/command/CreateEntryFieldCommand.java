package com.backend.application.command;

import com.backend.domain.enums.EntryFieldType;
import java.math.BigDecimal;

public record CreateEntryFieldCommand(
    Long componentTypeId,
    String fieldKey,
    EntryFieldType fieldType,
    Boolean isRequired,
    Integer maxLength,
    BigDecimal minValue,
    BigDecimal maxValue) {
  public CreateEntryFieldCommand {
    if (componentTypeId == null) {
      throw new IllegalArgumentException("Component type ID is required");
    }
    if (fieldKey == null || fieldKey.isBlank()) {
      throw new IllegalArgumentException("Field key is required");
    }
    if (fieldType == null) {
      throw new IllegalArgumentException("Field type is required");
    }
  }
}
