export interface PlatformSettingsResponse {
    platformName: string;
    defaultLanguage: string;
    defaultCurrency: string;
    emailFromAddress: string;
    emailFromName: string;
    twoFactorPolicy: TwoFactorPolicy;
    recaptchaEnabled: boolean;
    recaptchaSiteKey: string | null;
    recaptchaThreshold: number;
}

export interface PatchPlatformSettingsRequest {
    platformName?: string;
    defaultLanguage?: string;
    defaultCurrency?: string;
    emailFromAddress?: string;
    emailFromName?: string;
    twoFactorPolicy?: TwoFactorPolicy;
    recaptchaEnabled?: boolean;
    recaptchaSiteKey?: string | null;
    recaptchaSecretKey?: string | null;
    recaptchaThreshold?: number;
}

export type TwoFactorPolicy = 'DISABLED' | 'REQUIRED';
