package com.backend.infrastructure.persistence.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.backend.infrastructure.tenant.CurrentTenantIdentifierResolver;
import com.backend.infrastructure.tenant.MultiTenantConnectionProvider;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
				"com.backend.infrastructure.persistence.repository",
				"com.backend.infrastructure.persistence.commerce",
				"com.backend.infrastructure.persistence.tenant.repository",
                "com.backend.domain.repository"
}, entityManagerFactoryRef = "tenantEntityManagerFactory", transactionManagerRef = "tenantTransactionManager")
public class DatabaseConfig {

        @Value("${spring.jpa.show-sql:false}")
        private boolean showSql;

        private final MultiTenantConnectionProvider multiTenantConnectionProvider;
        private final CurrentTenantIdentifierResolver tenantIdentifierResolver;

        public DatabaseConfig(MultiTenantConnectionProvider multiTenantConnectionProvider,
                        CurrentTenantIdentifierResolver tenantIdentifierResolver) {
                this.multiTenantConnectionProvider = multiTenantConnectionProvider;
                this.tenantIdentifierResolver = tenantIdentifierResolver;
        }

        @Bean(name = "tenantEntityManagerFactory")
        public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory() {
                LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
                em.setPackagesToScan(
								"com.backend.domain.entity",
								"com.backend.domain.commerce",
								"com.backend.infrastructure.persistence.entity",
                                "com.backend.infrastructure.persistence.repository.entity");
                em.setPersistenceUnitName("tenant");

                HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
                em.setJpaVendorAdapter(vendorAdapter);

                Map<String, Object> properties = new HashMap<>();
                properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
                properties.put("hibernate.show_sql", String.valueOf(showSql));
                properties.put("hibernate.format_sql", String.valueOf(showSql));
                properties.put("hibernate.hbm2ddl.auto", "none");
                properties.put("hibernate.multiTenancy", "DATABASE");
                properties.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
                properties.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);
                em.setJpaPropertyMap(properties);

                return em;
        }

        @Bean(name = "tenantTransactionManager")
        public PlatformTransactionManager tenantTransactionManager(
                        @Qualifier("tenantEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
                return new JpaTransactionManager(entityManagerFactory);
        }
}
