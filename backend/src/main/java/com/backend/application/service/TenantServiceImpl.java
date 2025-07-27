package com.backend.application.service;

import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.request.CreateTenantRequest;
import com.backend.presentation.dto.request.UpdateTenantRequest;
import com.backend.presentation.dto.response.TenantResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TenantServiceImpl implements TenantService {

    @Autowired
    private TenantRepository tenantRepository;

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
    public Optional<Tenant> getTenantEntityById(Long id) {
        return tenantRepository.findById(id);
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
}