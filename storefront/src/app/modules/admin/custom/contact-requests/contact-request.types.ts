export interface PlatformContactRequestRow {
    id: number;
    uid: string;
    fullName: string;
    subject: string;
    message: string;
    messagePreview: string;
    locale: string;
    source: string;
    clientIp: string | null;
    userAgent: string | null;
    createdAt: string;
}
