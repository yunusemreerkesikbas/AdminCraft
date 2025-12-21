package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.entity.PageTemplate;

interface PageTemplateJpaRepository extends JpaRepository<PageTemplate, Long> {

  Optional<PageTemplate> findByUid(String uid);

  @EntityGraph(attributePaths = { "slots", "i18nContent" })
  Optional<PageTemplate> findWithSlotsById(Long id);

  @EntityGraph(attributePaths = { "slots", "i18nContent" })
  Optional<PageTemplate> findWithSlotsByUid(String uid);

  @EntityGraph(attributePaths = { "slots", "i18nContent" })
  List<PageTemplate> findByIsActiveTrueOrderByIdDesc();

  @EntityGraph(attributePaths = { "slots", "i18nContent" })
  List<PageTemplate> findAllByOrderByIdDesc();

  boolean existsByUid(String uid);
}
