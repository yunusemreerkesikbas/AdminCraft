package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.VerificationToken;
import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;
import com.backend.domain.repository.VerificationTokenRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VerificationTokenRepositoryImpl implements VerificationTokenRepository {

    private final VerificationTokenJpaRepository jpaRepository;

    @Override
    public VerificationToken save(VerificationToken token) {
        return jpaRepository.save(token);
    }

    @Override
    public Optional<VerificationToken> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<VerificationToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash);
    }

    @Override
    public Optional<VerificationToken> findByTokenHashAndStatus(String tokenHash, TokenStatus status) {
        return jpaRepository.findByTokenHashAndStatus(tokenHash, status);
    }

    @Override
    public List<VerificationToken> findByUserIdAndTokenType(Long userId, TokenType tokenType) {
        return jpaRepository.findByUserIdAndTokenType(userId, tokenType);
    }

    @Override
    public List<VerificationToken> findByUserIdAndTokenTypeAndStatus(Long userId, TokenType tokenType, TokenStatus status) {
        return jpaRepository.findByUserIdAndTokenTypeAndStatus(userId, tokenType, status);
    }

    @Override
    public Optional<VerificationToken> findActiveTokenByUserAndType(Long userId, TokenType tokenType) {
        return jpaRepository.findActiveTokenByUserAndType(userId, tokenType, LocalDateTime.now());
    }

    @Override
    public List<VerificationToken> findExpiredTokens(LocalDateTime now) {
        return jpaRepository.findExpiredTokens(now);
    }

    @Override
    @Transactional
    public void revokeAllActiveTokensForUser(Long userId, TokenType tokenType) {
        jpaRepository.revokeAllActiveTokensForUser(userId, tokenType);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens(LocalDateTime before) {
        jpaRepository.deleteExpiredTokens(before);
    }

    @Override
    public long countByUserIdAndTokenTypeAndStatus(Long userId, TokenType tokenType, TokenStatus status) {
        return jpaRepository.countByUserIdAndTokenTypeAndStatus(userId, tokenType, status);
    }

    @Override
    public boolean existsByTokenHash(String tokenHash) {
        return jpaRepository.existsByTokenHash(tokenHash);
    }
}
