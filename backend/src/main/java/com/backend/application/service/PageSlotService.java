package com.backend.application.service;

import java.util.List;

import com.backend.application.command.PageSlotCommands.AddComponentToSlotCommand;
import com.backend.application.command.PageSlotCommands.CreatePageSlotCommand;
import com.backend.application.command.PageSlotCommands.ReorderSlotComponentsCommand;
import com.backend.presentation.dto.response.PageSlotResponse;

public interface PageSlotService {

  PageSlotResponse createSlot(Long pageId, CreatePageSlotCommand command);

  List<PageSlotResponse> getSlotsByPageId(Long pageId);

  List<PageSlotResponse> getSharedSlots();

  void deleteSlot(Long pageId, String slotName);

  void addComponentToSlot(Long pageId, String slotName, AddComponentToSlotCommand command);

  void removeComponentFromSlot(Long pageId, String slotName, Long componentId);

  void reorderComponents(Long pageId, String slotName, ReorderSlotComponentsCommand command);
}
