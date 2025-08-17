package com.backend;

import com.backend.application.service.SiteServiceImpl;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class DependencyInjectionTest {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SiteServiceImpl siteService;

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
        assertThat(siteRepository).isNotNull();
        assertThat(tenantRepository).isNotNull();
        assertThat(siteService).isNotNull();
    }

    @Test
    void repositoriesBeanInjection() {
        // Test that repository beans are properly injected
        assertThat(siteRepository).isNotNull();
        assertThat(tenantRepository).isNotNull();
    }

    @Test
    void siteServiceBeanInjection() {
        // Test that SiteService bean is properly injected with its dependencies
        assertThat(siteService).isNotNull();
    }
}