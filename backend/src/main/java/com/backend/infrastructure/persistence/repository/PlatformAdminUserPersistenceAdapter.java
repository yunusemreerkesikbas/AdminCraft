package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformAdminUser;
import com.backend.domain.repository.PlatformAdminUserRepository;
import com.backend.infrastructure.persistence.platform.mapper.PlatformAdminUserMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformAdminUserPersistenceAdapter implements PlatformAdminUserRepository {

    private final com.backend.infrastructure.persistence.platform.repository.PlatformAdminUserRepository platformAdminUserJpaRepository;
    private final PlatformAdminUserMapper platformAdminUserMapper;

    @Override
    public Optional<PlatformAdminUser> findByEmailAndIsActiveTrue(String email) {
        return platformAdminUserJpaRepository.findByEmailAndIsActiveTrue(email).map(platformAdminUserMapper::toDomain);
    }

    @Override
    public PlatformAdminUser save(PlatformAdminUser admin) {
        return platformAdminUserMapper.toDomain(
                platformAdminUserJpaRepository.save(platformAdminUserMapper.toEntity(admin)));
    }
}
