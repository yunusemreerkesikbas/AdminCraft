package com.backend.infrastructure.persistence.platform.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.infrastructure.persistence.platform.entity.PlatformDemoRequest;

@Repository
public interface PlatformDemoRequestJpaRepository extends JpaRepository<PlatformDemoRequest, Long> {

    @Query("""
        SELECT d FROM PlatformDemoRequest d
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(d.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(COALESCE(d.phone, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(d.message) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(d.locale) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<PlatformDemoRequest> search(@Param("search") String search, Pageable pageable);

    boolean existsByEmailAndClientIpAndCreatedAtAfter(String email, String clientIp, LocalDateTime since);
}
