package com.backend.application.service;

import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import com.backend.infrastructure.persistence.platform.repository.ProvisioningJobRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AsyncProvisioningExecutor {

  private final TenantPlatformRepository tenantRepository;
  private final ProvisioningJobRepository jobRepository;
  private final TenantModuleRepository tenantModuleRepository;

  @Value("${spring.datasource.tenant.host}")
  private String dbHost;

  @Value("${spring.datasource.tenant.port}")
  private String dbPort;

  @Value("${spring.datasource.tenant.username}")
  private String dbUsername;

  @Value("${spring.datasource.tenant.password}")
  private String dbPassword;

  public AsyncProvisioningExecutor(TenantPlatformRepository tenantRepository,
      ProvisioningJobRepository jobRepository,
      TenantModuleRepository tenantModuleRepository) {
    this.tenantRepository = tenantRepository;
    this.jobRepository = jobRepository;
    this.tenantModuleRepository = tenantModuleRepository;
  }

  @Async
  @Transactional("platformTransactionManager")
  public void executeProvisioning(Long jobId, Tenant tenant, List<String> modules, String correlationId) {
    log.info("Async provisioning started on thread: {}", Thread.currentThread().getName());

    MDC.put("correlationId", correlationId);
    MDC.put("tenantId", String.valueOf(tenant.getId()));

    ProvisioningJob job = jobRepository.findById(jobId)
        .orElseThrow(() -> new IllegalStateException("Job not found during execution: " + jobId));

    try {
      job.setStatus("running");
      job.setStartedAt(LocalDateTime.now());
      jobRepository.save(job);

      log.info("Starting provisioning for tenant {} with modules: {}", tenant.getId(), modules);

      updateProgress(job, 10);
      createDatabaseIfNotExists(tenant.getDatabaseName());

      updateProgress(job, 40);
      runMigrations(tenant.getDatabaseName(), modules);

      updateProgress(job, 70);
      insertTenantModules(tenant.getId(), modules);

      updateProgress(job, 90);
      tenant.setStatus("ACTIVE");
      tenantRepository.save(tenant);

      job.setStatus("succeeded");
      job.setProgress(100);
      job.setCompletedAt(LocalDateTime.now());
      jobRepository.save(job);

      log.info("Provisioning completed successfully for tenant {} on thread: {}",
          tenant.getId(), Thread.currentThread().getName());

    } catch (Exception e) {
      log.error("Provisioning failed for tenant {}", tenant.getId(), e);

      String errorMessage = e.getMessage();
      if (errorMessage != null && errorMessage.length() > 500) {
        errorMessage = errorMessage.substring(0, 497) + "...";
      }

      job.setStatus("failed");
      job.setError(errorMessage);
      job.setCompletedAt(LocalDateTime.now());
      jobRepository.save(job);
    } finally {
      MDC.clear();
    }
  }

  private void createDatabaseIfNotExists(String dbName) {
    String jdbcUrl = String.format("jdbc:mysql://%s:%s?useSSL=false&allowPublicKeyRetrieval=true",
        dbHost, dbPort);

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(dbUsername);
    config.setPassword(dbPassword);
    config.setMaximumPoolSize(1);

    try (HikariDataSource ds = new HikariDataSource(config);
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement()) {

      String createDbSql = String.format(
          "CREATE DATABASE IF NOT EXISTS %s CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
          dbName);
      stmt.execute(createDbSql);

      log.info("Database created or already exists: {}", dbName);

    } catch (Exception e) {
      log.error("Failed to create database: {}", dbName, e);
      throw new RuntimeException("Database creation failed", e);
    }
  }

  private void runMigrations(String dbName, List<String> modules) {
    log.info("Running Flyway migrations for {} with modules: {}", dbName, modules);

    List<String> locations = new ArrayList<>();
    locations.add("classpath:db/tenant/core");

    for (String module : modules) {
      if (!"core".equals(module)) {
        String location = "classpath:db/tenant/" + module;
        locations.add(location);
        log.debug("Added migration location: {}", location);
      }
    }

    DataSource tenantDs = createTenantDataSource(dbName);

    Flyway flyway = Flyway.configure()
        .dataSource(tenantDs)
        .locations(locations.toArray(new String[0]))
        .baselineOnMigrate(true)
        .baselineVersion("0")
        .load();

    flyway.migrate();

    log.info("Migrations completed for {}", dbName);
  }

  private DataSource createTenantDataSource(String dbName) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format(
        "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Istanbul&characterEncoding=UTF-8&useUnicode=true",
        dbHost, dbPort, dbName));
    config.setUsername(dbUsername);
    config.setPassword(dbPassword);
    config.setMaximumPoolSize(2);

    return new HikariDataSource(config);
  }

  private void updateProgress(ProvisioningJob job, int progress) {
    job.setProgress(progress);
    jobRepository.save(job);
  }

  private void insertTenantModules(Long tenantId, List<String> modules) {
    log.info("Inserting tenant_modules records for tenant {} with modules: {}", tenantId, modules);

    List<TenantModule> tenantModules = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();

    for (String moduleCode : modules) {
      // Defensive: validate module codes although FE/BE coordinate via enums
      try {
        com.backend.domain.enums.ModuleCode.fromCode(moduleCode);
      } catch (IllegalArgumentException ex) {
        log.warn("Skipping unknown module code during provisioning: {}", moduleCode);
        continue;
      }
      TenantModule tenantModule = TenantModule.builder()
          .tenantId(tenantId)
          .moduleCode(moduleCode)
          .status("enabled")
          .installedAt(now)
          .build();
      tenantModules.add(tenantModule);
      log.debug("Prepared tenant_module record: tenantId={}, moduleCode={}", tenantId, moduleCode);
    }

    tenantModuleRepository.saveAll(tenantModules);
    log.info("Successfully inserted {} tenant_modules records for tenant {}", tenantModules.size(), tenantId);
  }
}
