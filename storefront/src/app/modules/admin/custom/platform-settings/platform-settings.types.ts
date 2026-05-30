export interface PlatformSettingsResponse {
    platformName: string;
    defaultLanguage: string;
    defaultCurrency: string;
    emailFromAddress: string;
    emailFromName: string;
    twoFactorPolicy: TwoFactorPolicy;
}

export interface PatchPlatformSettingsRequest {
    platformName?: string;
    defaultLanguage?: string;
    defaultCurrency?: string;
    emailFromAddress?: string;
    emailFromName?: string;
    twoFactorPolicy?: TwoFactorPolicy;
}

export type TwoFactorPolicy = 'DISABLED' | 'REQUIRED';

export interface RequestTwoFactorPolicyChangeRequest {
    twoFactorPolicy: TwoFactorPolicy;
}

export interface TwoFactorPolicyChangeRequestResponse {
    pendingChangeId: string;
    maskedEmail: string;
    targetPolicy: TwoFactorPolicy;
    expiresInSeconds: number;
    resendCooldownSeconds: number;
    emailSent: boolean;
}

export interface PendingOtpPolicySession {
    pendingChangeId: string;
    maskedEmail: string;
    targetPolicy: TwoFactorPolicy;
    expiresAt: number;
    resendCooldownSeconds: number;
}

export interface ConfirmTwoFactorPolicyChangeRequest {
    pendingChangeId: string;
    otpCode: string;
}
