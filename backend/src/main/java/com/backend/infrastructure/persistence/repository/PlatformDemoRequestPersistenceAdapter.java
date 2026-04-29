package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformDemoRequest;
import com.backend.domain.repository.PlatformDemoRequestRepository;
import com.backend.infrastructure.persistence.platform.mapper.PlatformDemoRequestMapper;
import com.backend.infrastructure.persistence.platform.repository.PlatformDemoRequestJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformDemoRequestPersistenceAdapter implements PlatformDemoRequestRepository {

    private final PlatformDemoRequestJpaRepository jpaRepository;
    private final PlatformDemoRequestMapper mapper;

    @Override
    public PlatformDemoRequest save(PlatformDemoRequest entity) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(entity)));
    }

    @Override
    public Page<PlatformDemoRequest> search(String search, Pageable pageable) {
        return jpaRepository.search(search, pageable).map(mapper::toDomain);
    }

    @Override
    public boolean hasRecentSubmission(String email, String clientIp, LocalDateTime since) {
        return jpaRepository.existsByEmailAndClientIpAndCreatedAtAfter(email, clientIp, since);
    }
}
