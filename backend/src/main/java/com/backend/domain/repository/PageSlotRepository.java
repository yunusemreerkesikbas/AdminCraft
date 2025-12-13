package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.PageSlot;

public interface PageSlotRepository {

  Optional<PageSlot> findByUid(String uid);

  Optional<PageSlot> findById(Long id);

  List<PageSlot> findByPageId(Long pageId);

  List<PageSlot> findSharedSlots();

  Optional<PageSlot> findByPageIdAndSlotName(Long pageId, String slotName);

  Optional<PageSlot> findSharedSlotBySlotName(String slotName);

  PageSlot save(PageSlot pageSlot);

  List<PageSlot> saveAll(List<PageSlot> pageSlots);

  void delete(PageSlot pageSlot);

  boolean existsByPageIdAndSlotName(Long pageId, String slotName);
}
