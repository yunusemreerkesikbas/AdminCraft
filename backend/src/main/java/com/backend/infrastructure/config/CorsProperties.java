package com.backend.infrastructure.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = List.of(
            "http://localhost:4200",
            "http://localhost:4201",
            "http://localhost:3000",
            "http://localhost:8090",
            "http://localhost:8080",
            "http://localhost:8081",
            "https://localhost:4200",
            "https://localhost:4201",
            "https://localhost:3000",
            "https://localhost:8090",
            "https://localhost:8080",
            "https://localhost:8081");

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins != null ? allowedOrigins : this.allowedOrigins;
    }
}
