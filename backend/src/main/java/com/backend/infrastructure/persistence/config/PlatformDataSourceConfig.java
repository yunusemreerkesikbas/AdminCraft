package com.backend.infrastructure.persistence.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.backend.infrastructure.persistence.platform.repository", entityManagerFactoryRef = "platformEntityManagerFactory", transactionManagerRef = "platformTransactionManager")
public class PlatformDataSourceConfig {

  @Primary
  @Bean(name = "platformDataSource")
  @ConfigurationProperties(prefix = "spring.datasource.platform")
  public DataSource platformDataSource() {
    return DataSourceBuilder.create().type(HikariDataSource.class).build();
  }

  @Primary
  @Bean(name = "platformEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean platformEntityManagerFactory(
      @Qualifier("platformDataSource") DataSource dataSource) {

    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("com.backend.infrastructure.persistence.platform.entity");
    em.setPersistenceUnitName("platform");

    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);

    Map<String, Object> properties = new HashMap<>();
    properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
    properties.put("hibernate.show_sql", "true");
    properties.put("hibernate.format_sql", "true");
    properties.put("hibernate.hbm2ddl.auto", "none");
    em.setJpaPropertyMap(properties);

    return em;
  }

  @Primary
  @Bean(name = "platformTransactionManager")
  public PlatformTransactionManager platformTransactionManager(
      @Qualifier("platformEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
