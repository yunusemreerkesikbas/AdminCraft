package com.backend.application.service.config;

import java.util.List;

import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.dto.config.ConfigPropertyResult;

public interface ConfigPropertiesAdminService {

    List<ConfigPropertyResult> listProperties(ConfigPrincipal principal);

    ConfigPropertyResult getProperty(ConfigPrincipal principal, String key);

    ConfigPropertyResult upsertProperty(ConfigPrincipal principal, String key, String value, boolean secret, String reason);

    void deleteProperty(ConfigPrincipal principal, String key, String reason);
}
