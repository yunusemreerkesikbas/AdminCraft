package com.backend.application.service;

import java.util.List;

import com.backend.application.command.PageSlotCommands.AddComponentToSlotCommand;
import com.backend.application.command.PageSlotCommands.CreatePageSlotCommand;
import com.backend.application.command.PageSlotCommands.ReorderSlotComponentsCommand;
import com.backend.application.dto.slot.PageSlotDto;

public interface PageSlotService {

  PageSlotDto createSlot(Long pageId, CreatePageSlotCommand command);

  List<PageSlotDto> getSlotsByPageId(Long pageId);

  List<PageSlotDto> getSharedSlots();

  void deleteSlot(Long pageId, String slotName);

  void addComponentToSlot(Long pageId, String slotName, AddComponentToSlotCommand command);

  void removeComponentFromSlot(Long pageId, String slotName, Long componentId);

  void reorderComponents(Long pageId, String slotName, ReorderSlotComponentsCommand command);
}
