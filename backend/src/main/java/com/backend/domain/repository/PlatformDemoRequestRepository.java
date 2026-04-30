package com.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.entity.PlatformDemoRequest;

public interface PlatformDemoRequestRepository {

    PlatformDemoRequest save(PlatformDemoRequest entity);

    Page<PlatformDemoRequest> search(String search, Pageable pageable);

    /**
     * Atomically inserts {@code entity} only when no row exists for the same email (case-insensitive)
     * and client IP within {@code windowStart} (rows with {@code created_at} &gt;= {@code windowStart} block insert).
     */
    Optional<PlatformDemoRequest> saveIfNotDuplicateWithinWindow(PlatformDemoRequest entity, LocalDateTime windowStart);
}
