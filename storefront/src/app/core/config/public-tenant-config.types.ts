export interface PublicTenantConfig {
    recaptcha: RecaptchaConfig;
}

export interface RecaptchaConfig {
    enabled: boolean;
    siteKey: string | null;
    threshold: number;
}
