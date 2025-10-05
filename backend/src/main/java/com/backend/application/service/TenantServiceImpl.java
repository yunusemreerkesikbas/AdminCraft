package com.backend.application.service;

import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
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
    public TenantResponse getTenantBySubdomain(String subdomain, Language displayLanguage) {
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found with subdomain: " + subdomain));
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

        // Track old supported languages to detect newly added ones
        List<Language> oldSupportedLanguages = new ArrayList<>(tenant.getSupportedLanguages());

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

        // Detect newly added languages and trigger provisioning
        if (request.supportedLanguages() != null && !request.supportedLanguages().isEmpty()) {
            List<Language> newLanguages = request.supportedLanguages().stream()
                .filter(lang -> !oldSupportedLanguages.contains(lang))
                .toList();

            if (!newLanguages.isEmpty()) {
                log.info("Detected {} new languages for tenant {}: {}",
                    newLanguages.size(), id, newLanguages);

                try {
                    provisioningService.createLanguageProvisioningJob(id, new java.util.HashSet<>(newLanguages));
                    log.info("Provisioning job created successfully for tenant {} with languages: {}",
                        id, newLanguages);
                } catch (Exception ex) {
                    log.error("Failed to create provisioning job for tenant {} with languages {}: {}",
                        id, newLanguages, ex.getMessage(), ex);
                    // Don't fail the tenant update if provisioning fails
                    // The provisioning can be retried manually if needed
                }
            }
        }

        return TenantResponse.from(updatedTenant, displayLanguage);
    }

    @Override
    @Transactional
    public TenantResponse activateTenant(Long id, Language displayLanguage) {
        Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + id));

        tenant.activate();
        Tenant activatedTenant = tenantRepository.save(tenant);
        return TenantResponse.from(activatedTenant, displayLanguage);
    }

    @Override
    @Transactional
    public TenantResponse suspendTenant(Long id, Language displayLanguage) {
        Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + id));

        tenant.suspend();
        Tenant suspendedTenant = tenantRepository.save(tenant);
        return TenantResponse.from(suspendedTenant, displayLanguage);
    }

    @Override
    @Transactional
    public TenantResponse setMaintenanceMode(Long id, Language displayLanguage) {
        Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id: " + id));

        tenant.setMaintenance();
        Tenant maintenanceTenant = tenantRepository.save(tenant);
        return TenantResponse.from(maintenanceTenant, displayLanguage);
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
    public boolean isSubdomainAvailable(String subdomain) {
        return !tenantRepository.existsBySubdomain(subdomain);
    }

    @Override
    public boolean isCustomDomainAvailable(String customDomain) {
        return !tenantRepository.existsByCustomDomain(customDomain);
    }

    @Override
    public long getTenantCountByStatus(TenantStatus status) {
        return tenantRepository.countByStatus(status);
    }

    @Override
    public boolean hasAccessToTenant(String currentUserEmail, Long tenantId) {
        log.debug("Checking tenant access for user {} to tenant {}", currentUserEmail, tenantId);
        
        try {
            // Get the current authentication context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // First, try to get tenantId from the authentication details (JWT token)
            if (authentication != null && authentication.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
                String role = (String) details.get("role");
                Long userTenantId = (Long) details.get("tenantId");
                
                log.debug("User {} has role {} and tenantId {} from token", currentUserEmail, role, userTenantId);
                
                // SUPER_ADMIN users have access to all tenants
                if ("SUPER_ADMIN".equals(role)) {
                    log.debug("User {} has SUPER_ADMIN role, granting access to tenant {}", currentUserEmail, tenantId);
                    return true;
                }
                
                // For other roles, check if user's tenant matches the requested tenant
                if (userTenantId != null && userTenantId.equals(tenantId)) {
                    log.debug("User {} has access to their own tenant {}", currentUserEmail, tenantId);
                    return true;
                }
                
                log.debug("User {} denied access to tenant {} (user tenant: {})", currentUserEmail, tenantId, userTenantId);
                return false;
            }
            
            // Fallback: if authentication details are not available, query the database
            log.debug("Authentication details not available, falling back to database query");
            
            // Find the current user
            Optional<User> currentUser = userRepository.findByEmail(currentUserEmail);
            if (currentUser.isEmpty()) {
                log.warn("User not found: {}", currentUserEmail);
                return false;
            }

            User user = currentUser.get();
            
            // SUPER_ADMIN users have access to all tenants
            if (user.getRole().name().equals("SUPER_ADMIN")) {
                log.debug("User {} has SUPER_ADMIN role, granting access to tenant {}", currentUserEmail, tenantId);
                return true;
            }

            // For other roles, check if user belongs to the tenant
            boolean hasAccess = user.getTenantId().equals(tenantId);
            log.debug("User {} access to tenant {}: {} (user tenant: {})", currentUserEmail, tenantId, hasAccess, user.getTenantId());
            return hasAccess;

        } catch (Exception ex) {
            log.error("Error checking tenant access for user {} to tenant {}: {}", 
                     currentUserEmail, tenantId, ex.getMessage());
            return false; // Deny access on error for security
        }
    }
}