package com.backend.application.service.config;

import com.backend.application.dto.config.ConfigAuthChallengeResult;
import com.backend.application.dto.config.ConfigAuthResult;

public interface ConfigAuthenticationService {

    ConfigAuthChallengeResult login(String email, String password, Long tenantId, String subdomain,
            String ipAddress, String userAgent);

    ConfigAuthResult verifyOtp(String pendingToken, String otpCode, Long tenantId, String subdomain,
            String ipAddress);
}
