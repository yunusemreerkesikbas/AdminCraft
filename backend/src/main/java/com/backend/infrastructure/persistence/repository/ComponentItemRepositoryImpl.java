package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentItem;
import com.backend.domain.repository.ComponentItemRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ComponentItemRepositoryImpl implements ComponentItemRepository {

  private final ComponentItemJpaRepository jpaRepository;

  public ComponentItemRepositoryImpl(ComponentItemJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public List<ComponentItem> findRootsByComponentId(Long componentId) {
    return jpaRepository.findRootsByComponentId(componentId);
  }

  @Override
  public List<ComponentItem> findAllByComponentId(Long componentId) {
    return jpaRepository.findAllByComponentId(componentId);
  }

  @Override
  public boolean existsByComponentIdAndUid(Long componentId, String uid) {
    return jpaRepository.existsByComponentIdAndUid(componentId, uid);
  }

  @Override
  public Optional<ComponentItem> findByIdAndComponentId(Long id, Long componentId) {
    return jpaRepository.findByIdAndComponentId(id, componentId);
  }

  @Override
  public ComponentItem save(ComponentItem item) {
    return jpaRepository.save(item);
  }

  @Override
  public void delete(ComponentItem item) {
    jpaRepository.delete(item);
  }
}

