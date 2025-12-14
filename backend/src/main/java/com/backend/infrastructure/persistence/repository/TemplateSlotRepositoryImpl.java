package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.TemplateSlot;
import com.backend.domain.repository.TemplateSlotRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TemplateSlotRepositoryImpl implements TemplateSlotRepository {

  private final TemplateSlotJpaRepository jpaRepository;

  @Override
  public Optional<TemplateSlot> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<TemplateSlot> findByTemplateId(Long templateId) {
    return jpaRepository.findByTemplateIdOrderBySortOrder(templateId);
  }

  @Override
  public Optional<TemplateSlot> findByTemplateIdAndSlotName(Long templateId, String slotName) {
    return jpaRepository.findByTemplateIdAndSlotName(templateId, slotName);
  }

  @Override
  public TemplateSlot save(TemplateSlot templateSlot) {
    return jpaRepository.save(templateSlot);
  }

  @Override
  public void saveAll(List<TemplateSlot> templateSlots) {
    jpaRepository.saveAll(templateSlots);
  }

  @Override
  public void delete(TemplateSlot templateSlot) {
    jpaRepository.delete(templateSlot);
  }

  @Override
  @Transactional
  public void deleteByTemplateIdAndSlotName(Long templateId, String slotName) {
    jpaRepository.deleteByTemplateIdAndSlotName(templateId, slotName);
  }

  @Override
  public boolean existsByTemplateIdAndSlotName(Long templateId, String slotName) {
    return jpaRepository.existsByTemplateIdAndSlotName(templateId, slotName);
  }
}
