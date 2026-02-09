package com.backend.application.service;

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

  /**
   * CRITICAL: Module execution order for Flyway migrations.
   * This order ensures that FK dependencies are satisfied:
   * - core creates base tables (users, sites)
   * - media creates responsive_media_set
   * - component_library creates components/entries and adds FK to responsive_media_set
   * - pagebuilder creates pages/slots and references components
   * - product creates product catalog
   * See docs/global/migrations.md for full documentation.
   */
  private static final List<String> MODULE_ORDER = List.of(
      "core", // Base tables (users, sites) - always first
      "media", // Media assets, responsive_media_set
      "component_library", // Components, entries (references media)
      "pagebuilder", // Pages, slots (references components)
      "product" // Product catalog
  );

  @Value("${spring.datasource.tenant.host}")
  private String dbHost;

  @Value("${spring.datasource.tenant.port}")
  private String dbPort;

  @Value("${spring.datasource.tenant.username}")
  private String dbUsername;

  @Value("${spring.datasource.tenant.password}")
  private String dbPassword;

  @Value("${spring.datasource.tenant.jdbc-url-template}")
  private String jdbcUrlTemplate;

  @Value("${spring.datasource.tenant.driver-class-name}")
  private String driverClassName;

  public void migrateTenant(String dbName, List<String> modules) {
    log.info("Starting migration for tenant database: {} with modules: {}", dbName, modules);

    try (HikariDataSource tenantDs = createTenantDataSource(dbName)) {
      // CRITICAL: Modules must be executed in this order for FK dependencies
      // See docs/global/migrations.md for details
      List<String> orderedModules = getOrderedModules(modules);

      for (String module : orderedModules) {
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
    String jdbcUrl = jdbcUrlTemplate.replace("{dbName}", dbName);
    config.setJdbcUrl(jdbcUrl);
    config.setDriverClassName(driverClassName);
    config.setUsername(dbUsername);
    config.setPassword(dbPassword);
    config.setMaximumPoolSize(2);
    config.setPoolName("MigrationPool-" + dbName);
    return new HikariDataSource(config);
  }

  /**
   * Orders the requested modules according to MODULE_ORDER.
   * Only includes modules that are in both the requested list and MODULE_ORDER.
   * Modules not in MODULE_ORDER are appended at the end (for forward
   * compatibility).
   */
  public List<String> getOrderedModules(List<String> requestedModules) {
    List<String> ordered = new java.util.ArrayList<>();

    // Add modules in the correct order
    for (String module : MODULE_ORDER) {
      if (requestedModules.contains(module)) {
        ordered.add(module);
      }
    }

    // Add any modules not in MODULE_ORDER (forward compatibility)
    for (String module : requestedModules) {
      if (!ordered.contains(module)) {
        log.warn("Module '{}' not in MODULE_ORDER, appending at end. Consider adding to MODULE_ORDER.", module);
        ordered.add(module);
      }
    }

    log.debug("Ordered modules for migration: {}", ordered);
    return ordered;
  }
}
