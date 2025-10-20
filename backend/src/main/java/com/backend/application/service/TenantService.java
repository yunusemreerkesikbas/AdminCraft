package com.backend.application.service;

import com.backend.application.dto.tenant.TenantModuleResponse;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.presentation.dto.request.CreateTenantRequest;
import com.backend.presentation.dto.request.UpdateTenantRequest;
import com.backend.presentation.dto.response.TenantAdminInfoResponse;
import com.backend.presentation.dto.response.TenantDetailResponse;
import com.backend.presentation.dto.response.TenantListResponse;

import java.util.List;

public interface TenantService {

    void deleteTenant(Long id);

    boolean isCustomDomainAvailable(String customDomain);

    boolean hasAccessToTenant(String currentUserEmail, Long tenantId);

    List<TenantModuleResponse> getTenantModules(Long tenantId, Language displayLanguage);

    TenantDetailResponse createTenantWithDetail(CreateTenantRequest request, Language displayLanguage);

    TenantDetailResponse updateTenantWithDetail(Long id, UpdateTenantRequest request, Language displayLanguage);

    TenantListResponse getTenantListById(Long id, Language displayLanguage);

    List<TenantListResponse> getAllTenantsAsList(Language displayLanguage);

    List<TenantListResponse> getTenantsByStatusAsList(TenantStatus status, Language displayLanguage);

    TenantDetailResponse getTenantDetailById(Long id, Language displayLanguage);

    TenantAdminInfoResponse getTenantAdminInfo(Long id);
}