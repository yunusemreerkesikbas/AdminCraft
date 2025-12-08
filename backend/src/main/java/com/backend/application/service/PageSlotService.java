package com.backend.application.service;

import java.util.List;

import com.backend.presentation.dto.request.CreatePageSlotRequest;
import com.backend.presentation.dto.response.PageSlotResponse;

public interface PageSlotService {

  PageSlotResponse createSlot(Long pageId, CreatePageSlotRequest request);

  List<PageSlotResponse> getSlotsByPageId(Long pageId);

  List<PageSlotResponse> getSharedSlots();

  void deleteSlot(Long pageId, String slotName);

  void addComponentToSlot(Long pageId, String slotName, Long componentId);

  void removeComponentFromSlot(Long pageId, String slotName, Long componentId);

  void reorderComponents(Long pageId, String slotName, List<Long> componentIds);
}
