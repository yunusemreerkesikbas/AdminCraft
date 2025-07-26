export interface User {
    id: number;
    name: string;
    email: string;
    avatar?: string;
    status?: string;
    role?: string;
    tenantId?: number;
    preferredLanguage?: string;
}
