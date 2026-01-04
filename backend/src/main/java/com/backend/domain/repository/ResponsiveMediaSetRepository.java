package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.ResponsiveMediaSet;

/**
 * Repository interface for ResponsiveMediaSet entity.
 */
public interface ResponsiveMediaSetRepository {

  ResponsiveMediaSet save(ResponsiveMediaSet entity);

  Optional<ResponsiveMediaSet> findById(Long id);

  Optional<ResponsiveMediaSet> findByUid(String uid);

  Optional<ResponsiveMediaSet> findByCode(String code);

  void deleteById(Long id);

  void delete(ResponsiveMediaSet entity);

  boolean existsByCode(String code);

  /**
   * Find all responsive sets that use a specific media (desktop or mobile).
   */
  List<ResponsiveMediaSet> findByMediaId(Long mediaId);

  /**
   * Find responsive sets by desktop media ID.
   */
  List<ResponsiveMediaSet> findByDesktopMediaId(Long mediaId);

  /**
   * Find responsive sets by mobile media ID.
   */
  List<ResponsiveMediaSet> findByMobileMediaId(Long mediaId);

  /**
   * Batch fetch responsive sets by IDs.
   * Used for optimized N+1 query prevention in delivery services.
   */
  List<ResponsiveMediaSet> findByIdIn(java.util.Collection<Long> ids);

  /**
   * Batch fetch responsive sets with eagerly loaded media and translations.
   * Prevents N+1 queries in delivery services.
   */
  List<ResponsiveMediaSet> findByIdInWithMedia(java.util.Collection<Long> ids);
}
