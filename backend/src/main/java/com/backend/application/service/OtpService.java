package com.backend.application.service;

import com.backend.domain.entity.User;
import com.backend.domain.entity.VerificationToken;
import com.backend.domain.enums.TokenType;

public interface OtpService {

    String generateOtp();

    VerificationToken createOtpToken(User user, TokenType tokenType, String ipAddress, String userAgent);

    /**
     * Creates a login OTP token with a separate session identifier.
     * Returns the OTP code as the first element and the session token as the second element.
     */
    LoginOtpResult createLoginOtpToken(User user, String ipAddress, String userAgent);

    record LoginOtpResult(String otpCode, String sessionToken) {}

    record PasswordResetTokenResult(VerificationToken token, String plainToken) {}

    record EmailVerificationTokenResult(VerificationToken token, String plainToken) {}

    PasswordResetTokenResult createPasswordResetToken(User user, String ipAddress, String userAgent);

    EmailVerificationTokenResult createEmailVerificationToken(User user, String ipAddress, String userAgent);

    boolean validateOtp(String tokenHash, String otpCode);

    boolean validateToken(String tokenHash);

    void markTokenAsUsed(String tokenHash);

    void revokeToken(String tokenHash);

    void revokeAllUserTokens(Long userId, TokenType tokenType);

    VerificationToken getActiveToken(Long userId, TokenType tokenType);

    String hashToken(String token);

    int getRemainingAttempts(String tokenHash);

    void cleanupExpiredTokens();
}
