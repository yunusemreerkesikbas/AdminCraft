package com.backend.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.PageCategory;

@Repository
public interface PageCategoryJpaRepository extends JpaRepository<PageCategory, Long> {
  boolean existsByUid(String uid);

  List<PageCategory> findAllByOrderBySortOrderAsc();

  List<PageCategory> findByParentId(Long parentId);

  List<PageCategory> findByParentIdOrderBySortOrderAsc(Long parentId);

  List<PageCategory> findByParentIdIsNullOrderBySortOrderAsc();
}
