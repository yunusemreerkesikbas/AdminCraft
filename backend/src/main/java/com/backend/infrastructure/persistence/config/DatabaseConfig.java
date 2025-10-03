package com.backend.infrastructure.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
        "com.backend.infrastructure.persistence.repository",
        "com.backend.domain.repository"
})
public class DatabaseConfig {

}