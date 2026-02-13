package com.backend.application.service;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ProvisioningJob;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.TenantModule;
import com.backend.domain.repository.ProvisioningJobRepository;
import com.backend.domain.repository.TenantModuleRepository;
import com.backend.domain.repository.TenantRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@lombok.RequiredArgsConstructor
public class AsyncProvisioningExecutor {

  private static final Pattern VALID_DB_NAME = Pattern.compile("^ac_[a-z0-9_]+_\\d+$");

  private final TenantRepository tenantRepository;
  private final ProvisioningJobRepository jobRepository;
  private final TenantModuleRepository tenantModuleRepository;
  private final TenantMigrationService tenantMigrationService;
  private final Environment environment;

  @Value("${spring.datasource.tenant.host}")
  private String dbHost;

  @Value("${spring.datasource.tenant.port}")
  private String dbPort;

  @Value("${spring.datasource.tenant.username}")
  private String dbUsername;

  @Value("${spring.datasource.tenant.password}")
  private String dbPassword;

  @Async
  public void executeProvisioning(Long jobId, Long tenantId, String dbName, List<String> modules, String correlationId) {
    log.info("Async provisioning started on thread: {}", Thread.currentThread().getName());

    MDC.put("correlationId", correlationId);
    MDC.put("tenantId", String.valueOf(tenantId));

    try {
      updateJobStatus(jobId, "running", 0, null, LocalDateTime.now(), null);

      log.info("Starting provisioning for tenant {} with modules: {}", tenantId, modules);

      updateJobProgress(jobId, 20);
      prepareDatabaseForProvisioning(tenantId, dbName);

      updateJobProgress(jobId, 60);
      tenantMigrationService.migrateTenant(dbName, modules);

      updateJobProgress(jobId, 90);
      insertTenantModules(tenantId, modules);

      updateTenantStatus(tenantId, "ACTIVE");
      updateJobStatus(jobId, "succeeded", 100, null, null, LocalDateTime.now());

      log.info("Provisioning completed successfully for tenant {} on thread: {}",
          tenantId, Thread.currentThread().getName());

    } catch (Exception e) {
      log.error("Provisioning failed for tenant {}", tenantId, e);
      String errorMessage = buildDetailedErrorMessage(e);
      updateJobStatus(jobId, "failed", 0, errorMessage, null, LocalDateTime.now());
    } finally {
      MDC.clear();
    }
  }

  @Async
  public void executeSyncMigrations(Long jobId, Long tenantId, String dbName, List<String> modules, String correlationId) {
    log.info("Async sync migrations started on thread: {}", Thread.currentThread().getName());
    MDC.put("correlationId", correlationId);
    MDC.put("tenantId", String.valueOf(tenantId));
    try {
      updateJobStatus(jobId, "running", 0, null, LocalDateTime.now(), null);
      log.info("Starting migration sync for tenant {} with modules: {}", tenantId, modules);
      updateJobProgress(jobId, 30);
      tenantMigrationService.migrateTenant(dbName, modules);
      updateJobStatus(jobId, "succeeded", 100, null, null, LocalDateTime.now());
      log.info("Migration sync completed successfully for tenant {} on thread: {}",
          tenantId, Thread.currentThread().getName());

    } catch (Exception e) {
      log.error("Migration sync failed for tenant {}", tenantId, e);
      String errorMessage = buildDetailedErrorMessage(e);
      updateJobStatus(jobId, "failed", 0, errorMessage, null, LocalDateTime.now());
    } finally {
      MDC.clear();
    }
  }

  private void createDatabaseIfNotExists(String dbName) {
    createDatabaseIfNotExists(dbName, false);
  }

