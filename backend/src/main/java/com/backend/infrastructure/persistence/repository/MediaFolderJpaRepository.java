package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MediaFolder;

@Repository
public interface MediaFolderJpaRepository extends JpaRepository<MediaFolder, Long> {

  Optional<MediaFolder> findByUid(String uid);

  Optional<MediaFolder> findByCode(String code);

  Optional<MediaFolder> findByPath(String path);

  List<MediaFolder> findByParentId(Long parentId);

  List<MediaFolder> findByParentIsNull();

  List<MediaFolder> findByDepth(Integer depth);

  List<MediaFolder> findByPathStartingWith(String pathPrefix);

  boolean existsByCode(String code);

  boolean existsByPath(String path);

  long countByParentId(Long parentId);
}
