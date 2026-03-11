package com.backend.infrastructure.email;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.port.MailConfigPort;

import lombok.RequiredArgsConstructor;

@Component
@Primary
@RequiredArgsConstructor
public class RuntimeMailConfig implements MailConfigPort {

    private final GlobalRuntimeConfigService globalRuntimeConfigService;
    private final EmailProperties emailProperties;

    @Override
    public String getProvider() {
        return globalRuntimeConfigService.getEmailProvider();
    }

    @Override
    public String getFromAddress() {
        return globalRuntimeConfigService.getEmailFromAddress();
    }

    @Override
    public String getFromName() {
        return globalRuntimeConfigService.getEmailFromName();
    }

    @Override
    public boolean isEnabled() {
        return emailProperties.isEnabled();
    }

    @Override
    public boolean isLogSimplified() {
        return emailProperties.isLogSimplified();
    }
}

