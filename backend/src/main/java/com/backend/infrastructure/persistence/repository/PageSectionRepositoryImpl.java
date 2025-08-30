package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageSection;
import com.backend.domain.repository.PageSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PageSectionRepositoryImpl implements PageSectionRepository {

  private final PageSectionJpaRepository jpa;

  @Override
  public PageSection save(PageSection section) {
    return jpa.save(section);
  }

  @Override
  public List<PageSection> saveAll(Iterable<PageSection> sections) {
    return jpa.saveAll(sections);
  }

  @Override
  public Optional<PageSection> findById(Long id) {
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
  public List<PageSection> findByPageIdOrderByDisplayOrderAsc(Long pageId) {
    return jpa.findByPageIdOrderByDisplayOrderAsc(pageId);
  }
}
