package com.backend.application.service;

import com.backend.domain.entity.User;
import com.backend.domain.enums.UserRole;
import com.backend.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TenantAdminCreationService {

  private final TenantDbExecutor tenantDbExecutor;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public TenantAdminCreationService(TenantDbExecutor tenantDbExecutor,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this.tenantDbExecutor = tenantDbExecutor;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public void createAdminUser(Long tenantId, String tenantDbName, String adminEmail, String temporaryPassword) {
    tenantDbExecutor.withTenant(tenantId, tenantDbName, () -> {
      if (userRepository.existsByEmail(adminEmail)) {
        throw new IllegalStateException("tenant.admin.already.exists");
      }

      User adminUser = new User();
      adminUser.setEmail(adminEmail);
      adminUser.setPasswordHash(passwordEncoder.encode(temporaryPassword));
      adminUser.setFullName("Admin");
      adminUser.setRole(UserRole.TENANT_ADMIN);
      adminUser.setIsActive(true);
      adminUser.setEmailVerified(false);
      adminUser.setTwoFactorEnabled(false);

      userRepository.save(adminUser);
      return null;
    });
  }
}
