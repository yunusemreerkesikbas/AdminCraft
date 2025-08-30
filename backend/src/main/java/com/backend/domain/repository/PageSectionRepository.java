package com.backend.domain.repository;

import com.backend.domain.entity.PageSection;

import java.util.List;
import java.util.Optional;

public interface PageSectionRepository {
  PageSection save(PageSection section);

  List<PageSection> saveAll(Iterable<PageSection> sections);

  Optional<PageSection> findById(Long id);

  void deleteById(Long id);

  boolean existsById(Long id);

  List<PageSection> findByPageIdOrderByDisplayOrderAsc(Long pageId);
}
