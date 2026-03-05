package com.backend.domain.port;

public interface OtpConfig {

    int getExpirySeconds();

    int getMaxAttempts();

    String getBypassCode();
}
