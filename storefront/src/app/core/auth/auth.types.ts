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
}

export type LoginResponse = ApiResponse<LoginResponseData>;
