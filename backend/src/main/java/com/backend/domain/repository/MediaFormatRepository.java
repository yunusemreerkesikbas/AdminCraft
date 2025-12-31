package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.MediaFormat;

/**
 * Repository interface for MediaFormat entity.
 */
public interface MediaFormatRepository {

  MediaFormat save(MediaFormat format);

  Optional<MediaFormat> findById(Long id);

  Optional<MediaFormat> findByUid(String uid);

  Optional<MediaFormat> findByCode(String code);

  List<MediaFormat> findAll();

  List<MediaFormat> findByIsSystemTrue();

  List<MediaFormat> findByIsSystemFalse();

  boolean existsByCode(String code);

  void deleteById(Long id);

  void delete(MediaFormat format);

  long count();
}
