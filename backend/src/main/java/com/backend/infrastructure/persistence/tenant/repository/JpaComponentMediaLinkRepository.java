package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ComponentMediaLink;

/**
 * JPA Repository for ComponentMediaLink entity.
 */
public interface JpaComponentMediaLinkRepository extends JpaRepository<ComponentMediaLink, Long> {

  List<ComponentMediaLink> findByMediaId(Long mediaId);

  List<ComponentMediaLink> findByComponentId(Long componentId);

  List<ComponentMediaLink> findByEntryId(Long entryId);

  List<ComponentMediaLink> findByResponsiveSetId(Long responsiveSetId);

  @Query("SELECT COUNT(c) > 0 FROM ComponentMediaLink c WHERE c.componentId = :componentId " +
      "AND c.mediaId = :mediaId AND c.linkType = :linkType " +
      "AND (c.entryId = :entryId OR (c.entryId IS NULL AND :entryId IS NULL))")
  boolean existsByComponentIdAndMediaIdAndLinkTypeAndEntryId(
      @Param("componentId") Long componentId,
      @Param("mediaId") Long mediaId,
      @Param("linkType") ComponentMediaLink.LinkType linkType,
      @Param("entryId") Long entryId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("DELETE FROM ComponentMediaLink c WHERE c.componentId = :componentId AND c.entryId IS NULL")
  void deleteComponentLevelByComponentId(@Param("componentId") Long componentId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("DELETE FROM ComponentMediaLink c WHERE c.componentId = :componentId")
  void deleteByComponentId(@Param("componentId") Long componentId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("DELETE FROM ComponentMediaLink c WHERE c.entryId = :entryId")
  void deleteByEntryId(@Param("entryId") Long entryId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("DELETE FROM ComponentMediaLink c WHERE c.responsiveSetId = :responsiveSetId")
  void deleteByResponsiveSetId(@Param("responsiveSetId") Long responsiveSetId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("DELETE FROM ComponentMediaLink c WHERE c.componentId = :componentId AND c.mediaId = :mediaId")
  void deleteByComponentIdAndMediaId(
      @Param("componentId") Long componentId,
      @Param("mediaId") Long mediaId);

  @Query("SELECT COUNT(c) FROM ComponentMediaLink c WHERE c.mediaId = :mediaId")
  long countByMediaId(@Param("mediaId") Long mediaId);
}
