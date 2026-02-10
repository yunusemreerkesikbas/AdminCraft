export enum TenantStatus {
    PENDING = 'PENDING',
    ACTIVE = 'ACTIVE',
    SUSPENDED = 'SUSPENDED',
    MAINTENANCE = 'MAINTENANCE'
}

export enum Language {
    TR = 'TR',
    EN = 'EN',
    ZH = 'ZH',
    HI = 'HI',
    ES = 'ES',
    FR = 'FR',
    RU = 'RU',
    AR = 'AR',
    BN = 'BN',
    PT = 'PT',
    UR = 'UR',
}

export const LANGUAGE_LABELS: Record<Language, string> = {
    [Language.TR]: 'Türkçe',
    [Language.EN]: 'English',
    [Language.ZH]: '中文',
    [Language.HI]: 'हिन्दी',
    [Language.ES]: 'Español',
    [Language.FR]: 'Français',
    [Language.RU]: 'Русский',
    [Language.AR]: 'العربية',
    [Language.BN]: 'বাংলা',
    [Language.PT]: 'Português',
    [Language.UR]: 'اردو',
};

export enum ProvisioningJobStatus {
    PENDING = 'PENDING',
    RUNNING = 'RUNNING',
    COMPLETED = 'COMPLETED',
    FAILED = 'FAILED'
}

export enum SyncJobStatus {
    PENDING = 'pending',
    RUNNING = 'running',
    SUCCEEDED = 'succeeded',
    FAILED = 'failed'
}

/**
 * Status CSS class mapping utility
 */
export class StatusUtils {
    static getTenantStatusClass(status: TenantStatus): string {
        const map: Record<TenantStatus, string> = {
            [TenantStatus.ACTIVE]: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
            [TenantStatus.PENDING]: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
            [TenantStatus.SUSPENDED]: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
            [TenantStatus.MAINTENANCE]: 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
        };
        return map[status];
    }

    static getJobStatusClass(status: SyncJobStatus): string {
        const map: Record<SyncJobStatus, string> = {
            [SyncJobStatus.SUCCEEDED]: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
            [SyncJobStatus.PENDING]: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
            [SyncJobStatus.RUNNING]: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
            [SyncJobStatus.FAILED]: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        };
        return map[status];
    }
}
