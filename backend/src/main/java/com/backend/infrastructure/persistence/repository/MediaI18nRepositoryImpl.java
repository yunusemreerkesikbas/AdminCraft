package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MediaI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.MediaI18nRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MediaI18nRepositoryImpl implements MediaI18nRepository {

  private final MediaI18nJpaRepository jpaRepository;

  @Override
  public MediaI18n save(MediaI18n mediaI18n) {
    return jpaRepository.save(mediaI18n);
  }

  @Override
  public Optional<MediaI18n> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Optional<MediaI18n> findByUid(String uid) {
    return jpaRepository.findByUid(uid);
  }

  @Override
  public List<MediaI18n> findByMediaId(Long mediaId) {
    return jpaRepository.findByMediaId(mediaId);
  }

  @Override
  public Optional<MediaI18n> findByMediaIdAndLanguage(Long mediaId, Language language) {
    return jpaRepository.findByMediaIdAndLanguage(mediaId, language);
  }

  @Override
  public boolean existsByMediaIdAndLanguage(Long mediaId, Language language) {
    return jpaRepository.existsByMediaIdAndLanguage(mediaId, language);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public void deleteByMediaId(Long mediaId) {
    jpaRepository.deleteByMediaId(mediaId);
  }

  @Override
  public void deleteByMediaIdAndLanguage(Long mediaId, Language language) {
    jpaRepository.deleteByMediaIdAndLanguage(mediaId, language);
  }

  @Override
  public long countByMediaId(Long mediaId) {
    return jpaRepository.countByMediaId(mediaId);
  }
}
