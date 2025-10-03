package com.backend.presentation.dto.mapper;

import com.backend.domain.entity.Tenant;
import com.backend.presentation.dto.response.TenantLanguagesResponse;
import org.springframework.stereotype.Component;

@Component
public class TenantLanguagesMapper {

    public TenantLanguagesResponse toResponse(Tenant tenant) {
        return new TenantLanguagesResponse(
                tenant.getId(),
                tenant.getDefaultLanguage(),
                tenant.getSupportedLanguages());
    }
}
