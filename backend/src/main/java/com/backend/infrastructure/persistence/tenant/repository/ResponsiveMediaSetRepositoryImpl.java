package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.repository.ResponsiveMediaSetRepository;

import lombok.RequiredArgsConstructor;

/**
 * Repository implementation for ResponsiveMediaSet entity.
 */
@Repository
@RequiredArgsConstructor
public class ResponsiveMediaSetRepositoryImpl implements ResponsiveMediaSetRepository {

  private final JpaResponsiveMediaSetRepository jpaRepository;

  @Override
  public ResponsiveMediaSet save(ResponsiveMediaSet entity) {
    return jpaRepository.save(entity);
  }

  @Override
  public Optional<ResponsiveMediaSet> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Optional<ResponsiveMediaSet> findByUid(String uid) {
    return jpaRepository.findByUid(uid);
  }

  @Override
  public Optional<ResponsiveMediaSet> findByCode(String code) {
    return jpaRepository.findByCode(code);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public void delete(ResponsiveMediaSet entity) {
    jpaRepository.delete(entity);
  }

  @Override
  public boolean existsByCode(String code) {
    return jpaRepository.existsByCode(code);
  }

  @Override
  public List<ResponsiveMediaSet> findByMediaId(Long mediaId) {
    return jpaRepository.findByMediaId(mediaId);
  }

  @Override
  public List<ResponsiveMediaSet> findByDesktopMediaId(Long mediaId) {
    return jpaRepository.findByDesktopMediaId(mediaId);
  }

  @Override
  public List<ResponsiveMediaSet> findByMobileMediaId(Long mediaId) {
    return jpaRepository.findByMobileMediaId(mediaId);
  }

  @Override
  public List<ResponsiveMediaSet> findByIdIn(java.util.Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return jpaRepository.findByIdIn(ids);
  }

  @Override
  public List<ResponsiveMediaSet> findByIdInWithMedia(java.util.Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return jpaRepository.findByIdInWithMedia(ids);
  }
}
