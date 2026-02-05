package com.backend.application.service;

import com.backend.application.dto.AuthResult;
import com.backend.application.dto.TokenValidationResult;
import com.backend.domain.enums.Language;

public interface AuthenticationService {

    AuthResult authenticate(String email, String password, Long tenantId, String subdomain);

    AuthResult authenticate(String email, String password, Long tenantId, String subdomain,
            String deviceFingerprint, String ipAddress, String userAgent);

    AuthResult verifyOtp(String pendingToken, String otpCode, boolean trustDevice,
            String deviceFingerprint, String deviceName, String ipAddress, String userAgent,
            Long tenantId, String subdomain);

    void requestPasswordReset(String email, Long tenantId, String subdomain,
            String ipAddress, String userAgent, Language language);

    TokenValidationResult validateResetToken(String token);

    void resetPassword(String token, String newPassword);

    void requestEmailVerification(Long userId, String ipAddress, String userAgent, Language language);

    TokenValidationResult validateEmailVerificationToken(String token);

    AuthResult setInitialPassword(
            String token,
            String password,
            String deviceFingerprint,
            boolean trustDevice,
            String deviceName,
            String ipAddress,
            String userAgent);

    AuthResult refreshToken(String refreshToken);

    void logout(String token);
}