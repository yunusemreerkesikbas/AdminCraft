package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.SlotComponent;

public interface SlotComponentRepository {

  Optional<SlotComponent> findById(Long id);

  List<SlotComponent> findBySlotIdOrderBySortOrder(Long slotId);

  List<SlotComponent> findBySlotIdIn(List<Long> slotIds);

  Optional<SlotComponent> findBySlotIdAndComponentId(Long slotId, Long componentId);

  Optional<Integer> findMaxSortOrderBySlotId(Long slotId);

  SlotComponent save(SlotComponent slotComponent);

  List<SlotComponent> saveAll(List<SlotComponent> slotComponents);

  void delete(SlotComponent slotComponent);

  void deleteBySlotId(Long slotId);

  boolean existsBySlotIdAndComponentId(Long slotId, Long componentId);
}
