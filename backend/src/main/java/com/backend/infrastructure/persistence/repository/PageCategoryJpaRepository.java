package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.PageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageCategoryJpaRepository extends JpaRepository<PageCategory, Long> {
  boolean existsByUid(String uid);

  List<PageCategory> findAllByOrderBySortOrderAsc();

  List<PageCategory> findByParentId(Long parentId);

  List<PageCategory> findByParentIdOrderBySortOrderAsc(Long parentId);

  List<PageCategory> findByParentIdIsNullOrderBySortOrderAsc();
}
