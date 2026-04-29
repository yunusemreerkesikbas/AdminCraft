package com.backend.application.service.config;

import com.backend.application.dto.config.ConfigAuthResult;
import com.backend.application.dto.config.ConfigLoginResult;

public interface ConfigAuthenticationService {

    ConfigLoginResult login(String email, String password, Long tenantId, String subdomain,
            String ipAddress, String userAgent);

    ConfigAuthResult verifyOtp(String pendingToken, String otpCode, Long tenantId, String subdomain,
            String ipAddress);

    ConfigAuthResult refreshToken(String refreshToken, String ipAddress);
}
