package com.backend.infrastructure.tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String> {

  private static final long serialVersionUID = 1L;

  private static final int MAX_POOLS = 10;
  private static final int MAX_POOL_SIZE_PER_TENANT = 5;
  private static final long IDLE_EVICT_MINUTES = 30;

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

  private volatile DataSource defaultDataSource;

  private final LoadingCache<String, DataSource> tenantDataSources = CacheBuilder.newBuilder()
      .maximumSize(MAX_POOLS)
      .expireAfterAccess(IDLE_EVICT_MINUTES, TimeUnit.MINUTES)
      .removalListener((RemovalListener<String, DataSource>) notification -> {
        log.info("LRU eviction: closing datasource for {} (cause: {})",
            notification.getKey(), notification.getCause());
        closeDataSource(notification.getValue());
      })
      .build(new CacheLoader<>() {
        @Override
        public DataSource load(String dbName) {
          return createDataSource(dbName);
        }
      });

  @Override
  protected DataSource selectAnyDataSource() {
    if (defaultDataSource == null) {
      synchronized (this) {
        if (defaultDataSource == null) {
          defaultDataSource = createDefaultDataSource();
        }
      }
    }
    return defaultDataSource;
  }

  private DataSource createDefaultDataSource() {
    log.debug("Creating default datasource for Hibernate initialization");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format(
        "jdbc:mysql://%s:%s/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Istanbul",
        dbHost, dbPort));
    config.setUsername(dbUsername);
    config.setPassword(dbPassword);
    config.setDriverClassName(driverClassName);
    config.setMaximumPoolSize(1);
    config.setMinimumIdle(0);
    config.setPoolName("DefaultPool");
    return new HikariDataSource(config);
  }

  @Override
  protected DataSource selectDataSource(String tenantIdentifier) {
    try {
      return tenantDataSources.get(tenantIdentifier);
    } catch (ExecutionException e) {
      log.error("Failed to get/create datasource for tenant: {}", tenantIdentifier, e);
      throw new RuntimeException("Failed to get datasource for tenant: " + tenantIdentifier, e.getCause());
    }
  }

  private DataSource createDataSource(String dbName) {
    log.info("Creating new datasource for tenant DB: {}", dbName);

    HikariConfig config = new HikariConfig();
    String jdbcUrl = jdbcUrlTemplate.replace("{dbName}", dbName);
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(dbUsername);
    config.setPassword(dbPassword);
    config.setDriverClassName(driverClassName);

    config.setMaximumPoolSize(MAX_POOL_SIZE_PER_TENANT);
    config.setMinimumIdle(1);
    config.setIdleTimeout(TimeUnit.MINUTES.toMillis(IDLE_EVICT_MINUTES));
    config.setConnectionTimeout(60000);
    config.setValidationTimeout(5000);
    config.setInitializationFailTimeout(10000);
    config.setLeakDetectionThreshold(10000);
    config.setPoolName("TenantPool-" + dbName);

    return new HikariDataSource(config);
  }

  public DataSource getDataSource(String dbName) {
    return selectDataSource(dbName);
  }

  public void warmUpConnectionPool(String tenantDbName) {
    log.debug("Pre-warming connection pool for tenant DB: {}", tenantDbName);

    int maxAttempts = 3;
    long retryDelayMs = 500;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        DataSource dataSource = selectDataSource(tenantDbName);
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
          statement.execute("SELECT 1");
          log.debug("Connection pool pre-warmed successfully for tenant DB: {} (attempt {}/{})",
              tenantDbName, attempt, maxAttempts);
          return;
        }
      } catch (SQLException e) {
        if (attempt == maxAttempts) {
          log.error("Failed to pre-warm connection pool for tenant DB {} after {} attempts: {}",
              tenantDbName, maxAttempts, e.getMessage(), e);
          throw new RuntimeException("Connection pool initialization failed for tenant: " + tenantDbName, e);
        }
        long delayMs = retryDelayMs * attempt;
        log.warn("Failed to pre-warm connection pool for tenant DB {} (attempt {}/{}), retrying in {}ms: {}",
            tenantDbName, attempt, maxAttempts, delayMs, e.getMessage());

        try {
          Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Connection pool pre-warming interrupted for tenant: " + tenantDbName, ie);
        }
      }
    }
  }

  private void closeDataSource(DataSource dataSource) {
    if (dataSource instanceof HikariDataSource hikari) {
      hikari.close();
    }
  }
}
