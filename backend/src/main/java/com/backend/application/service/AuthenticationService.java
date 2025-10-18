package com.backend.application.service;

import com.backend.application.command.auth.AuthenticateCommand;
import com.backend.presentation.dto.response.LoginResponse;

public interface AuthenticationService {
    LoginResponse authenticate(AuthenticateCommand command);

    LoginResponse refreshToken(String refreshToken);

    void logout(String token);
}