package com.backend.infrastructure.email;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.port.FrontendConfigPort;

import lombok.RequiredArgsConstructor;

@Component
@Primary
@RequiredArgsConstructor
public class RuntimeFrontendConfig implements FrontendConfigPort {

    private final GlobalRuntimeConfigService globalRuntimeConfigService;

    @Override
    public String getBaseUrl() {
        return globalRuntimeConfigService.getFrontendBaseUrl();
    }
}

