package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MediaContainer;
import com.backend.domain.repository.MediaContainerRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MediaContainerRepositoryImpl implements MediaContainerRepository {

  private final MediaContainerJpaRepository jpaRepository;

  @Override
  public MediaContainer save(MediaContainer container) {
    return jpaRepository.save(container);
  }

  @Override
  public Optional<MediaContainer> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Optional<MediaContainer> findByUid(String uid) {
    return jpaRepository.findByUid(uid);
  }

  @Override
  public Optional<MediaContainer> findByCode(String code) {
    return jpaRepository.findByCode(code);
  }

  @Override
  public Optional<MediaContainer> findByMasterMediaId(Long masterMediaId) {
    return jpaRepository.findByMasterMediaId(masterMediaId);
  }

  @Override
  public List<MediaContainer> findAll() {
    return jpaRepository.findAll();
  }

  @Override
  public boolean existsByCode(String code) {
    return jpaRepository.existsByCode(code);
  }

  @Override
  public boolean existsByMasterMediaId(Long masterMediaId) {
    return jpaRepository.existsByMasterMediaId(masterMediaId);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public void delete(MediaContainer container) {
    jpaRepository.delete(container);
  }

  @Override
  public long count() {
    return jpaRepository.count();
  }
}
