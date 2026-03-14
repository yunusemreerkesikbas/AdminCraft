package com.backend.domain.repository;

import java.util.List;

import com.backend.domain.entity.ComponentMediaLink;

/**
 * Repository interface for ComponentMediaLink entity.
 * Used for tracking media usage in components and entries.
 */
public interface ComponentMediaLinkRepository {

  ComponentMediaLink save(ComponentMediaLink entity);

  List<ComponentMediaLink> saveAll(List<ComponentMediaLink> entities);

  /**
   * Find all links for a specific media.
   * Used for "Linked Components" feature.
   */
  List<ComponentMediaLink> findByMediaId(Long mediaId);

  /**
   * Find all links for a specific component.
   */
  List<ComponentMediaLink> findByComponentId(Long componentId);

  /**
   * Find all links for a specific entry.
   */
  List<ComponentMediaLink> findByEntryId(Long entryId);

  /**
   * Find all links for a responsive media set.
   */
  List<ComponentMediaLink> findByResponsiveSetId(Long responsiveSetId);

  /**
   * Check if a link already exists for the given component + media + link type + entry.
   * Used to prevent duplicate links when DB uniqueness is relaxed for NULL entry_id.
   */
  boolean existsByComponentIdAndMediaIdAndLinkTypeAndEntryId(
      Long componentId,
      Long mediaId,
      ComponentMediaLink.LinkType linkType,
      Long entryId);

  /**
   * Delete only component-level responsive links for a component.
   */
  void deleteComponentLevelByComponentId(Long componentId);

  /**
   * Delete all links for a component.
   */
  void deleteByComponentId(Long componentId);

  /**
   * Delete all links for an entry.
   */
  void deleteByEntryId(Long entryId);

  /**
   * Delete all links for a responsive set.
   */
  void deleteByResponsiveSetId(Long responsiveSetId);

  /**
   * Delete specific link by component and media.
   */
  void deleteByComponentIdAndMediaId(Long componentId, Long mediaId);

  /**
   * Count how many components/entries use a specific media.
   */
  long countByMediaId(Long mediaId);
}
