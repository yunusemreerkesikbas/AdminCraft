package com.backend.application.service;

import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TenantMigrationService {

  @Value("${spring.datasource.tenant.host}")
  private String dbHost;

  @Value("${spring.datasource.tenant.port}")
  private String dbPort;

  @Value("${spring.datasource.tenant.username}")
  private String dbUsername;

  @Value("${spring.datasource.tenant.password}")
  private String dbPassword;

  public void migrateTenant(String dbName, List<String> modules) {
    log.info("Starting migration for tenant database: {} with modules: {}", dbName, modules);

    try (HikariDataSource tenantDs = createTenantDataSource(dbName)) {
      List<String> modulesToMigrate = new ArrayList<>();
      if (modules.contains("core")) {
        modulesToMigrate.add("core");
      }
      modules.stream()
          .filter(m -> !"core".equals(m))
          .forEach(modulesToMigrate::add);

      for (String module : modulesToMigrate) {
        String location = "classpath:db/tenant/" + module;
        String historyTable = "flyway_" + module + "_history";

        log.debug("Running Flyway for module: {} (location: {}, table: {})", module, location, historyTable);

        Flyway flyway = Flyway.configure()
            .dataSource(tenantDs)
            .locations(location)
            .table(historyTable)
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .outOfOrder(false)
            .validateOnMigrate(true)
            .load();

        try {
          var result = flyway.migrate();
          log.info("Migration result for module {} in {}: {} migrations executed, target version: {}",
              module, dbName, result.migrationsExecuted, result.targetSchemaVersion);
          if (result.migrationsExecuted > 0) {
            result.migrations.forEach(m -> log.info("  Executed: {} - {}", m.version, m.description));
          }
        } catch (org.flywaydb.core.api.FlywayException e) {
          log.warn("Flyway migration failed validation for module: {}. Attempting repair due to error: {}", module,
              e.getMessage());
          flyway.repair();
          flyway.migrate();
        }
        log.info("Migration completed for module: {} in database: {}", module, dbName);
      }
      log.info("All migrations completed successfully for database: {}", dbName);
    } catch (Exception e) {
      log.error("Migration failed for database: {}", dbName, e);
      throw new RuntimeException("Migration failed for database: " + dbName, e);
    }
  }

  private HikariDataSource createTenantDataSource(String dbName) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format(
        "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Istanbul&characterEncoding=UTF-8&useUnicode=true",
        dbHost, dbPort, dbName));
    config.setUsername(dbUsername);
    config.setPassword(dbPassword);
    config.setMaximumPoolSize(2);
    config.setPoolName("MigrationPool-" + dbName);
    return new HikariDataSource(config);
  }
}
