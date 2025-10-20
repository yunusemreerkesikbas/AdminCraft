package com.backend.infrastructure.tenant;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class MultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String> {

  private static final long serialVersionUID = 1L;

  private static final int MAX_POOLS = 10;
  private static final int MAX_POOL_SIZE_PER_TENANT = 5;
  private static final long IDLE_EVICT_MILLIS = 30 * 60 * 1000;

  @Value("${spring.datasource.tenant.host}")
  private String dbHost;

  @Value("${spring.datasource.tenant.port}")
  private String dbPort;

  @Value("${spring.datasource.tenant.username}")
  private String dbUsername;

  @Value("${spring.datasource.tenant.password}")
  private String dbPassword;

  private final Map<String, DataSource> tenantDataSources = new LinkedHashMap<String, DataSource>(MAX_POOLS, 0.75f,
      true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, DataSource> eldest) {
      if (size() > MAX_POOLS) {
        log.info("LRU eviction: closing datasource for {}", eldest.getKey());
        closeDataSource(eldest.getValue());
        return true;
      }
      return false;
    }
  };

  @Override
  protected DataSource selectAnyDataSource() {
    return tenantDataSources.values().stream()
        .findFirst()
        .orElseGet(() -> createDefaultDataSource());
  }

  private DataSource createDefaultDataSource() {
    log.debug("Creating default datasource for Hibernate initialization");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format(
        "jdbc:mysql://%s:%s/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Istanbul",
        dbHost, dbPort));
    config.setUsername(dbUsername);
    config.setPassword(dbPassword);
    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    config.setMaximumPoolSize(1);
    config.setMinimumIdle(0);
    config.setPoolName("DefaultPool");
    return new HikariDataSource(config);
  }

  @Override
  protected DataSource selectDataSource(String tenantIdentifier) {
    return tenantDataSources.computeIfAbsent(tenantIdentifier, this::createDataSource);
  }

  private DataSource createDataSource(String dbName) {
    log.info("Creating new datasource for tenant DB: {}", dbName);

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format(
        "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Istanbul&characterEncoding=UTF-8&useUnicode=true",
        dbHost, dbPort, dbName));
    config.setUsername(dbUsername);
    config.setPassword(dbPassword);
    config.setDriverClassName("com.mysql.cj.jdbc.Driver");

    config.setMaximumPoolSize(MAX_POOL_SIZE_PER_TENANT);
    config.setMinimumIdle(0);
    config.setIdleTimeout(IDLE_EVICT_MILLIS);
    config.setConnectionTimeout(10000);
    config.setLeakDetectionThreshold(10000);
    config.setPoolName("TenantPool-" + dbName);

    return new HikariDataSource(config);
  }

  private void closeDataSource(DataSource dataSource) {
    if (dataSource instanceof HikariDataSource) {
      ((HikariDataSource) dataSource).close();
    }
  }
}


