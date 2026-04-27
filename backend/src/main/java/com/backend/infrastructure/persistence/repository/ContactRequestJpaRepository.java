package com.backend.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.ContactRequest;

@Repository
public interface ContactRequestJpaRepository extends JpaRepository<ContactRequest, Long> {

    @Query("""
        SELECT c FROM ContactRequest c
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.subject) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.message) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.locale) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<ContactRequest> search(@Param("search") String search, Pageable pageable);
}
