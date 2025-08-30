package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageSectionJpaRepository extends JpaRepository<PageSection, Long> {
  List<PageSection> findByPageIdOrderByDisplayOrderAsc(Long pageId);
}
