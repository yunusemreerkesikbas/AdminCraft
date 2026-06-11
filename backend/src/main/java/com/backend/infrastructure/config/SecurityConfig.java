package com.backend.infrastructure.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.backend.infrastructure.security.JwtAuthenticationFilter;
import com.backend.infrastructure.tenant.TenantFilter;
import com.backend.presentation.filter.CmsPreviewFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize and @PostAuthorize annotations
public class SecurityConfig {

        private final CorsProperties corsProperties;
        private final Environment env;

        public SecurityConfig(CorsProperties corsProperties, Environment env) {
                this.corsProperties = corsProperties;
                this.env = env;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12); // Higher rounds for better security
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter,
                        TenantFilter tenantFilter, CmsPreviewFilter cmsPreviewFilter) throws Exception {
                boolean isDev = Arrays.asList(env.getActiveProfiles()).contains("dev");

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .headers(h -> {
                                        h.frameOptions(fo -> fo.deny());
                                        h.contentTypeOptions(cto -> {});
                                        h.referrerPolicy(rp -> rp.policy(
                                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
                                        h.contentSecurityPolicy(csp -> csp.policyDirectives(
                                                "default-src 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'; "
                                                        + "img-src 'self' data: https:; font-src 'self' data:; connect-src 'self'; "
                                                        + "script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"));
                                        if (!isDev) {
                                                h.httpStrictTransportSecurity(hsts -> hsts
                                                        .includeSubDomains(true)
                                                        .maxAgeInSeconds(31_536_000));
                                        }
                                })
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Context path is /api, controller @RequestMapping("/auth") = servlet
                                                // path
                                                // "/auth"
                                                .requestMatchers("/auth/**").permitAll() // Authentication endpoints
                                                .requestMatchers("/config/auth/**").permitAll() // Config panel auth endpoints
                                                .requestMatchers("/platform/cms/config").permitAll() // Platform public config flags
                                                .requestMatchers("/platform/public/newsletter/**").permitAll() // Platform public newsletter
                                                .requestMatchers("/platform/public/demo-requests").permitAll() // Landing demo requests
                                                .requestMatchers("/health/**").permitAll() // Health check endpoints
                                                .requestMatchers("/actuator/health").permitAll() // Liveness probe (no details)
												.requestMatchers("/actuator/**").hasRole("SUPER_ADMIN") // SEC-005: gate all
																										// other actuator
																										// endpoints
												.requestMatchers("/public/**").permitAll() // Public API endpoints
												.requestMatchers("/commerce/cart/**").permitAll() // Public Commerce cart API
												.requestMatchers("/cms/preview/**").authenticated() // SmartEdit preview ticket issue (TENANT_ADMIN via @PreAuthorize)
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
                                .addFilterAfter(tenantFilter, JwtAuthenticationFilter.class)
                                .addFilterAfter(cmsPreviewFilter, TenantFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
                if (!corsProperties.getAllowedOriginPatterns().isEmpty()) {
                        configuration.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
                }

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
								"X-User-ID",
								"X-Client-Version",
								"X-Cart-Token",
								"X-Cms-Preview-Ticket"));

                // Allow credentials (cookies, authorization headers)
                configuration.setAllowCredentials(true);

                // Expose authorization header to frontend
                configuration.setExposedHeaders(Arrays.asList(
								"Authorization",
								"X-Total-Count",
								"X-Page-Number",
								"X-Page-Size",
								"X-Cart-Token"));

                // Cache preflight response for 1 hour
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
