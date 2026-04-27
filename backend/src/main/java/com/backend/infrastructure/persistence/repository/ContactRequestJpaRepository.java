package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
        )
        AND (:locale IS NULL OR :locale = '' OR c.locale = :locale)
        """)
    Page<ContactRequest> search(
            @Param("search") String search,
            @Param("locale") String locale,
            Pageable pageable);

    @Modifying
    @Query("DELETE FROM ContactRequest c WHERE c.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
