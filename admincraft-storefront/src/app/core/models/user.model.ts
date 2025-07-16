export interface User {
    id: number;
    username: string;
    email: string;
    firstName?: string;
    lastName?: string;
    status: 'active' | 'inactive' | 'pending';
    roles: string[];
    createdAt: string;
    updatedAt: string;
} 