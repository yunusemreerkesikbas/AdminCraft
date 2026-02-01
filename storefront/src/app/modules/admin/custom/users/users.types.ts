import { CrudEntity } from '@core/crud/api.types';

export interface User extends CrudEntity {
    email: string;
    fullName: string;
    firstName?: string;
    lastName?: string;
    role: UserRole;
    phone?: string;
    jobTitle?: string;
    department?: string;
    isActive: boolean;
    emailVerified: boolean;
    twoFactorEnabled: boolean;
    lastLoginAt?: string;
    lastLoginIp?: string;
    failedLoginAttempts: number;
    accountLocked: boolean;
    createdAt: string;
    updatedAt?: string;
    displayName: string;
    isSuperAdmin: boolean;
    isTenantAdmin: boolean;
}

export enum UserRole {
    SUPER_ADMIN = 'SUPER_ADMIN',
    TENANT_ADMIN = 'TENANT_ADMIN',
    EDITOR = 'EDITOR',
    VIEWER = 'VIEWER'
}

export interface CreateUserRequest {
    email: string;
    fullName: string;
    password: string;
    role: UserRole;
    firstName?: string;
    lastName?: string;
    phone?: string;
    jobTitle?: string;
    department?: string;
    isActive?: boolean;
    notes?: string;
}

export interface UpdateUserRequest {
    email?: string;
    fullName?: string;
    role?: UserRole;
    firstName?: string;
    lastName?: string;
    phone?: string;
    jobTitle?: string;
    department?: string;
    isActive?: boolean;
    notes?: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export interface ResetPasswordResponse {
    newPassword: string;
}
