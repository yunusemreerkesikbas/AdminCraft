package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.MediaContainer;

/**
 * Repository interface for MediaContainer entity.
 */
public interface MediaContainerRepository {

  MediaContainer save(MediaContainer container);

  Optional<MediaContainer> findById(Long id);

  Optional<MediaContainer> findByUid(String uid);

  Optional<MediaContainer> findByCode(String code);

  Optional<MediaContainer> findByMasterMediaId(Long masterMediaId);

  List<MediaContainer> findAll();

  boolean existsByCode(String code);

  boolean existsByMasterMediaId(Long masterMediaId);

  void deleteById(Long id);

  void delete(MediaContainer container);

  long count();
}
