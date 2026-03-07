package com.backend.application.service.config;

public interface GlobalRuntimeConfigService {

    String getEmailProvider();

    String getEmailFromAddress();

    String getEmailFromName();

    String getFrontendBaseUrl();
}

