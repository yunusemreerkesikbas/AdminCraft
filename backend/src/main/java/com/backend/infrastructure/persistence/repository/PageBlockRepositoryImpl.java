package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageBlock;
import com.backend.domain.repository.PageBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PageBlockRepositoryImpl implements PageBlockRepository {

  private final PageBlockJpaRepository jpa;

  @Override
  public PageBlock save(PageBlock block) {
    return jpa.save(block);
  }

  @Override
  public List<PageBlock> saveAll(Iterable<PageBlock> blocks) {
    return jpa.saveAll(blocks);
  }

  @Override
  public Optional<PageBlock> findById(Long id) {
    return jpa.findById(id);
  }

  @Override
  public void deleteById(Long id) {
    jpa.deleteById(id);
  }

  @Override
  public boolean existsById(Long id) {
    return jpa.existsById(id);
  }

  @Override
  public List<PageBlock> findBySectionIdOrderByDisplayOrderAsc(Long sectionId) {
    return jpa.findBySectionIdOrderByDisplayOrderAsc(sectionId);
  }

  @Override
  public List<PageBlock> findBySectionIdInOrderByDisplayOrderAsc(List<Long> sectionIds) {
    return jpa.findBySectionIdInOrderByDisplayOrderAsc(sectionIds);
  }
}
