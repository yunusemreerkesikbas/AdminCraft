package com.backend.application.service;

import com.backend.application.dto.tenant.TenantModuleResponse;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;
import com.backend.presentation.dto.request.CreateTenantRequest;
import com.backend.presentation.dto.request.UpdateTenantRequest;
import com.backend.presentation.dto.response.TenantResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Override
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request, Language displayLanguage) {
        if (tenantRepository.existsBySubdomain(request.subdomain())) {
            throw new IllegalArgumentException("Subdomain already exists: " + request.subdomain());
        }

        Tenant tenant = new Tenant();
        tenant.setSubdomain(request.subdomain());
        tenant.setCompanyName(request.companyName());
        tenant.setAdminEmail(request.adminEmail());
        tenant.setAdminName(request.adminName());
        tenant.setPhone(request.phone());
        tenant.setDefaultLanguage(request.defaultLanguage());
        tenant.setSupportedLanguages(request.supportedLanguages());
        tenant.setTimezone(request.timezone());
        tenant.setCurrency(request.currency());
        tenant.setNotes(request.notes());

        Tenant savedTenant = tenantRepository.save(tenant);
        return TenantResponse.from(savedTenant, displayLanguage);
    }

    @Override
    public TenantResponse getTenantById(Long id, Language displayLanguage) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + id));
        return TenantResponse.from(tenant, displayLanguage);
    }

    @Override
    public List<TenantResponse> getAllTenants(Language displayLanguage) {
        return tenantRepository.findAll().stream()
                .map(tenant -> TenantResponse.from(tenant, displayLanguage))
                .toList();
    }

    @Override
    public List<TenantResponse> getTenantsByStatus(TenantStatus status, Language displayLanguage) {
        return tenantRepository.findByStatus(status).stream()
                .map(tenant -> TenantResponse.from(tenant, displayLanguage))
                .toList();
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(Long id, UpdateTenantRequest request, Language displayLanguage) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + id));

        if (request.companyName() != null) {
            tenant.setCompanyName(request.companyName());
        }
        if (request.adminEmail() != null) {
            tenant.setAdminEmail(request.adminEmail());
        }
        if (request.adminName() != null) {
            tenant.setAdminName(request.adminName());
        }
        if (request.phone() != null) {
            tenant.setPhone(request.phone());
        }
        if (request.defaultLanguage() != null) {
            tenant.setDefaultLanguage(request.defaultLanguage());
        }
        if (request.supportedLanguages() != null && !request.supportedLanguages().isEmpty()) {
            tenant.setSupportedLanguages(request.supportedLanguages());
        }
        if (request.customDomain() != null) {
            if (!request.customDomain().isEmpty() &&
                    tenantRepository.existsByCustomDomainAndIdNot(request.customDomain(), id)) {
                throw new IllegalArgumentException("Custom domain already exists: " + request.customDomain());
            }
            tenant.setCustomDomain(request.customDomain());
        }
        if (request.sslEnabled() != null) {
            tenant.setSslEnabled(request.sslEnabled());
        }
        if (request.timezone() != null) {
            tenant.setTimezone(request.timezone());
        }
        if (request.currency() != null) {
            tenant.setCurrency(request.currency());
        }
        if (request.notes() != null) {
            tenant.setNotes(request.notes());
        }

        Tenant updatedTenant = tenantRepository.save(tenant);

        return TenantResponse.from(updatedTenant, displayLanguage);
    }

    @Override
    @Transactional
    public void deleteTenant(Long id) {
        if (tenantRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Tenant not found with id: " + id);
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
            boolean hasAccess = user.getTenantId().equals(tenantId);
            log.debug("User {} access to tenant {}: {} (user tenant: {})", currentUserEmail, tenantId, hasAccess,
                    user.getTenantId());
            return hasAccess;

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
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + tenantId));
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
}