package com.backend.infrastructure.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.backend.infrastructure.security.JwtAuthenticationFilter;
import com.backend.infrastructure.tenant.TenantFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize and @PostAuthorize annotations
public class SecurityConfig {

        private final CorsProperties corsProperties;

        public SecurityConfig(CorsProperties corsProperties) {
                this.corsProperties = corsProperties;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12); // Higher rounds for better security
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter,
                        TenantFilter tenantFilter) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Context path is /api, controller @RequestMapping("/auth") = servlet
                                                // path
                                                // "/auth"
                                                .requestMatchers("/auth/**").permitAll() // Authentication endpoints
                                                .requestMatchers("/config/public").permitAll() // Public tenant config (unauthenticated)
                                                .requestMatchers("/health/**").permitAll() // Health check endpoints
                                                .requestMatchers("/actuator/**").permitAll() // Actuator endpoints for
                                                                                             // monitoring
                                                .requestMatchers("/public/**").permitAll() // Public API endpoints
                                                .requestMatchers("/cms/**").permitAll() // CMS Delivery API (public)
                                                .requestMatchers("/media/files/**").permitAll() // Media file downloads
                                                                                                // (images)
                                                .requestMatchers("/swagger-ui/**").permitAll() // Swagger documentation
                                                .requestMatchers("/v3/api-docs/**").permitAll() // OpenAPI documentation
                                                .requestMatchers("/favicon.ico").permitAll() // Favicon
                                                .requestMatchers("/error").permitAll() // Error pages

                                                // All other endpoints require authentication
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterAfter(tenantFilter, JwtAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
                configuration.setAllowedOriginPatterns(Arrays.asList(
                                "http://*.localhost:4200",
                                "http://*.localhost:4201",
                                "https://*.localhost:4200",
                                "https://*.localhost:4201"));

                // Allow all HTTP methods
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));

                // Allow specific headers commonly used in REST APIs
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "Accept-Language",
                                "X-Requested-With",
                                "Cache-Control",
                                "X-Tenant-ID",
                                "X-Tenant-Subdomain",
                                "X-User-ID"));

                // Allow credentials (cookies, authorization headers)
                configuration.setAllowCredentials(true);

                // Expose authorization header to frontend
                configuration.setExposedHeaders(Arrays.asList(
                                "Authorization",
                                "X-Total-Count",
                                "X-Page-Number",
                                "X-Page-Size"));

                // Cache preflight response for 1 hour
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}