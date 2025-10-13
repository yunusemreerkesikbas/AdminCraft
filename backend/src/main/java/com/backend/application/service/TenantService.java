package com.backend.application.service;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.presentation.dto.request.CreateTenantRequest;
import com.backend.presentation.dto.request.UpdateTenantRequest;
import com.backend.presentation.dto.response.TenantResponse;

import java.util.List;

public interface TenantService {

    TenantResponse createTenant(CreateTenantRequest request, Language displayLanguage);

    TenantResponse getTenantById(Long id, Language displayLanguage);

    List<TenantResponse> getAllTenants(Language displayLanguage);

    List<TenantResponse> getTenantsByStatus(TenantStatus status, Language displayLanguage);

    TenantResponse updateTenant(Long id, UpdateTenantRequest request, Language displayLanguage);

    void deleteTenant(Long id);

    boolean isCustomDomainAvailable(String customDomain);

    // Security access control method
    boolean hasAccessToTenant(String currentUserEmail, Long tenantId);
}