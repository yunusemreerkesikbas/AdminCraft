package com.backend.infrastructure.persistence.platform.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.PlatformRefreshToken;
import com.backend.domain.repository.PlatformRefreshTokenRepository;
import com.backend.infrastructure.persistence.platform.mapper.PlatformRefreshTokenMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformRefreshTokenRepositoryImpl implements PlatformRefreshTokenRepository {

    private final JpaPlatformRefreshTokenRepository jpaRepository;
    private final PlatformRefreshTokenMapper mapper;

    @Override
    @Transactional("platformTransactionManager")
    public PlatformRefreshToken save(PlatformRefreshToken token) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(token)));
    }

    @Override
    public Optional<PlatformRefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    @Transactional("platformTransactionManager")
    public int revokeByTokenHash(String tokenHash) {
        return jpaRepository.revokeByTokenHash(tokenHash, LocalDateTime.now());
    }

    @Override
    @Transactional("platformTransactionManager")
    public void revokeAllByUserId(Long userId) {
        jpaRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }

    @Override
    @Transactional("platformTransactionManager")
    public int deleteExpiredTokens() {
        return jpaRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
