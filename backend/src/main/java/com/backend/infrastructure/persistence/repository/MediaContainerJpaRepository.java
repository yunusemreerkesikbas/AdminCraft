package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MediaContainer;

@Repository
public interface MediaContainerJpaRepository extends JpaRepository<MediaContainer, Long> {

  @EntityGraph(attributePaths = { "items", "items.format", "items.media" })
  Optional<MediaContainer> findByUid(String uid);

  @EntityGraph(attributePaths = { "items", "items.format", "items.media" })
  Optional<MediaContainer> findByCode(String code);

  @EntityGraph(attributePaths = { "items", "items.format", "items.media", "masterMedia" })
  Optional<MediaContainer> findByMasterMediaId(Long masterMediaId);

  boolean existsByCode(String code);

  boolean existsByMasterMediaId(Long masterMediaId);
}
