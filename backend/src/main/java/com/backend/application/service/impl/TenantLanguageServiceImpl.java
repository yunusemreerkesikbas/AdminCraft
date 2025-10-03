package com.backend.application.service.impl;

import com.backend.application.service.TenantLanguageService;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.mapper.TenantLanguagesMapper;
import com.backend.presentation.dto.request.TenantLanguagesUpdateRequest;
import com.backend.presentation.dto.response.TenantLanguagesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TenantLanguageServiceImpl implements TenantLanguageService {

    private final TenantRepository tenantRepository;
    private final TenantLanguagesMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public TenantLanguagesResponse getLanguages(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        return mapper.toResponse(tenant);
    }

    @Override
    @Transactional
    public TenantLanguagesResponse updateLanguages(Long tenantId, TenantLanguagesUpdateRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        if (!request.supportedLanguages().contains(request.defaultLanguage())) {
            throw new IllegalArgumentException("Default language must be in supported languages");
        }

        tenant.setDefaultLanguage(request.defaultLanguage());
        tenant.setSupportedLanguages(request.supportedLanguages());

        Tenant updated = tenantRepository.save(tenant);
        return mapper.toResponse(updated);
    }

    @Override
    public Set<Language> getNewlyAddedLanguages(Long tenantId, Set<Language> newSupportedLanguages) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        Set<Language> currentLanguages = tenant.getSupportedLanguages();
        Set<Language> newLanguages = new HashSet<>(newSupportedLanguages);
        newLanguages.removeAll(currentLanguages);

        return newLanguages;
    }
}
