import { ApiResponse } from '../crud/api.types';

export interface LoginResponseData {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    userId: number;
    email: string;
    fullName?: string;
    role: string;
    preferredLanguage: string;
    subdomain: string;
    tenantId: number;
    requires2FA: boolean;
    pendingToken: string;
    resendCooldownSeconds?: number;
}

export type LoginResponse = ApiResponse<LoginResponseData>;

export interface VerifyOtpRequest {
    pendingToken: string;
    otpCode: string;
    trustDevice: boolean;
    deviceFingerprint: string;
    deviceName: string;
    tenantId: number;
    subdomain: string;
}

export interface TwoFactorPendingState {
    pendingToken: string;
    email: string;
    tenantId: number;
    subdomain: string;
}
