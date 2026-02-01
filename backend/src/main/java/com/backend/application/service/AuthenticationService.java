package com.backend.application.service;

import com.backend.presentation.dto.response.LoginResponse;

public interface AuthenticationService {
    LoginResponse authenticate(String email, String password, Long tenantId, String subdomain);

    LoginResponse refreshToken(String refreshToken);

    void logout(String token);
}