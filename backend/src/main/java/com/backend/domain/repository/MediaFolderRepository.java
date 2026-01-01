package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.MediaFolder;

/**
 * Repository interface for MediaFolder entity.
 */
public interface MediaFolderRepository {

  MediaFolder save(MediaFolder folder);

  Optional<MediaFolder> findById(Long id);

  Optional<MediaFolder> findByUid(String uid);

  Optional<MediaFolder> findByCode(String code);

  Optional<MediaFolder> findByPath(String path);

  List<MediaFolder> findAll();

  List<MediaFolder> findByParentId(Long parentId);

  List<MediaFolder> findByParentIsNull(); // Root folders

  List<MediaFolder> findByDepth(Integer depth);

  List<MediaFolder> findByPathStartingWith(String pathPrefix);

  boolean existsByCode(String code);

  boolean existsByPath(String path);

  void deleteById(Long id);

  void delete(MediaFolder folder);

  long count();

  long countByParentId(Long parentId);
}
