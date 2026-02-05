package com.backend.infrastructure.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.trusted-device")
public class TrustedDeviceProperties {

    private int expiryDays = 30;
    private int maxDevicesPerUser = 5;
}
