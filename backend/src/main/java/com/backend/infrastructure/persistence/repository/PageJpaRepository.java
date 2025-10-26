package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.PageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageJpaRepository extends JpaRepository<Page, Long> {

  Optional<Page> findByUuid(String uuid);

  Optional<Page> findByUid(String uid);

  List<Page> findByStatus(PageStatus status);

  List<Page> findByCategoryId(Long categoryId);

  boolean existsByUid(String uid);
}
