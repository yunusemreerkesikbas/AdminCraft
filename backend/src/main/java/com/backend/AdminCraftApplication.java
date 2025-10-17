package com.backend;

import com.backend.infrastructure.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
@ConfigurationPropertiesScan
@EnableConfigurationProperties(JwtProperties.class)
public class AdminCraftApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminCraftApplication.class, args);
    }

}