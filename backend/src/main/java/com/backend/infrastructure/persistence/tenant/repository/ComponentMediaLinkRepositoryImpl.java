package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ComponentMediaLink;
import com.backend.domain.repository.ComponentMediaLinkRepository;

import lombok.RequiredArgsConstructor;

/**
 * Repository implementation for ComponentMediaLink entity.
 */
@Repository
@RequiredArgsConstructor
public class ComponentMediaLinkRepositoryImpl implements ComponentMediaLinkRepository {

  private final JpaComponentMediaLinkRepository jpaRepository;

  @Override
  public ComponentMediaLink save(ComponentMediaLink entity) {
    return jpaRepository.save(entity);
  }

  @Override
  public List<ComponentMediaLink> saveAll(List<ComponentMediaLink> entities) {
    return jpaRepository.saveAll(entities);
  }

  @Override
  public List<ComponentMediaLink> findByMediaId(Long mediaId) {
    return jpaRepository.findByMediaId(mediaId);
  }

  @Override
  public List<ComponentMediaLink> findByComponentId(Long componentId) {
    return jpaRepository.findByComponentId(componentId);
  }

  @Override
  public List<ComponentMediaLink> findByEntryId(Long entryId) {
    return jpaRepository.findByEntryId(entryId);
  }

  @Override
  public List<ComponentMediaLink> findByResponsiveSetId(Long responsiveSetId) {
    return jpaRepository.findByResponsiveSetId(responsiveSetId);
  }

  @Override
  public boolean existsByComponentIdAndMediaIdAndLinkTypeAndEntryId(
      Long componentId,
      Long mediaId,
      ComponentMediaLink.LinkType linkType,
      Long entryId) {
    return jpaRepository.existsByComponentIdAndMediaIdAndLinkTypeAndEntryId(
        componentId, mediaId, linkType, entryId);
  }

  @Override
  @Transactional
  public void deleteComponentLevelByComponentId(Long componentId) {
    jpaRepository.deleteComponentLevelByComponentId(componentId);
  }

  @Override
  @Transactional
  public void deleteByComponentId(Long componentId) {
    jpaRepository.deleteByComponentId(componentId);
  }

  @Override
  @Transactional
  public void deleteByEntryId(Long entryId) {
    jpaRepository.deleteByEntryId(entryId);
  }

  @Override
  @Transactional
  public void deleteByResponsiveSetId(Long responsiveSetId) {
    jpaRepository.deleteByResponsiveSetId(responsiveSetId);
  }

  @Override
  @Transactional
  public void deleteByComponentIdAndMediaId(Long componentId, Long mediaId) {
    jpaRepository.deleteByComponentIdAndMediaId(componentId, mediaId);
  }

  @Override
  public long countByMediaId(Long mediaId) {
    return jpaRepository.countByMediaId(mediaId);
  }
}
