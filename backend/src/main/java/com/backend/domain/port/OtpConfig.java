package com.backend.domain.port;

public interface OtpConfig {

    int getLength();

    int getExpirySeconds();

    int getMaxAttempts();

    String getBypassCode();
}
