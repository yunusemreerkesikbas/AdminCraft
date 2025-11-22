package com.backend.application.usecase;

import com.backend.application.dto.response.AdminUserResponse;
import com.backend.application.service.PasswordGeneratorService;
import com.backend.application.service.TenantAdminCreationService;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GenerateTenantAdminUserUseCase {

  private final TenantPlatformRepository tenantRepository;
  private final TenantAdminCreationService tenantAdminCreationService;
  private final PasswordGeneratorService passwordGeneratorService;

  @Value("${app.frontend.base-url:http://%s.localhost:4200}")
  private String frontendBaseUrl;

  public GenerateTenantAdminUserUseCase(TenantPlatformRepository tenantRepository,
      TenantAdminCreationService tenantAdminCreationService,
      PasswordGeneratorService passwordGeneratorService) {
    this.tenantRepository = tenantRepository;
    this.tenantAdminCreationService = tenantAdminCreationService;
    this.passwordGeneratorService = passwordGeneratorService;
  }

  @Transactional("platformTransactionManager")
  public AdminUserResponse execute(Long tenantId) {
    Tenant tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + tenantId));

    if (!"ACTIVE".equals(tenant.getStatus())) {
      throw new IllegalStateException("Tenant must be ACTIVE to generate admin user");
    }

    String adminEmail = "admin@" + tenant.getSubdomain() + ".com";
    if (Boolean.TRUE.equals(tenant.getHasAdminUser())
        && tenantAdminCreationService.adminExists(tenant.getId(), tenant.getDatabaseName(), adminEmail)) {
      throw new IllegalStateException("Admin user already exists for this tenant");
    }

    String temporaryPassword = passwordGeneratorService.generate();

    try {
      // Step 1: Create user in tenant database FIRST
      tenantAdminCreationService.createAdminUser(tenant.getId(), tenant.getDatabaseName(), adminEmail,
          temporaryPassword);

      tenant.setAdminEmail(adminEmail);
      tenant.setAdminName("Admin");
      tenant.setHasAdminUser(true);
      tenantRepository.save(tenant);

      String loginUrl = resolveLoginUrl(tenant.getSubdomain());
      return new AdminUserResponse(adminEmail, temporaryPassword, tenant.getSubdomain(), loginUrl);

    } catch (Exception e) {
      tenant.setHasAdminUser(false);
      tenant.setAdminEmail(null);
      tenant.setAdminName(null);
      tenantRepository.save(tenant);
      throw e;
    }
  }

  private String resolveLoginUrl(String subdomain) {
    if (frontendBaseUrl.contains("%s")) {
      return String.format(frontendBaseUrl, subdomain);
    }
    if (frontendBaseUrl.contains("{subdomain}")) {
      return frontendBaseUrl.replace("{subdomain}", subdomain);
    }
    return "http://" + subdomain + ".localhost:4200";
  }
}
