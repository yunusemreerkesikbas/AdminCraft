package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.Page;
import com.backend.domain.enums.PageStatus;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    Optional<Page> findByUuid(String uuid);

    Optional<Page> findByUid(String uid);

    List<Page> findByStatus(PageStatus status);

    boolean existsByUid(String uid);

    List<Page> findByUidIn(List<String> uids);
}
