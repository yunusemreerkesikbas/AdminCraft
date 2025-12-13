package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.entity.TemplateSlot;

interface TemplateSlotJpaRepository extends JpaRepository<TemplateSlot, Long> {

  List<TemplateSlot> findByTemplateIdOrderBySortOrder(Long templateId);

  Optional<TemplateSlot> findByTemplateIdAndSlotName(Long templateId, String slotName);

  void deleteByTemplateIdAndSlotName(Long templateId, String slotName);

  boolean existsByTemplateIdAndSlotName(Long templateId, String slotName);
}
