package com.backend.infrastructure.persistence.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.enums.OutreachContactStatus;
import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachContact;

@Repository
public interface PlatformOutreachContactRepository extends JpaRepository<PlatformOutreachContact, Long> {

    Optional<PlatformOutreachContact> findByEmailIgnoreCase(String email);

    List<PlatformOutreachContact> findAllByStatus(OutreachContactStatus status);

    List<PlatformOutreachContact> findByIdIn(List<Long> ids);

    @Query("""
        SELECT c FROM PlatformOutreachContact c
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(COALESCE(c.companyName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(COALESCE(c.city, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<PlatformOutreachContact> searchContacts(
        @Param("search") String search,
        Pageable pageable
    );
}
