package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.ResponsiveMediaSet;

/**
 * JPA Repository for ResponsiveMediaSet entity.
 */
public interface JpaResponsiveMediaSetRepository extends JpaRepository<ResponsiveMediaSet, Long> {

  Optional<ResponsiveMediaSet> findByUid(String uid);

  Optional<ResponsiveMediaSet> findByCode(String code);

  boolean existsByCode(String code);

  List<ResponsiveMediaSet> findByDesktopMediaId(Long mediaId);

  List<ResponsiveMediaSet> findByMobileMediaId(Long mediaId);

  @Query("SELECT r FROM ResponsiveMediaSet r WHERE r.desktopMedia.id = :mediaId OR r.mobileMedia.id = :mediaId")
  List<ResponsiveMediaSet> findByMediaId(@Param("mediaId") Long mediaId);

  List<ResponsiveMediaSet> findByIdIn(java.util.Collection<Long> ids);

  /**
   * Batch fetch responsive sets with eagerly loaded media and translations.
   * Prevents N+1 queries in delivery services.
   */
  @EntityGraph(attributePaths = {"desktopMedia", "desktopMedia.translations", "mobileMedia", "mobileMedia.translations"})
  @Query("SELECT r FROM ResponsiveMediaSet r WHERE r.id IN :ids")
  List<ResponsiveMediaSet> findByIdInWithMedia(@Param("ids") java.util.Collection<Long> ids);
}
