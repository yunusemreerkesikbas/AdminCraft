package com.backend.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.backend.application.config.StorageConfigProperties;

@Configuration
@EnableConfigurationProperties(StorageConfigProperties.class)
public class StorageConfig {
}
