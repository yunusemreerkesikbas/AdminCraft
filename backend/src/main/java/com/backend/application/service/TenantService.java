package com.backend.application.service;

import com.backend.application.dto.request.CreateTenantRequest;
import com.backend.application.dto.request.UpdateTenantRequest;
import com.backend.application.dto.response.TenantProvisioningJobData;
import com.backend.application.dto.tenant.TenantModuleResponse;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.presentation.dto.response.TenantDetailResponse;
import com.backend.presentation.dto.response.TenantListResponse;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantService {

    void deleteTenant(Long id);

    boolean isCustomDomainAvailable(String customDomain);

    boolean hasAccessToTenant(String currentUserEmail, Long tenantId);

    List<TenantModuleResponse> getTenantModules(Long tenantId, Language displayLanguage);

    List<TenantProvisioningJobData> getTenantProvisioningJobs(Long tenantId);

    TenantDetailResponse createTenantWithDetail(CreateTenantRequest request, Language displayLanguage);

    TenantDetailResponse updateTenantWithDetail(Long id, UpdateTenantRequest request, Language displayLanguage);

    TenantListResponse getTenantListById(Long id, Language displayLanguage);

    List<TenantListResponse> getAllTenantsAsList(Language displayLanguage);

    List<TenantListResponse> getTenantsByStatusAsList(TenantStatus status, Language displayLanguage);

    TenantDetailResponse getTenantDetailById(Long id, Language displayLanguage);

    Page<TenantListResponse> searchTenants(
            String search,
            TenantStatus status,
            Pageable pageable,
            Language displayLanguage);
}
