export interface ConfigAuthChallengeResponse {
    pendingToken: string;
    email: string;
    tenantId: number | null;
    subdomain: string | null;
    role: string;
}

export interface ConfigAuthResponse {
    accessToken: string;
    refreshToken: string | null;
    tokenType: string;
    expiresIn: number;
    issuedAt: number;
    userId: number;
    email: string;
    fullName: string;
    role: string;
    tenantId: number | null;
    subdomain: string | null;
}

export interface ConfigTokenState {
    accessToken: string;
    refreshToken: string | null;
    tokenType: string;
    expiresIn: number;
    userId: number;
    email: string;
    fullName: string;
    role: string;
    tenantId: number | null;
    subdomain: string | null;
    issuedAt: number;
}

export interface ConfigRecaptchaState {
    enabled: boolean;
    siteKeyMasked: string | null;
    secretConfigured: boolean;
    updatedAt: string | null;
}

export interface ConfigAuditItem {
    id: number;
    actorUserId: number;
    actorEmail: string;
    actorRole: string;
    targetTenantId: number;
    action: string;
    reason: string;
    beforeJson: string;
    afterJson: string;
    correlationId: string | null;
    createdAt: string;
}

export interface PatchRecaptchaPayload {
    recaptchaEnabled: boolean;
    recaptchaSiteKey?: string | null;
    recaptchaSecretKey?: string | null;
    reason: string;
}

export interface ConfigSection {
    id: string;
    title: string;
    description: string;
    icon: string;
}

export interface ConfigProperty {
    key: string;
    value: string | null;
    secret: boolean;
    updatedAt: string | null;
}

export interface UpsertPropertyPayload {
    value: string | null;
    secret: boolean;
    reason: string;
}
