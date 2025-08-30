package com.backend.domain.repository;

import com.backend.domain.entity.PageBlock;

import java.util.List;
import java.util.Optional;

public interface PageBlockRepository {
  PageBlock save(PageBlock block);

  List<PageBlock> saveAll(Iterable<PageBlock> blocks);

  Optional<PageBlock> findById(Long id);

  void deleteById(Long id);

  boolean existsById(Long id);

  List<PageBlock> findBySectionIdOrderByDisplayOrderAsc(Long sectionId);

  List<PageBlock> findBySectionIdInOrderByDisplayOrderAsc(List<Long> sectionIds);
}
