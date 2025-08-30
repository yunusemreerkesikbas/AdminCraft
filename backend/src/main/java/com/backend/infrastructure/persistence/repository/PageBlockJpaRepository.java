package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageBlockJpaRepository extends JpaRepository<PageBlock, Long> {
  List<PageBlock> findBySectionIdOrderByDisplayOrderAsc(Long sectionId);

  List<PageBlock> findBySectionIdInOrderByDisplayOrderAsc(List<Long> sectionIds);
}
