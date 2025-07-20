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
    
    TenantResponse getTenantBySubdomain(String subdomain, Language displayLanguage);
    
    List<TenantResponse> getAllTenants(Language displayLanguage);
    
    List<TenantResponse> getTenantsByStatus(TenantStatus status, Language displayLanguage);
    
    TenantResponse updateTenant(Long id, UpdateTenantRequest request, Language displayLanguage);
    
    TenantResponse activateTenant(Long id, Language displayLanguage);
    
    TenantResponse suspendTenant(Long id, Language displayLanguage);
    
    TenantResponse setMaintenanceMode(Long id, Language displayLanguage);
    
    void deleteTenant(Long id);
    
    boolean isSubdomainAvailable(String subdomain);
    
    boolean isCustomDomainAvailable(String customDomain);
    
    long getTenantCountByStatus(TenantStatus status);
}