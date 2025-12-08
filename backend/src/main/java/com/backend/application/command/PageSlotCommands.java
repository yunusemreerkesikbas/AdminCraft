package com.backend.application.command;

import java.util.List;

public class PageSlotCommands {

  public record CreatePageSlotCommand(
      String slotName,
      String position,
      Integer sortOrder,
      Boolean isShared) {
  }

  public record AddComponentToSlotCommand(
      Long componentId) {
  }

  public record ReorderSlotComponentsCommand(
      List<Long> componentIds) {
  }
}

