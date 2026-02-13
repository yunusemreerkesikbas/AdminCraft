package com.backend.infrastructure.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("dev")
public class FlywayDevConfig {

  @Bean
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
      try {
        flyway.migrate();
      } catch (FlywayValidateException e) {
        log.warn("Flyway checksum mismatch (e.g. migration file changed). Running repair: {}", e.getMessage());
        flyway.repair();
        flyway.migrate();
      }
    };
  }
}
