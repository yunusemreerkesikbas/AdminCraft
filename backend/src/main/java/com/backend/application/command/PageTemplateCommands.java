package com.backend.application.command;

import java.util.List;

public class PageTemplateCommands {

  public record CreatePageTemplateCommand(
      String name,
      String uid,
      String description,
      Boolean isActive) {
  }

  public record UpdatePageTemplateCommand(
      String name,
      String description,
      Boolean isActive) {
  }

  public record CreateTemplateSlotCommand(
      String slotName,
      String position,
      Integer sortOrder,
      Boolean isRequired,
      Integer maxComponents,
      List<String> allowedTypes) {
  }
}
