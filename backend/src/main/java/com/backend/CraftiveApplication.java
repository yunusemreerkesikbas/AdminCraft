package com.backend;

import com.backend.infrastructure.config.AppSecurityProperties;
import com.backend.infrastructure.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
@ConfigurationPropertiesScan
@EnableConfigurationProperties({ JwtProperties.class, AppSecurityProperties.class })
public class CraftiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(CraftiveApplication.class, args);
    }

}
