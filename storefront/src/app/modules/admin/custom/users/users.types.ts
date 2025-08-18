export interface User {
    id: number;
    email: string;
    fullName: string;
    role: UserRole;
    preferredLanguage: Language;
    tenantId: number;
    tenantName?: string;
    isActive: boolean;
    lastLoginAt?: string;
    createdAt: string;
    updatedAt?: string;
}

export interface UserPagination {
    length: number;
    size: number;
    page: number;
    lastPage: number;
    startIndex: number;
    endIndex: number;
}

export enum UserRole {
    SUPER_ADMIN = 'SUPER_ADMIN',
    TENANT_ADMIN = 'TENANT_ADMIN',
    EDITOR = 'EDITOR',
    VIEWER = 'VIEWER'
}

export enum Language {
    TR = 'TR',
    EN = 'EN'
}

export interface CreateUserRequest {
    email: string;
    fullName: string;
    password: string;
    role: UserRole;
    preferredLanguage: Language;
    tenantId?: number;
}

export interface UpdateUserRequest {
    email?: string;
    fullName?: string;
    role?: UserRole;
    preferredLanguage?: Language;
    isActive?: boolean;
    password?: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}