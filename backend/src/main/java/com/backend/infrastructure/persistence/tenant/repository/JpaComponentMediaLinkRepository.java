package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.ComponentMediaLink;

/**
 * JPA Repository for ComponentMediaLink entity.
 */
public interface JpaComponentMediaLinkRepository extends JpaRepository<ComponentMediaLink, Long> {

  List<ComponentMediaLink> findByMediaId(Long mediaId);

  List<ComponentMediaLink> findByComponentId(Long componentId);

  List<ComponentMediaLink> findByEntryId(Long entryId);

  List<ComponentMediaLink> findByResponsiveSetId(Long responsiveSetId);

  boolean existsByComponentIdAndMediaIdAndLinkTypeAndEntryId(
      Long componentId,
      Long mediaId,
      ComponentMediaLink.LinkType linkType,
      Long entryId);

  @Modifying
  @Query("DELETE FROM ComponentMediaLink c WHERE c.componentId = :componentId")
  void deleteByComponentId(@Param("componentId") Long componentId);

  @Modifying
  @Query("DELETE FROM ComponentMediaLink c WHERE c.entryId = :entryId")
  void deleteByEntryId(@Param("entryId") Long entryId);

  @Modifying
  @Query("DELETE FROM ComponentMediaLink c WHERE c.responsiveSetId = :responsiveSetId")
  void deleteByResponsiveSetId(@Param("responsiveSetId") Long responsiveSetId);

  @Modifying
  @Query("DELETE FROM ComponentMediaLink c WHERE c.componentId = :componentId AND c.mediaId = :mediaId")
  void deleteByComponentIdAndMediaId(
      @Param("componentId") Long componentId,
      @Param("mediaId") Long mediaId);

  long countByMediaId(Long mediaId);
}
