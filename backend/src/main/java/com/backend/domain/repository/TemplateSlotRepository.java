package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.TemplateSlot;

public interface TemplateSlotRepository {

  Optional<TemplateSlot> findById(Long id);

  List<TemplateSlot> findByTemplateId(Long templateId);

  Optional<TemplateSlot> findByTemplateIdAndSlotName(Long templateId, String slotName);

  TemplateSlot save(TemplateSlot templateSlot);

  void delete(TemplateSlot templateSlot);

  void deleteByTemplateIdAndSlotName(Long templateId, String slotName);

  boolean existsByTemplateIdAndSlotName(Long templateId, String slotName);
}
