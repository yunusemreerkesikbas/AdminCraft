package com.backend.application.service.config;

import java.util.List;

import com.backend.application.dto.config.ConfigAuditItemResult;
import com.backend.application.dto.config.ConfigPrincipal;
import com.backend.application.dto.config.ConfigRecaptchaResult;
import com.backend.presentation.dto.request.config.PatchConfigRecaptchaRequest;

public interface ConfigRecaptchaAdminService {

    ConfigRecaptchaResult getRecaptcha(ConfigPrincipal principal);

    ConfigRecaptchaResult patchRecaptcha(ConfigPrincipal principal, PatchConfigRecaptchaRequest request);

    List<ConfigAuditItemResult> getAuditTrail(ConfigPrincipal principal, int limit);
}
