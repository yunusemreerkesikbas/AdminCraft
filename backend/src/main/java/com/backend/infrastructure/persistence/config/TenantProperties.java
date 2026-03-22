package com.backend.infrastructure.persistence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "craftive.tenant")
public class TenantProperties {
    private String defaultSchema = "public";
    private String schemaPrefix = "tenant_";
    private boolean createSchemaIfNotExists = true;
    private int maxPoolSize = 10;
    private int minPoolSize = 2;
}