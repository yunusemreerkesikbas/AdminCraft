package com.backend.application.service;

import com.backend.application.dto.SecuritySettingsResult;
import com.backend.domain.enums.TwoFactorPolicy;

public interface SecuritySettingsService {

    SecuritySettingsResult getSecuritySettings();

    SecuritySettingsResult updateTwoFactorPolicy(TwoFactorPolicy policy);
}
