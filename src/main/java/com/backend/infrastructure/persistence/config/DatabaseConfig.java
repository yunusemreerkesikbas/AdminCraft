package com.backend.infrastructure.persistence.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.backend.infrastructure.persistence.repository",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
@RequiredArgsConstructor
public class DatabaseConfig {

    private final Environment environment;

    @Value("${spring.jpa.hibernate.ddl-auto:validate}")
    private String ddlAuto;

    @Value("${spring.jpa.show-sql:false}")
    private String showSql;

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.backend.domain.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        
        // Use configurable properties instead of hardcoded values
        properties.put("hibernate.dialect", 
            environment.getProperty("spring.jpa.database-platform", 
                "org.hibernate.dialect.MySQL8Dialect"));
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        
        // Optional configurable properties with defaults
        properties.put("hibernate.format_sql", 
            environment.getProperty("spring.jpa.properties.hibernate.format_sql", "true"));
        properties.put("hibernate.use_sql_comments", 
            environment.getProperty("spring.jpa.properties.hibernate.use_sql_comments", "true"));
        properties.put("hibernate.jdbc.batch_size", 
            environment.getProperty("spring.jpa.properties.hibernate.jdbc.batch_size", "20"));
        properties.put("hibernate.order_inserts", 
            environment.getProperty("spring.jpa.properties.hibernate.order_inserts", "true"));
        properties.put("hibernate.order_updates", 
            environment.getProperty("spring.jpa.properties.hibernate.order_updates", "true"));
        properties.put("hibernate.jdbc.batch_versioned_data", 
            environment.getProperty("spring.jpa.properties.hibernate.jdbc.batch_versioned_data", "true"));
        properties.put("hibernate.connection.characterEncoding", 
            environment.getProperty("spring.jpa.properties.hibernate.connection.characterEncoding", "utf8"));
        properties.put("hibernate.connection.useUnicode", 
            environment.getProperty("spring.jpa.properties.hibernate.connection.useUnicode", "true"));
        
        return properties;
    }
}