package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MediaI18n;
import com.backend.domain.enums.Language;

@Repository
public interface MediaI18nJpaRepository extends JpaRepository<MediaI18n, Long> {

  Optional<MediaI18n> findByUid(String uid);

  List<MediaI18n> findByMediaId(Long mediaId);

  Optional<MediaI18n> findByMediaIdAndLanguage(Long mediaId, Language language);

  boolean existsByMediaIdAndLanguage(Long mediaId, Language language);

  @Modifying
  void deleteByMediaId(Long mediaId);

  @Modifying
  void deleteByMediaIdAndLanguage(Long mediaId, Language language);

  long countByMediaId(Long mediaId);
}
