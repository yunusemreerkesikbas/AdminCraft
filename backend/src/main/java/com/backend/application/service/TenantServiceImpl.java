package com.backend.application.service;

import com.backend.application.command.CreateTenantCommand;
import com.backend.application.command.UpdateTenantCommand;
import com.backend.application.dto.tenant.TenantModuleResponse;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProvisioningStatus;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;
import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import com.backend.infrastructure.persistence.platform.repository.ProvisioningJobRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;
import com.backend.presentation.dto.response.TenantDetailResponse;
import com.backend.presentation.dto.response.TenantListResponse;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.shared.constants.ValidationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ProvisioningService provisioningService;
    private final TenantModuleRepository tenantModuleRepository;
    private final ProvisioningJobRepository provisioningJobRepository;

    @Override
    @Transactional
    public TenantDetailResponse createTenantWithDetail(CreateTenantCommand command, Language displayLanguage) {
        if (ValidationConstants.isReservedSubdomain(command.subdomain())) {
            throw new IllegalArgumentException("Subdomain is reserved and cannot be used: " + command.subdomain());
        }
        if (tenantRepository.existsBySubdomain(command.subdomain())) {
            throw new IllegalArgumentException("Subdomain already exists: " + command.subdomain());
        }

        Tenant tenant = new Tenant();
        tenant.setSubdomain(command.subdomain());
        tenant.setCompanyName(command.companyName());
        tenant.setDefaultLanguage(command.defaultLanguage());
        tenant.setSupportedLanguages(command.supportedLanguages());
        tenant.setNotes(command.notes());
        tenant.setAdminEmail(command.adminEmail());
        tenant.setAdminName(command.adminName());

        Tenant savedTenant = tenantRepository.save(tenant);

        ProvisioningStatus provisioningStatus = calculateProvisioningStatus(savedTenant.getId());
        Integer modulesCount = countProvisionedModules(savedTenant.getId());

        return TenantDetailResponse.from(savedTenant, displayLanguage, provisioningStatus, modulesCount);
    }

    @Override
    @Transactional
    public TenantDetailResponse updateTenantWithDetail(Long id, UpdateTenantCommand command, Language displayLanguage) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));

        if (command.companyName() != null) {
            tenant.setCompanyName(command.companyName());
        }
        if (command.defaultLanguage() != null) {
            tenant.setDefaultLanguage(command.defaultLanguage());
        }
        if (command.supportedLanguages() != null && !command.supportedLanguages().isEmpty()) {
            tenant.setSupportedLanguages(command.supportedLanguages());
        }
        if (command.customDomain() != null) {
            if (!command.customDomain().isEmpty() &&
                    tenantRepository.existsByCustomDomainAndIdNot(command.customDomain(), id)) {
                throw new IllegalArgumentException("Custom domain already exists: " + command.customDomain());
            }
            tenant.setCustomDomain(command.customDomain());
        }
        if (command.notes() != null) {
            tenant.setNotes(command.notes());
        }

        Tenant updatedTenant = tenantRepository.save(tenant);

        ProvisioningStatus provisioningStatus = calculateProvisioningStatus(updatedTenant.getId());
        Integer modulesCount = countProvisionedModules(updatedTenant.getId());

        return TenantDetailResponse.from(updatedTenant, displayLanguage, provisioningStatus, modulesCount);
    }

    @Override
    @Transactional
    public void deleteTenant(Long id) {
        if (tenantRepository.findById(id).isEmpty()) {
            throw new TenantNotFoundException(id);
        }
        tenantRepository.deleteById(id);
    }

    @Override
    public boolean isCustomDomainAvailable(String customDomain) {
        return !tenantRepository.existsByCustomDomain(customDomain);
    }

    @Override
    public boolean hasAccessToTenant(String currentUserEmail, Long tenantId) {
        log.debug("Checking tenant access for user {} to tenant {}", currentUserEmail, tenantId);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
                String role = (String) details.get("role");
                Long userTenantId = (Long) details.get("tenantId");

                log.debug("User {} has role {} and tenantId {} from token", currentUserEmail, role, userTenantId);
                if ("SUPER_ADMIN".equals(role)) {
                    log.debug("User {} has SUPER_ADMIN role, granting access to tenant {}", currentUserEmail, tenantId);
                    return true;
                }
                if (userTenantId != null && userTenantId.equals(tenantId)) {
                    log.debug("User {} has access to their own tenant {}", currentUserEmail, tenantId);
                    return true;
                }

                log.debug("User {} denied access to tenant {} (user tenant: {})", currentUserEmail, tenantId,
                        userTenantId);
                return false;
            }
            log.debug("Authentication details not available, falling back to database query");
            Optional<User> currentUser = userRepository.findByEmail(currentUserEmail);
            if (currentUser.isEmpty()) {
                log.warn("User not found: {}", currentUserEmail);
                return false;
            }

            User user = currentUser.get();
            if (user.getRole().name().equals("SUPER_ADMIN")) {
                log.debug("User {} has SUPER_ADMIN role, granting access to tenant {}", currentUserEmail, tenantId);
                return true;
            }
            log.debug("User {} access granted based on TenantContext routing", currentUserEmail);
            return true;

        } catch (Exception ex) {
            log.error("Error checking tenant access for user {} to tenant {}: {}",
                    currentUserEmail, tenantId, ex.getMessage());
            return false; // Deny access on error for security
        }
    }

    @Override
    public List<TenantModuleResponse> getTenantModules(Long tenantId, Language displayLanguage) {
        log.debug("Fetching modules for tenant: {}", tenantId);
        tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));
        List<TenantModule> tenantModules = tenantModuleRepository.findByTenantIdAndStatus(tenantId, "enabled");
        return tenantModules.stream()
                .map(tm -> TenantModuleResponse.builder()
                        .id(tm.getId())
                        .moduleCode(tm.getModuleCode())
                        .moduleName(
                                tm.getModuleCatalog() != null ? tm.getModuleCatalog().getName() : tm.getModuleCode())
                        .status(tm.getStatus())
                        .targetVersion(tm.getTargetVersion())
                        .installedAt(tm.getInstalledAt())
                        .build())
                .toList();
    }

    @Override
    public TenantListResponse getTenantListById(Long id, Language displayLanguage) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));
        ProvisioningStatus provisioningStatus = calculateProvisioningStatus(id);
        Integer modulesCount = countProvisionedModules(id);
        return TenantListResponse.from(tenant, displayLanguage, provisioningStatus, modulesCount);
    }

    @Override
    public List<TenantListResponse> getAllTenantsAsList(Language displayLanguage) {
        return tenantRepository.findAll().stream()
                .map(tenant -> {
                    ProvisioningStatus provisioningStatus = calculateProvisioningStatus(tenant.getId());
                    Integer modulesCount = countProvisionedModules(tenant.getId());
                    return TenantListResponse.from(tenant, displayLanguage, provisioningStatus, modulesCount);
                })
                .toList();
    }

    @Override
    public List<TenantListResponse> getTenantsByStatusAsList(TenantStatus status, Language displayLanguage) {
        return tenantRepository.findByStatus(status).stream()
                .map(tenant -> {
                    ProvisioningStatus provisioningStatus = calculateProvisioningStatus(tenant.getId());
                    Integer modulesCount = countProvisionedModules(tenant.getId());
                    return TenantListResponse.from(tenant, displayLanguage, provisioningStatus, modulesCount);
                })
                .toList();
    }

    @Override
    public TenantDetailResponse getTenantDetailById(Long id, Language displayLanguage) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));
        ProvisioningStatus provisioningStatus = calculateProvisioningStatus(id);
        Integer modulesCount = countProvisionedModules(id);
        return TenantDetailResponse.from(tenant, displayLanguage, provisioningStatus, modulesCount);
    }

    private ProvisioningStatus calculateProvisioningStatus(Long tenantId) {
        Optional<ProvisioningJob> latestJob = provisioningJobRepository
                .findFirstByTenantIdOrderByCreatedAtDesc(tenantId);

        if (latestJob.isEmpty()) {
            return ProvisioningStatus.IDLE;
        }

        String jobStatus = latestJob.get().getStatus();
        return switch (jobStatus) {
            case "pending", "running" -> ProvisioningStatus.PROVISIONING;
            case "failed" -> ProvisioningStatus.FAILED;
            case "succeeded" -> ProvisioningStatus.IDLE;
            default -> ProvisioningStatus.IDLE;
        };
    }

    private Integer countProvisionedModules(Long tenantId) {
        Integer count = tenantModuleRepository.countEnabledModulesByTenantId(tenantId);
        return count != null ? count : 0;
    }
}