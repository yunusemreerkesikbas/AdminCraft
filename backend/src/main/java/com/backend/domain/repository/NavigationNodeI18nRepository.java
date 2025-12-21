package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.NavigationNodeI18n;
import com.backend.domain.enums.Language;

@Repository
public interface NavigationNodeI18nRepository extends JpaRepository<NavigationNodeI18n, Long> {

  Optional<NavigationNodeI18n> findByNodeIdAndLanguage(Long nodeId, Language language);

  List<NavigationNodeI18n> findByNodeId(Long nodeId);

  List<NavigationNodeI18n> findByNodeIdIn(List<Long> nodeIds);

  @Modifying
  void deleteByNodeId(Long nodeId);

  boolean existsByNodeIdAndLanguage(Long nodeId, Language language);
}
