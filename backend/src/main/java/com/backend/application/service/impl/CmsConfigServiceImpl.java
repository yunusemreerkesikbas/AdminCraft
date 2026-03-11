package com.backend.application.service.impl;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.service.CmsConfigService;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.entity.ConfigProperty;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CmsConfigServiceImpl implements CmsConfigService {

    private final ConfigPropertyService configPropertyService;

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getPublicConfig(Long tenantId, String tenantDbName) {
        return configPropertyService.findAll(tenantId, tenantDbName).stream()
                .filter(p -> !Boolean.TRUE.equals(p.getSecret()))
                .collect(Collectors.toMap(
                        ConfigProperty::getConfigKey,
                        p -> p.getConfigValue() != null ? p.getConfigValue() : "",
                        (left, right) -> right,
                        LinkedHashMap::new));
    }
}
