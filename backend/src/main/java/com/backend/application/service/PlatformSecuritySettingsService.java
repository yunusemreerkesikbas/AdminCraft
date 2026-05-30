package com.backend.application.service;

import com.backend.application.dto.TwoFactorPolicyChangeRequestResult;
import com.backend.application.dto.response.PlatformSettingsData;
import com.backend.domain.enums.TwoFactorPolicy;

public interface PlatformSecuritySettingsService {

    TwoFactorPolicyChangeRequestResult requestTwoFactorPolicyChange(
            TwoFactorPolicy targetPolicy,
            String ipAddress,
            String userAgent);

    PlatformSettingsData confirmTwoFactorPolicyChange(String pendingChangeId, String otpCode);
}
