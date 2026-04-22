package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.RefreshToken;
import com.backend.domain.repository.RefreshTokenRepository;
import com.backend.infrastructure.persistence.repository.mapper.RefreshTokenMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRepository;
    private final RefreshTokenMapper mapper;

    @Override
    @Transactional
    public RefreshToken save(RefreshToken token) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(token)));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public int revokeByTokenHash(String tokenHash) {
        return jpaRepository.revokeByTokenHash(tokenHash, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void revokeAllByUserId(Long userId) {
        jpaRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public int deleteExpiredTokens() {
        return jpaRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
