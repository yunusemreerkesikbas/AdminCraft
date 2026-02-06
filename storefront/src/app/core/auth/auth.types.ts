import { ApiResponse } from '../crud/api.types';

export interface LoginResponseData {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    userId: number;
    email: string;
    role: string;
    preferredLanguage: string;
    subdomain: string;
    tenantId: number;
    requires2FA: boolean;
    pendingToken: string;
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
