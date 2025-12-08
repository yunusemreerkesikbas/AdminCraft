package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.SlotComponent;
import com.backend.domain.repository.SlotComponentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SlotComponentRepositoryImpl implements SlotComponentRepository {

  private final SlotComponentJpaRepository jpaRepository;

  @Override
  public Optional<SlotComponent> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<SlotComponent> findBySlotIdOrderBySortOrder(Long slotId) {
    return jpaRepository.findBySlotIdOrderBySortOrder(slotId);
  }

  @Override
  public List<SlotComponent> findBySlotIdIn(List<Long> slotIds) {
    return jpaRepository.findBySlotIdIn(slotIds);
  }

  @Override
  public Optional<SlotComponent> findBySlotIdAndComponentId(Long slotId, Long componentId) {
    return jpaRepository.findBySlotIdAndComponentId(slotId, componentId);
  }

  @Override
  public Optional<Integer> findMaxSortOrderBySlotId(Long slotId) {
    return jpaRepository.findMaxSortOrderBySlotId(slotId);
  }

  @Override
  public SlotComponent save(SlotComponent slotComponent) {
    return jpaRepository.save(slotComponent);
  }

  @Override
  public List<SlotComponent> saveAll(List<SlotComponent> slotComponents) {
    return jpaRepository.saveAll(slotComponents);
  }

  @Override
  public void delete(SlotComponent slotComponent) {
    jpaRepository.delete(slotComponent);
  }

  @Override
  @Transactional
  public void deleteBySlotId(Long slotId) {
    jpaRepository.deleteBySlotId(slotId);
  }

  @Override
  public boolean existsBySlotIdAndComponentId(Long slotId, Long componentId) {
    return jpaRepository.existsBySlotIdAndComponentId(slotId, componentId);
  }
}
