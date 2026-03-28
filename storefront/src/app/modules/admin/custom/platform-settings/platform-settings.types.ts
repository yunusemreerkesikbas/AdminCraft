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
