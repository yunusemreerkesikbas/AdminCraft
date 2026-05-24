package com.backend.application.service;

import com.backend.application.dto.SecuritySettingsResult;
import com.backend.application.dto.TwoFactorPolicyChangeRequestResult;
import com.backend.application.dto.UpdateSecuritySettingsCommand;
import com.backend.domain.enums.TwoFactorPolicy;

public interface SecuritySettingsService {

    SecuritySettingsResult getSecuritySettings();

    SecuritySettingsResult updateTwoFactorPolicy(TwoFactorPolicy policy);

    SecuritySettingsResult updateSecuritySettings(UpdateSecuritySettingsCommand command);

    TwoFactorPolicyChangeRequestResult requestTwoFactorPolicyChange(
            TwoFactorPolicy targetPolicy,
            String ipAddress,
            String userAgent);

    SecuritySettingsResult confirmTwoFactorPolicyChange(String pendingChangeId, String otpCode);
}
