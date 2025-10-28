package com.backend.presentation.controller;

import com.backend.application.dto.response.AdminUserResponse;
import com.backend.application.dto.tenant.TenantModuleResponse;
import com.backend.application.service.PasswordGeneratorService;
import com.backend.application.service.TenantService;
import com.backend.application.usecase.CreateTenantUseCase;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.enums.UserRole;
import com.backend.domain.repository.UserRepository;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.presentation.dto.request.CreateTenantRequest;
import com.backend.presentation.dto.request.UpdateTenantRequest;
import com.backend.presentation.dto.response.TenantDetailResponse;
import com.backend.presentation.dto.response.TenantListResponse;
import com.backend.shared.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TenantController {

        private final TenantService tenantService;
        private final CreateTenantUseCase createTenantUseCase;
        private final MessageSource messageSource;
        private final TenantPlatformRepository tenantRepository;
        private final TenantContext tenantContext;
        private final UserRepository userRepository;
        private final PasswordGeneratorService passwordGeneratorService;
        private final PasswordEncoder passwordEncoder;

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @PostMapping
        public ResponseEntity<ApiResponse<TenantDetailResponse>> createTenant(
                        @Valid @RequestBody CreateTenantRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                var cmd = new com.backend.application.command.CreateTenantCommand(
                                request.subdomain(), request.companyName(), request.defaultLanguage(),
                                request.supportedLanguages(), request.notes(), null, null);
                TenantDetailResponse response = createTenantUseCase.execute(cmd, displayLanguage);
                String message = messageSource.getMessage("tenant.created.success", null,
                                Locale.forLanguageTag(languageCode));
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(message, response));
        }

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<TenantDetailResponse>> getTenantById(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                TenantDetailResponse response = tenantService.getTenantDetailById(id, displayLanguage);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @GetMapping
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<List<TenantListResponse>>> getAllTenants(
                        @RequestParam(required = false) TenantStatus status,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                List<TenantListResponse> response = status != null
                                ? tenantService.getTenantsByStatusAsList(status, displayLanguage)
                                : tenantService.getAllTenantsAsList(displayLanguage);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<TenantDetailResponse>> updateTenant(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @Valid @RequestBody UpdateTenantRequest request,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                var cmd = new com.backend.application.command.UpdateTenantCommand(
                                request.companyName(), request.defaultLanguage(), request.supportedLanguages(),
                                request.customDomain(), request.notes());
                TenantDetailResponse response = tenantService.updateTenantWithDetail(id, cmd, displayLanguage);
                String message = messageSource.getMessage("tenant.updated.success", null,
                                Locale.forLanguageTag(languageCode));
                return ResponseEntity.ok(ApiResponse.success(message, response));
        }

        @PreAuthorize("hasRole('SUPER_ADMIN')")
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteTenant(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                tenantService.deleteTenant(id);
                String message = messageSource.getMessage("tenant.deleted.success", null,
                                Locale.forLanguageTag(languageCode));
                return ResponseEntity.ok(ApiResponse.success(message, null));
        }

        @GetMapping("/{tenantId}/modules")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<List<TenantModuleResponse>>> getTenantModules(
                        @PathVariable @Valid @NotNull @Min(1) Long tenantId,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {
                Language displayLanguage = Language.fromCodeOrDefault(languageCode);
                List<TenantModuleResponse> modules = tenantService.getTenantModules(tenantId, displayLanguage);
                return ResponseEntity.ok(ApiResponse.success(modules));
        }

        /**
         * Generate admin user for a tenant.
         * Sprint 20: Tenant Admin User Generator
         *
         * @param id tenant ID
         * @param languageCode display language
         * @return admin user credentials (one-time view)
         */
        @PostMapping("/{id}/generate-admin")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<AdminUserResponse>> generateAdminUser(
                        @PathVariable @Valid @NotNull @Min(1) Long id,
                        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String languageCode) {

                // 1. Validate tenant (platform transaction)
                Tenant tenant = validateTenantForAdminGeneration(id);

                // 2. Generate credentials
                String adminEmail = "admin@" + tenant.getSubdomain() + ".com";
                String temporaryPassword = passwordGeneratorService.generate();

                // 3. Create admin user in tenant DB (tenant transaction)
                createAdminUserInTenantDb(id, tenant.getDatabaseName(), adminEmail, temporaryPassword);

                // 4. Update platform tenant record (platform transaction)
                updateTenantAdminFlag(id, adminEmail);

                // 5. Build response
                String loginUrl = String.format("http://%s.localhost:4200", tenant.getSubdomain());
                AdminUserResponse response = new AdminUserResponse(
                                adminEmail,
                                temporaryPassword,
                                tenant.getSubdomain(),
                                loginUrl);

                log.info("Generated admin user for tenant {} (subdomain: {})", id, tenant.getSubdomain());

                String message = messageSource.getMessage("tenant.admin.generated.success", null,
                                Locale.forLanguageTag(languageCode));
                return ResponseEntity.ok(ApiResponse.success(message, response));
        }

        /**
         * Validate tenant exists, is ACTIVE, and has no admin user yet.
         * Uses platform transaction.
         */
        @Transactional("platformTransactionManager")
        private Tenant validateTenantForAdminGeneration(Long tenantId) {
                Tenant tenant = tenantRepository.findById(tenantId)
                                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + tenantId));

                if (!"ACTIVE".equals(tenant.getStatus())) {
                        throw new IllegalStateException("Tenant must be ACTIVE to generate admin user");
                }

                if (Boolean.TRUE.equals(tenant.getHasAdminUser())) {
                        throw new IllegalStateException("Admin user already exists for this tenant");
                }

                return tenant;
        }

        /**
         * Create admin user in tenant database.
         * Uses tenant transaction with explicit context management.
         */
        @Transactional("tenantTransactionManager")
        private void createAdminUserInTenantDb(Long tenantId, String tenantDbName, String adminEmail, String temporaryPassword) {
                // Set tenant context for database routing
                tenantContext.setTenantId(String.valueOf(tenantId));
                tenantContext.setTenantDbName(tenantDbName);

                try {
                        // Double-check in tenant DB
                        if (userRepository.existsByEmail(adminEmail)) {
                                throw new IllegalStateException("Admin user already exists in tenant database");
                        }

                        // Create admin user
                        User adminUser = new User();
                        adminUser.setEmail(adminEmail);
                        adminUser.setPasswordHash(passwordEncoder.encode(temporaryPassword));
                        adminUser.setFullName("Admin");
                        adminUser.setRole(UserRole.TENANT_ADMIN);
                        adminUser.setIsActive(true);
                        adminUser.setEmailVerified(false);
                        adminUser.setPasswordChangedAt(java.time.LocalDateTime.now());
                        adminUser.setTwoFactorEnabled(false);

                        userRepository.save(adminUser);
                        log.debug("Admin user created in tenant DB: {}", tenantDbName);

                } finally {
                        // Always clear tenant context
                        tenantContext.clear();
                }
        }

        /**
         * Update platform tenant record with admin user info.
         * Uses platform transaction.
         */
        @Transactional("platformTransactionManager")
        private void updateTenantAdminFlag(Long tenantId, String adminEmail) {
                Tenant tenant = tenantRepository.findById(tenantId)
                                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + tenantId));

                tenant.setAdminEmail(adminEmail);
                tenant.setAdminName("Admin");
                tenant.setHasAdminUser(true);
                tenantRepository.save(tenant);
                log.debug("Platform tenant record updated for tenant ID: {}", tenantId);
        }
}