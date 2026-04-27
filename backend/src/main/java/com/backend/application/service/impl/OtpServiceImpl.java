package com.backend.application.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.service.OtpService;
import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.entity.User;
import com.backend.domain.entity.VerificationToken;
import com.backend.domain.enums.TokenStatus;
import com.backend.domain.enums.TokenType;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.VerificationTokenRepository;
import com.backend.infrastructure.email.EmailVerificationProperties;
import com.backend.infrastructure.email.OtpProperties;
import com.backend.infrastructure.email.PasswordResetProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final VerificationTokenRepository tokenRepository;
    private final OtpProperties otpProperties;
    private final PasswordResetProperties passwordResetProperties;
    private final EmailVerificationProperties emailVerificationProperties;
    private final TenantContextPort tenantContext;
    private final GlobalRuntimeConfigService globalRuntimeConfigService;
    private final Environment environment;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateOtp() {
        int maxValue = (int) Math.pow(10, otpProperties.getLength());
        int otp = secureRandom.nextInt(maxValue);
        return String.format("%0" + otpProperties.getLength() + "d", otp);
    }

    @Override
    @Transactional("tenantTransactionManager")
    public VerificationToken createOtpToken(User user, TokenType tokenType, String ipAddress, String userAgent) {
        if (!tenantContext.isSet()) {
            throw new IllegalStateException("Tenant context required");
        }
        tokenRepository.revokeAllActiveTokensForUser(user.getId(), tokenType);

        String otp = generateOtp();
        String otpHash = hashToken(otp);

        VerificationToken token = VerificationToken.builder()
                .user(user)
                .tokenHash(otpHash)
                .tokenType(tokenType)
                .status(TokenStatus.ACTIVE)
                .targetValue(otpHash)
                .expiresAt(LocalDateTime.now().plusSeconds(otpProperties.getExpirySeconds()))
                .attemptCount(0)
                .maxAttempts(otpProperties.getMaxAttempts())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        return tokenRepository.save(token);
    }

    @Override
    @Transactional("tenantTransactionManager")
    public LoginOtpResult createLoginOtpToken(User user, String ipAddress, String userAgent) {
        if (!tenantContext.isSet()) {
            throw new IllegalStateException("Tenant context required");
        }
        tokenRepository.revokeAllActiveTokensForUser(user.getId(), TokenType.LOGIN_OTP);

        String otp = generateOtp();
        String sessionToken = UUID.randomUUID().toString();
        String sessionTokenHash = hashToken(sessionToken);

        VerificationToken token = VerificationToken.builder()
                .user(user)
                .tokenHash(sessionTokenHash)
                .tokenType(TokenType.LOGIN_OTP)
                .status(TokenStatus.ACTIVE)
                .targetValue(hashToken(otp))  // Store hashed OTP for security
                .expiresAt(LocalDateTime.now().plusSeconds(otpProperties.getExpirySeconds()))
                .attemptCount(0)
                .maxAttempts(otpProperties.getMaxAttempts())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        tokenRepository.save(token);

        return new LoginOtpResult(otp, sessionToken);
    }

    @Override
    @Transactional("tenantTransactionManager")
    public PasswordResetTokenResult createPasswordResetToken(User user, String ipAddress, String userAgent) {
        if (!tenantContext.isSet()) {
            throw new IllegalStateException("Tenant context required");
        }
        tokenRepository.revokeAllActiveTokensForUser(user.getId(), TokenType.PASSWORD_RESET);

        String plainToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(plainToken);

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .tokenType(TokenType.PASSWORD_RESET)
                .status(TokenStatus.ACTIVE)
                .targetValue(null)
                .expiresAt(LocalDateTime.now().plusSeconds(passwordResetProperties.getExpirySeconds()))
                .attemptCount(0)
                .maxAttempts(1)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        VerificationToken savedToken = tokenRepository.save(verificationToken);
        return new PasswordResetTokenResult(savedToken, plainToken);
    }

    @Override
    @Transactional("tenantTransactionManager")
    public EmailVerificationTokenResult createEmailVerificationToken(User user, String ipAddress, String userAgent) {
        if (!tenantContext.isSet()) {
            throw new IllegalStateException("Tenant context required");
        }
        tokenRepository.revokeAllActiveTokensForUser(user.getId(), TokenType.EMAIL_VERIFY);

        String plainToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(plainToken);

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .tokenType(TokenType.EMAIL_VERIFY)
                .status(TokenStatus.ACTIVE)
                .targetValue(null)
                .expiresAt(LocalDateTime.now().plusSeconds(emailVerificationProperties.getExpirySeconds()))
                .attemptCount(0)
                .maxAttempts(1)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        VerificationToken savedToken = tokenRepository.save(verificationToken);
        return new EmailVerificationTokenResult(savedToken, plainToken);
    }

    @Override
    @Transactional("tenantTransactionManager")
    public boolean validateOtp(String tokenHash, String otpCode) {
        if (!isProductionProfile() && Boolean.TRUE.equals(globalRuntimeConfigService.getOtpBypassEnabled())) {
            String configBypassCode = globalRuntimeConfigService.getOtpBypassCodeDecrypted();
            if (configBypassCode != null && constantTimeEquals(otpCode, configBypassCode)) {
                log.warn("OTP bypass via config panel used — audit this access");
                return true;
            }
        }
        if (!isProductionProfile() && otpProperties.getBypassCode() != null
                && constantTimeEquals(otpCode, otpProperties.getBypassCode())) {
            log.info("OTP bypass code (env var) used");
            return true;
        }

        Optional<VerificationToken> tokenOpt = tokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            log.warn("OTP token not found");
            return false;
        }

        VerificationToken token = tokenOpt.get();

        if (!token.isUsable()) {
            log.warn("OTP token is not usable. Status: {}, Expired: {}, Attempts: {}/{}",
                    token.getStatus(), token.isExpired(), token.getAttemptCount(), token.getMaxAttempts());
            return false;
        }

        String expectedHash = hashToken(otpCode);
        boolean isValid = MessageDigest.isEqual(
                token.getTargetValue().getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));

        if (!isValid) {
            token.incrementAttempts();
            tokenRepository.save(token);
            log.warn("Invalid OTP attempt. Remaining attempts: {}", token.getRemainingAttempts());
        }

        return isValid;
    }

    @Override
    @Transactional("tenantTransactionManager")
    public boolean validateToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        Optional<VerificationToken> tokenOpt = tokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            log.warn("Token not found");
            return false;
        }

        VerificationToken token = tokenOpt.get();
        return token.isUsable();
    }

    @Override
    @Transactional("tenantTransactionManager")
    public void markTokenAsUsed(String rawToken) {
        String tokenHash = hashToken(rawToken);
        tokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.markAsUsed();
            tokenRepository.save(token);
            log.info("Token marked as used: type={}", token.getTokenType());
        });
    }

    @Override
    @Transactional("tenantTransactionManager")
    public void revokeToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        tokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.revoke();
            tokenRepository.save(token);
            log.info("Token revoked: type={}", token.getTokenType());
        });
    }

    @Override
    @Transactional("tenantTransactionManager")
    public void revokeAllUserTokens(Long userId, TokenType tokenType) {
        tokenRepository.revokeAllActiveTokensForUser(userId, tokenType);
        log.info("All {} tokens revoked for user: {}", tokenType, userId);
    }

    @Override
    public VerificationToken getActiveToken(Long userId, TokenType tokenType) {
        return tokenRepository.findActiveTokenByUserAndType(userId, tokenType).orElse(null);
    }

    @Override
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    public int getRemainingAttempts(String rawToken) {
        String tokenHash = hashToken(rawToken);
        return tokenRepository.findByTokenHash(tokenHash)
                .map(VerificationToken::getRemainingAttempts)
                .orElse(0);
    }

    @Override
    @Transactional("tenantTransactionManager")
    public void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        tokenRepository.deleteExpiredTokens(cutoff, cutoff);
        log.info("Cleaned up expired tokens older than {}", cutoff);
    }

    private boolean isProductionProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
