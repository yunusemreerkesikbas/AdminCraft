package com.backend.application.service;

import java.util.Map;

public interface CmsConfigService {

    Map<String, String> getPublicConfig(Long tenantId, String tenantDbName);
}
