package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MediaContainer;

@Repository
public interface MediaContainerJpaRepository extends JpaRepository<MediaContainer, Long> {

  Optional<MediaContainer> findByUid(String uid);

  Optional<MediaContainer> findByCode(String code);

  Optional<MediaContainer> findByMasterMediaId(Long masterMediaId);

  boolean existsByCode(String code);

  boolean existsByMasterMediaId(Long masterMediaId);
}
