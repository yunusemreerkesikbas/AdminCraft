package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.PageTemplate;

interface PageTemplateJpaRepository extends JpaRepository<PageTemplate, Long>, JpaSpecificationExecutor<PageTemplate> {

  Optional<PageTemplate> findByUid(String uid);

  @EntityGraph(attributePaths = { "slots", "i18nContent" })
  Optional<PageTemplate> findWithSlotsById(Long id);

  @EntityGraph(attributePaths = { "slots", "i18nContent" })
  Optional<PageTemplate> findWithSlotsByUid(String uid);

  @EntityGraph(attributePaths = { "slots", "i18nContent" })
  List<PageTemplate> findByIsActiveTrueOrderByIdDesc();

  @EntityGraph(attributePaths = { "slots", "i18nContent" })
  List<PageTemplate> findAllByOrderByIdDesc();

  @EntityGraph(attributePaths = { "slots", "i18nContent" })
  @Override
  Page<PageTemplate> findAll(Pageable pageable);

  boolean existsByUid(String uid);

  /**
   * Paginated search across page templates.
   * Searches uid field and i18n name/description via LEFT JOIN.
   * <p>
   * The {@code query} parameter must contain at least 2 characters; shorter
   * queries
   * are rejected by the service layer before invoking this repository method.
   */
  @EntityGraph(attributePaths = { "slots", "i18nContent" })
  @Query("SELECT DISTINCT t FROM PageTemplate t " +
      "LEFT JOIN t.i18nContent i18n " +
      "WHERE LOWER(t.uid) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
      "(i18n IS NOT NULL AND (LOWER(i18n.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
      "LOWER(i18n.description) LIKE LOWER(CONCAT('%', :query, '%'))))")
  Page<PageTemplate> searchByQuery(@Param("query") String query, Pageable pageable);
}
