package com.backend.domain.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.entity.PlatformDemoRequest;

public interface PlatformDemoRequestRepository {

    PlatformDemoRequest save(PlatformDemoRequest entity);

    Page<PlatformDemoRequest> search(String search, Pageable pageable);

    boolean hasRecentSubmission(String email, String clientIp, LocalDateTime since);
}
