package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MediaFolder;
import com.backend.domain.repository.MediaFolderRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MediaFolderRepositoryImpl implements MediaFolderRepository {

  private final MediaFolderJpaRepository jpaRepository;

  @Override
  public MediaFolder save(MediaFolder folder) {
    return jpaRepository.save(folder);
  }

  @Override
  public Optional<MediaFolder> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Optional<MediaFolder> findByUid(String uid) {
    return jpaRepository.findByUid(uid);
  }

  @Override
  public Optional<MediaFolder> findByCode(String code) {
    return jpaRepository.findByCode(code);
  }

  @Override
  public Optional<MediaFolder> findByPath(String path) {
    return jpaRepository.findByPath(path);
  }

  @Override
  public List<MediaFolder> findAll() {
    return jpaRepository.findAll();
  }

  @Override
  public List<MediaFolder> findByParentId(Long parentId) {
    return jpaRepository.findByParentId(parentId);
  }

  @Override
  public List<MediaFolder> findByParentIsNull() {
    return jpaRepository.findByParentIsNull();
  }

  @Override
  public List<MediaFolder> findByDepth(Integer depth) {
    return jpaRepository.findByDepth(depth);
  }

  @Override
  public List<MediaFolder> findByPathStartingWith(String pathPrefix) {
    return jpaRepository.findByPathStartingWith(pathPrefix);
  }

  @Override
  public boolean existsByCode(String code) {
    return jpaRepository.existsByCode(code);
  }

  @Override
  public boolean existsByPath(String path) {
    return jpaRepository.existsByPath(path);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public void delete(MediaFolder folder) {
    jpaRepository.delete(folder);
  }

  @Override
  public long count() {
    return jpaRepository.count();
  }

  @Override
  public long countByParentId(Long parentId) {
    return jpaRepository.countByParentId(parentId);
  }
}
