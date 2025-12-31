package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MediaFormat;

@Repository
public interface MediaFormatJpaRepository extends JpaRepository<MediaFormat, Long> {

  Optional<MediaFormat> findByUid(String uid);

  Optional<MediaFormat> findByCode(String code);

  List<MediaFormat> findByIsSystemTrue();

  List<MediaFormat> findByIsSystemFalse();

  boolean existsByCode(String code);
}
