package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.PageSlot;
import com.backend.domain.repository.PageSlotRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PageSlotRepositoryImpl implements PageSlotRepository {

  private final PageSlotJpaRepository jpaRepository;

  @Override
  public Optional<PageSlot> findByUid(String uid) {
    return jpaRepository.findByUid(uid);
  }

  @Override
  public Optional<PageSlot> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<PageSlot> findByPageId(Long pageId) {
    return jpaRepository.findByPageIdOrderBySortOrder(pageId);
  }

  @Override
  public List<PageSlot> findSharedSlots() {
    return jpaRepository.findSharedSlots();
  }

  @Override
  public Optional<PageSlot> findByPageIdAndSlotName(Long pageId, String slotName) {
    return jpaRepository.findByPageIdAndSlotName(pageId, slotName);
  }

  @Override
  public Optional<PageSlot> findSharedSlotBySlotName(String slotName) {
    return jpaRepository.findSharedSlotBySlotName(slotName);
  }

  @Override
  public PageSlot save(PageSlot pageSlot) {
    return jpaRepository.save(pageSlot);
  }

  @Override
  public List<PageSlot> saveAll(List<PageSlot> pageSlots) {
    return jpaRepository.saveAll(pageSlots);
  }

  @Override
  public void delete(PageSlot pageSlot) {
    jpaRepository.delete(pageSlot);
  }

  @Override
  public boolean existsByPageIdAndSlotName(Long pageId, String slotName) {
    return jpaRepository.existsByPageIdAndSlotName(pageId, slotName);
  }

  @Override
  public List<PageSlot> findByPageIdInAndSlotName(List<Long> pageIds, String slotName) {
    return jpaRepository.findByPageIdInAndSlotName(pageIds, slotName);
  }
}
