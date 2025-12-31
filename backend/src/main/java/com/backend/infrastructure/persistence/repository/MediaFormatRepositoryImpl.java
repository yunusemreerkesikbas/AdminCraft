package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MediaFormat;
import com.backend.domain.repository.MediaFormatRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MediaFormatRepositoryImpl implements MediaFormatRepository {

  private final MediaFormatJpaRepository jpaRepository;

  @Override
  public MediaFormat save(MediaFormat format) {
    return jpaRepository.save(format);
  }

  @Override
  public Optional<MediaFormat> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Optional<MediaFormat> findByUid(String uid) {
    return jpaRepository.findByUid(uid);
  }

  @Override
  public Optional<MediaFormat> findByCode(String code) {
    return jpaRepository.findByCode(code);
  }

  @Override
  public List<MediaFormat> findAll() {
    return jpaRepository.findAll();
  }

  @Override
  public List<MediaFormat> findByIsSystemTrue() {
    return jpaRepository.findByIsSystemTrue();
  }

  @Override
  public List<MediaFormat> findByIsSystemFalse() {
    return jpaRepository.findByIsSystemFalse();
  }

  @Override
  public boolean existsByCode(String code) {
    return jpaRepository.existsByCode(code);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public void delete(MediaFormat format) {
    jpaRepository.delete(format);
  }

  @Override
  public long count() {
    return jpaRepository.count();
  }
}
