package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.PageTemplate;
import com.backend.domain.repository.PageTemplateRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PageTemplateRepositoryImpl implements PageTemplateRepository {

  private final PageTemplateJpaRepository jpaRepository;

  @Override
  public Optional<PageTemplate> findById(Long id) {
    return jpaRepository.findWithSlotsById(id);
  }

  @Override
  public Optional<PageTemplate> findByUid(String uid) {
    return jpaRepository.findWithSlotsByUid(uid);
  }

  @Override
  public List<PageTemplate> findAll() {
    return jpaRepository.findAllByOrderByIdDesc();
  }

  @Override
  public Page<PageTemplate> findAll(Pageable pageable, String search) {
    if (search != null && !search.isBlank()) {
      return jpaRepository.searchByQuery(search.trim(), pageable);
    }
    return jpaRepository.findAll(pageable);
  }

  @Override
  public List<PageTemplate> findByIsActiveTrue() {
    return jpaRepository.findByIsActiveTrueOrderByIdDesc();
  }

  @Override
  public PageTemplate save(PageTemplate pageTemplate) {
    return jpaRepository.save(pageTemplate);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsByUid(String uid) {
    return jpaRepository.existsByUid(uid);
  }
}
