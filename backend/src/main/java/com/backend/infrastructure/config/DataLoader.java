package com.backend.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        Integer tenantCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tenants", Integer.class);
        
        if (tenantCount == null || tenantCount == 0) {
            log.info("Loading dummy data...");
            loadDummyData();
            log.info("Dummy data loaded successfully!");
        } else {
            log.info("Dummy data already exists, skipping data loading.");
        }
    }

    private void loadDummyData() throws Exception {
        ClassPathResource resource = new ClassPathResource("data.sql");
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                
                sqlBuilder.append(line).append("\n");
                
                // Execute when we reach a semicolon
                if (line.trim().endsWith(";")) {
                    String sql = sqlBuilder.toString().trim();
                    if (!sql.isEmpty()) {
                        try {
                            jdbcTemplate.execute(sql);
                        } catch (Exception e) {
                            log.warn("Failed to execute SQL: {}", e.getMessage());
                        }
                    }
                    sqlBuilder = new StringBuilder();
                }
            }
        }
    }
}