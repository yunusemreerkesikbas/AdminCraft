package com.backend.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.CreateTenantRequest;
import com.backend.application.dto.request.UpdateTenantRequest;
import com.backend.application.dto.response.TenantProvisioningJobData;
import com.backend.application.dto.tenant.TenantModuleResponse;
import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProvisioningStatus;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.presentation.dto.response.TenantDetailResponse;
import com.backend.presentation.dto.response.TenantListResponse;
import com.backend.shared.constants.ValidationConstants;
import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;
import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import com.backend.infrastructure.persistence.platform.repository.ProvisioningJobRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final ProvisioningJobRepository provisioningJobRepository;
    private final com.backend.infrastructure.tenant.TenantContext tenantContext;
    private final FrontendConfigPort frontendConfig;

    @Override
    @Transactional
    public TenantDetailResponse createTenantWithDetail(CreateTenantRequest request, Language displayLanguage) {
        if (ValidationConstants.isReservedSubdomain(request.subdomain())) {
            throw new IllegalArgumentException("Subdomain is reserved and cannot be used: " + request.subdomain());
        }
        if (tenantRepository.existsBySubdomain(request.subdomain())) {
            throw new IllegalArgumentException("Subdomain already exists: " + request.subdomain());
        }

        Tenant tenant = new Tenant();
        tenant.setSubdomain(request.subdomain());
        tenant.setCompanyName(request.companyName());
        tenant.setDefaultLanguage(request.defaultLanguage());
        tenant.setSupportedLanguages(request.supportedLanguages());
        tenant.setCurrency(request.currency() != null ? request.currency() : com.backend.domain.enums.Currency.TRY);
        tenant.setNotes(request.notes());
        tenant.setAdminEmail(null);
        tenant.setAdminName(null);

        Tenant savedTenant = tenantRepository.save(tenant);

        ProvisioningStatus provisioningStatus = calculateProvisioningStatus(savedTenant.getId());
        Integer modulesCount = countProvisionedModules(savedTenant.getId());

        return TenantDetailResponse.from(savedTenant, displayLanguage, provisioningStatus, modulesCount,
                resolveTenantFullDomain(savedTenant));
    }

    @Override
    @Transactional
    public TenantDetailResponse updateTenantWithDetail(Long id, UpdateTenantRequest request, Language displayLanguage) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));

        if (request.companyName() != null) {
            tenant.setCompanyName(request.companyName());
        }
        if (request.defaultLanguage() != null) {
            tenant.setDefaultLanguage(request.defaultLanguage());
        }
        if (request.supportedLanguages() != null && !request.supportedLanguages().isEmpty()) {
            tenant.setSupportedLanguages(request.supportedLanguages());
        }
        if (request.currency() != null) {
            tenant.setCurrency(request.currency());
        }
        if (request.customDomain() != null) {
            if (!request.customDomain().isEmpty() &&
                    tenantRepository.existsByCustomDomainAndIdNot(request.customDomain(), id)) {
                throw new IllegalArgumentException("Custom domain already exists: " + request.customDomain());
            }
            tenant.setCustomDomain(request.customDomain());
        }
        if (request.notes() != null) {
            tenant.setNotes(request.notes());
        }

        Tenant updatedTenant = tenantRepository.save(tenant);

        ProvisioningStatus provisioningStatus = calculateProvisioningStatus(updatedTenant.getId());
        Integer modulesCount = countProvisionedModules(updatedTenant.getId());

        return TenantDetailResponse.from(updatedTenant, displayLanguage, provisioningStatus, modulesCount,
                resolveTenantFullDomain(updatedTenant));
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
        return withPlatformContext(() -> {
            log.debug("Cleared tenant context to access platform database for tenant: {}", tenantId);

            tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new TenantNotFoundException(tenantId));
            List<TenantModule> tenantModules = tenantModuleRepository.findByTenantIdAndStatus(tenantId, "enabled");

            if (tenantModules.isEmpty()) {
                log.warn("Tenant {} has no tenant_modules records; returning default core module for dashboard access", tenantId);
                return List.of(TenantModuleResponse.builder()
                        .moduleCode("core")
                        .moduleName("Core Module")
                        .status("enabled")
                        .build());
            }

            return tenantModules.stream()
                    .map(tm -> TenantModuleResponse.builder()
                            .id(tm.getId())
                            .moduleCode(tm.getModuleCode())
                            .moduleName(
                                    tm.getModuleCatalog() != null ? tm.getModuleCatalog().getName()
                                            : tm.getModuleCode())
                            .status(tm.getStatus())
                            .targetVersion(tm.getTargetVersion())
                            .installedAt(tm.getInstalledAt())
                            .build())
                    .toList();
        });
    }

    @Override
    public List<TenantProvisioningJobData> getTenantProvisioningJobs(Long tenantId) {
        return withPlatformContext(() -> provisioningJobRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(job -> new TenantProvisioningJobData(
                        job.getId(),
                        job.getTenantId(),
                        job.getType(),
                        job.getStatus(),
                        job.getProgress(),
                        job.getError(),
                        job.getCreatedAt(),
                        job.getStartedAt(),
                        job.getCompletedAt()))
                .toList());
    }

    @Override
    public TenantListResponse getTenantListById(Long id, Language displayLanguage) {
        return withPlatformContext(() -> {
            Tenant tenant = tenantRepository.findById(id)
                    .orElseThrow(() -> new TenantNotFoundException(id));
            ProvisioningStatus provisioningStatus = calculateProvisioningStatus(id);
            Integer modulesCount = countProvisionedModules(id);
            return TenantListResponse.from(tenant, displayLanguage, provisioningStatus, modulesCount);
        });
    }

    @Override
    public List<TenantListResponse> getAllTenantsAsList(Language displayLanguage) {
        return withPlatformContext(() -> {
            List<Tenant> tenants = tenantRepository.findAll();
            TenantMetrics metrics = loadTenantMetrics(tenants.stream().map(Tenant::getId).toList());
            return tenants.stream()
                    .map(tenant -> toTenantListResponse(tenant, displayLanguage, metrics))
                    .toList();
        });
    }

    @Override
    public List<TenantListResponse> getTenantsByStatusAsList(TenantStatus status, Language displayLanguage) {
        return withPlatformContext(() -> {
            List<Tenant> tenants = tenantRepository.findByStatus(status);
            TenantMetrics metrics = loadTenantMetrics(tenants.stream().map(Tenant::getId).toList());
            return tenants.stream()
                    .map(tenant -> toTenantListResponse(tenant, displayLanguage, metrics))
                    .toList();
        });
    }

    @Override
    public TenantDetailResponse getTenantDetailById(Long id, Language displayLanguage) {
        log.debug("Fetching tenant detail for tenant: {}", id);
        return withPlatformContext(() -> {
            log.debug("Cleared tenant context to access platform database for tenant: {}", id);

            Tenant tenant = tenantRepository.findById(id)
                    .orElseThrow(() -> new TenantNotFoundException(id));
            ProvisioningStatus provisioningStatus = calculateProvisioningStatus(id);
            Integer modulesCount = countProvisionedModules(id);
            return TenantDetailResponse.from(tenant, displayLanguage, provisioningStatus, modulesCount,
                    resolveTenantFullDomain(tenant));
        });
    }

    @Override
    public Page<TenantListResponse> searchTenants(
            String search,
            TenantStatus status,
            Pageable pageable,
            Language displayLanguage) {
        return withPlatformContext(() -> {
            Page<Tenant> page = tenantRepository.searchTenants(search, status, pageable);
            TenantMetrics metrics = loadTenantMetrics(page.getContent().stream().map(Tenant::getId).toList());
            List<TenantListResponse> mapped = page.getContent().stream()
                    .map(tenant -> toTenantListResponse(tenant, displayLanguage, metrics))
                    .toList();
            return new PageImpl<>(mapped, pageable, page.getTotalElements());
        });
    }

    private ProvisioningStatus calculateProvisioningStatus(Long tenantId) {
        Optional<ProvisioningJob> latestJob = provisioningJobRepository
                .findFirstByTenantIdOrderByCreatedAtDesc(tenantId);

        if (latestJob.isEmpty()) {
            return ProvisioningStatus.IDLE;
        }

        return mapProvisioningStatus(latestJob.get().getStatus());
    }

    private Integer countProvisionedModules(Long tenantId) {
        Integer count = tenantModuleRepository.countEnabledModulesByTenantId(tenantId);
        return count != null ? count : 0;
    }

    private TenantListResponse toTenantListResponse(Tenant tenant, Language displayLanguage, TenantMetrics metrics) {
        ProvisioningStatus provisioningStatus = metrics.provisioningStatusByTenantId()
                .getOrDefault(tenant.getId(), ProvisioningStatus.IDLE);
        Integer modulesCount = metrics.moduleCountByTenantId().getOrDefault(tenant.getId(), 0);
        return TenantListResponse.from(tenant, displayLanguage, provisioningStatus, modulesCount);
    }

    private String resolveTenantFullDomain(Tenant tenant) {
        if (tenant.getCustomDomain() != null && !tenant.getCustomDomain().isBlank()) {
            return tenant.getCustomDomain().trim();
        }
        String baseUrl = frontendConfig.getBaseUrl();
        String formatted = (baseUrl != null && baseUrl.contains("%s"))
                ? String.format(baseUrl, tenant.getSubdomain()).trim()
                : (baseUrl != null ? baseUrl.trim() : "");
        formatted = formatted.replaceFirst("^https?://", "");
        return formatted.replaceFirst("/.*$", "");
    }

    private TenantMetrics loadTenantMetrics(List<Long> tenantIds) {
        if (tenantIds.isEmpty()) {
            return new TenantMetrics(Map.of(), Map.of());
        }

        Map<Long, ProvisioningStatus> provisioningStatusByTenantId = new HashMap<>();
        List<Object[]> latestStatuses = provisioningJobRepository.findLatestStatusesByTenantIds(tenantIds);
        for (Object[] row : latestStatuses) {
            Long tenantId = ((Number) row[0]).longValue();
            String status = (String) row[1];
            provisioningStatusByTenantId.putIfAbsent(tenantId, mapProvisioningStatus(status));
        }

        Map<Long, Integer> moduleCountByTenantId = new HashMap<>();
        List<Object[]> moduleCounts = tenantModuleRepository.countEnabledModulesByTenantIds(tenantIds);
        for (Object[] row : moduleCounts) {
            Long tenantId = ((Number) row[0]).longValue();
            Integer moduleCount = ((Number) row[1]).intValue();
            moduleCountByTenantId.put(tenantId, moduleCount);
        }

        return new TenantMetrics(provisioningStatusByTenantId, moduleCountByTenantId);
    }

    private ProvisioningStatus mapProvisioningStatus(String status) {
        return switch (status) {
            case "pending", "running" -> ProvisioningStatus.PROVISIONING;
            case "failed" -> ProvisioningStatus.FAILED;
            default -> ProvisioningStatus.IDLE;
        };
    }

    private <T> T withPlatformContext(Supplier<T> action) {
        String savedTenantId = tenantContext.getTenantId();
        String savedTenantDbName = tenantContext.getTenantDbName();
        String savedSubdomain = tenantContext.getSubdomain();

        try {
            tenantContext.clear();
            return action.get();
        } finally {
            tenantContext.clear();
            if (savedTenantId != null) {
                tenantContext.setTenantId(savedTenantId);
            }
            if (savedTenantDbName != null) {
                tenantContext.setTenantDbName(savedTenantDbName);
            }
            if (savedSubdomain != null) {
                tenantContext.setSubdomain(savedSubdomain);
            }
            log.debug("Restored tenant context after platform database query");
        }
    }

    private record TenantMetrics(
            Map<Long, ProvisioningStatus> provisioningStatusByTenantId,
            Map<Long, Integer> moduleCountByTenantId) {
    }
}
