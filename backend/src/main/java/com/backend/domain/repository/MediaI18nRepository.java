package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.MediaI18n;
import com.backend.domain.enums.Language;

/**
 * Repository interface for MediaI18n entity.
 */
public interface MediaI18nRepository {

  MediaI18n save(MediaI18n mediaI18n);

  Optional<MediaI18n> findById(Long id);

  Optional<MediaI18n> findByUid(String uid);

  List<MediaI18n> findByMediaId(Long mediaId);

  Optional<MediaI18n> findByMediaIdAndLanguage(Long mediaId, Language language);

  boolean existsByMediaIdAndLanguage(Long mediaId, Language language);

  void deleteById(Long id);

  void deleteByMediaId(Long mediaId);

  void deleteByMediaIdAndLanguage(Long mediaId, Language language);

  long countByMediaId(Long mediaId);
}
