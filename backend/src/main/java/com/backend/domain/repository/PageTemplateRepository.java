package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.entity.PageTemplate;

public interface PageTemplateRepository {

  Optional<PageTemplate> findById(Long id);

  Optional<PageTemplate> findByUid(String uid);

  List<PageTemplate> findAll();

  Page<PageTemplate> findAll(Pageable pageable, String search);

  List<PageTemplate> findByIsActiveTrue();

  PageTemplate save(PageTemplate pageTemplate);

  void deleteById(Long id);

  boolean existsByUid(String uid);
}