  private void createDatabaseIfNotExists(String dbName, boolean recreateIfExists) {
    validateDatabaseName(dbName);
    String quotedDbName = "`" + dbName + "`";

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

      boolean exists = databaseExists(conn, dbName);
      boolean hasTables = exists && databaseHasTables(conn, dbName);

      if (hasTables) {
        if (recreateIfExists) {
          log.warn("Database {} already contains tables. Dropping for clean provisioning.", dbName);
          stmt.execute("DROP DATABASE IF EXISTS " + quotedDbName);
        } else {
          throw new IllegalStateException(
              "Tenant database already initialized: " + dbName + ". Use sync migrations instead of full provision.");
        }
      }

      String createDbSql = "CREATE DATABASE IF NOT EXISTS " + quotedDbName
          + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
      stmt.execute(createDbSql);

      log.info("Database created or already exists: {}", dbName);

    } catch (Exception e) {
      log.error("Failed to create database: {}", dbName, e);
      throw new RuntimeException("Database creation failed", e);
    }
  }

  private void prepareDatabaseForProvisioning(Long tenantId, String dbName) {
    boolean allowRecreate = canRecreateDatabase(tenantId);
    createDatabaseIfNotExists(dbName, allowRecreate);
  }

  private void validateDatabaseName(String dbName) {
    if (dbName == null || !VALID_DB_NAME.matcher(dbName).matches()) {
      throw new IllegalArgumentException("Invalid database name: " + dbName
          + ". Must match pattern ac_<subdomain>_<tenant_id> (e.g. ac_htalks_32).");
    }
  }

  private boolean canRecreateDatabase(Long tenantId) {
    if (!isDestructiveRecreateAllowed()) {
      return false;
    }
    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));
    boolean hasModules = !tenantModuleRepository.findByTenantId(tenantId).isEmpty();

    return tenant.getStatus() == com.backend.domain.enums.TenantStatus.PENDING && !hasModules;
  }

  private boolean isDestructiveRecreateAllowed() {
    return environment.acceptsProfiles(
        org.springframework.core.env.Profiles.of("dev", "test"));
  }

  private boolean databaseExists(Connection conn, String dbName) {
    String sql = "SELECT SCHEMA_NAME FROM information_schema.schemata WHERE SCHEMA_NAME = ?";
    try (var ps = conn.prepareStatement(sql)) {
      ps.setString(1, dbName);
      try (var rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to check database existence", e);
    }
  }

  private boolean databaseHasTables(Connection conn, String dbName) {
    String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ?";
    try (var ps = conn.prepareStatement(sql)) {
      ps.setString(1, dbName);
      try (var rs = ps.executeQuery()) {
        if (!rs.next()) {
          return false;
        }
        return rs.getInt(1) > 0;
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to check database tables", e);
    }
  }

  @Transactional("platformTransactionManager")
  public void updateJobStatus(Long jobId, String status, Integer progress,
      String error, LocalDateTime startedAt, LocalDateTime completedAt) {
    ProvisioningJob job = jobRepository.findById(jobId)
        .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));

    job.setStatus(status);
    if (progress != null)
      job.setProgress(progress);
    if (error != null)
      job.setError(error);
    if (startedAt != null)
      job.setStartedAt(startedAt);
    if (completedAt != null)
      job.setCompletedAt(completedAt);

    jobRepository.save(job);
    jobRepository.flush();
    log.info("Job {} status updated to: {} (progress: {})", jobId, status, job.getProgress());
  }

  @Transactional("platformTransactionManager")
  public void updateJobProgress(Long jobId, int progress) {
    ProvisioningJob job = jobRepository.findById(jobId)
        .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));
    job.setProgress(progress);
    jobRepository.save(job);
    jobRepository.flush();
    log.debug("Job {} progress updated to: {}", jobId, progress);
  }

  @Transactional("platformTransactionManager")
  public void updateTenantStatus(Long tenantId, String status) {
    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));
    tenant.setStatus(com.backend.domain.enums.TenantStatus.valueOf(status));
    tenantRepository.save(tenant);
    log.info("Tenant {} status updated to: {}", tenantId, status);
  }

  @Transactional("platformTransactionManager")
  public void insertTenantModules(Long tenantId, List<String> modules) {
    log.info("Inserting tenant_modules records for tenant {} with modules: {}", tenantId, modules);

    List<TenantModule> existingModules = tenantModuleRepository.findByTenantId(tenantId);
    List<String> existingModuleCodes = existingModules.stream()
        .map(TenantModule::getModuleCode)
        .toList();

    log.debug("Tenant {} already has modules: {}", tenantId, existingModuleCodes);

    List<TenantModule> newModules = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();

    for (String moduleCode : modules) {
      if (existingModuleCodes.contains(moduleCode)) {
        log.debug("Skipping module {} - already installed for tenant {}", moduleCode, tenantId);
        continue;
      }

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
      newModules.add(tenantModule);
      log.debug("Prepared NEW tenant_module record: tenantId={}, moduleCode={}", tenantId, moduleCode);
    }

    if (!newModules.isEmpty()) {
      tenantModuleRepository.saveAll(newModules);
      log.info("Successfully inserted {} new tenant_modules records for tenant {}", newModules.size(), tenantId);
    } else {
      log.info("No new modules to insert for tenant {} - all modules already exist", tenantId);
    }
  }

  private String buildDetailedErrorMessage(Exception e) {
    StringBuilder errorMsg = new StringBuilder();

    errorMsg.append(e.getClass().getSimpleName()).append(": ");

    String message = e.getMessage();
    if (message != null && !message.isEmpty()) {
      errorMsg.append(message);
    } else {
      errorMsg.append("No message provided");
    }

    Throwable rootCause = e;
    while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
      rootCause = rootCause.getCause();
    }

    if (rootCause != e && rootCause.getMessage() != null) {
      errorMsg.append(" | Root cause: ")
          .append(rootCause.getClass().getSimpleName())
          .append(": ")
          .append(rootCause.getMessage());
    }

    StackTraceElement[] stackTrace = e.getStackTrace();
    if (stackTrace != null && stackTrace.length > 0) {
      errorMsg.append(" | at ");
      int limit = Math.min(2, stackTrace.length);
      for (int i = 0; i < limit; i++) {
        if (i > 0) errorMsg.append(" -> ");
        StackTraceElement element = stackTrace[i];
        errorMsg.append(element.getClassName())
            .append(".")
            .append(element.getMethodName())
            .append("(")
            .append(element.getFileName())
            .append(":")
            .append(element.getLineNumber())
            .append(")");
      }
    }

    String result = errorMsg.toString();
    if (result.length() > 500) {
      return result.substring(0, 497) + "...";
    }
    return result;
  }

}
