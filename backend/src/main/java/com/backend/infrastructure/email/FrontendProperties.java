package com.backend.infrastructure.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.backend.domain.port.FrontendConfigPort;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.frontend")
public class FrontendProperties implements FrontendConfigPort {

    private String baseUrl = "http://%s.localhost:4200";
}
