export interface PlatformDemoRequestRow {
    id: number;
    uid: string;
    fullName: string;
    email: string;
    phone: string | null;
    message: string;
    messagePreview: string;
    locale: string;
    source: string;
    clientIp: string | null;
    userAgent: string | null;
    createdAt: string;
}
