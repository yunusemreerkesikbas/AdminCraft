export interface PlatformSettingsResponse {
    platformName: string;
    defaultLanguage: string;
    defaultCurrency: string;
    emailFromAddress: string;
    emailFromName: string;
}

export interface PatchPlatformSettingsRequest {
    platformName?: string;
    defaultLanguage?: string;
    defaultCurrency?: string;
    emailFromAddress?: string;
    emailFromName?: string;
}
