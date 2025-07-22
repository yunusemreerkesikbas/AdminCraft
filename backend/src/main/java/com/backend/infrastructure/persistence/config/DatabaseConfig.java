package com.backend.infrastructure.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.backend.infrastructure.persistence.repository"
)
public class DatabaseConfig {
    // Spring Boot will automatically configure DataSource, EntityManagerFactory, and TransactionManager
    // based on application.yml properties
}