package com.backend.presentation.dto.response;

import com.backend.application.dto.TwoFactorPolicyChangeRequestResult;
import com.backend.domain.enums.TwoFactorPolicy;

public record TwoFactorPolicyChangeRequestResponse(
        String pendingChangeId,
        String maskedEmail,
        TwoFactorPolicy targetPolicy,
        int expiresInSeconds) {

    public static TwoFactorPolicyChangeRequestResponse from(TwoFactorPolicyChangeRequestResult result) {
        return new TwoFactorPolicyChangeRequestResponse(
                result.pendingChangeId(),
                result.maskedEmail(),
                result.targetPolicy(),
                result.expiresInSeconds());
    }
}
