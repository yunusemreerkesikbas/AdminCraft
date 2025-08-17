/**
 * Translation Types and Interfaces for Type Safety
 * Following Clean Architecture principles for domain modeling
 */

export enum SupportedLanguage {
    TR = 'tr',
    EN = 'en'
}

export interface TranslationConfig {
    defaultLang: SupportedLanguage;
    fallbackLang: SupportedLanguage;
    supportedLanguages: SupportedLanguage[];
    enableFallback: boolean;
    logMissingKeys: boolean;
}

export interface TranslationLoadError {
    language: string;
    module?: string;
    error: Error;
}

export interface AdminTranslations {
    admin: {
        common: {
            actions: {
                add: string;
                edit: string;
                delete: string;
                save: string;
                cancel: string;
                view: string;
                search: string;
                filter: string;
                export: string;
                import: string;
                refresh: string;
                close: string;
                back: string;
                next: string;
                previous: string;
                submit: string;
                reset: string;
                confirm: string;
            };
            status: {
                active: string;
                inactive: string;
                pending: string;
                suspended: string;
                maintenance: string;
                enabled: string;
                disabled: string;
            };
            validation: {
                required: string;
                email: string;
                minLength: string;
                maxLength: string;
                pattern: string;
                min: string;
                max: string;
                unique: string;
                invalid: string;
            };
            messages: {
                success: string;
                error: string;
                warning: string;
                info: string;
                loading: string;
                noData: string;
                confirmDelete: string;
                unsavedChanges: string;
            };
            fields: {
                name: string;
                email: string;
                phone: string;
                address: string;
                city: string;
                country: string;
                status: string;
                createdAt: string;
                updatedAt: string;
                description: string;
                title: string;
                type: string;
                category: string;
            };
        };
        dashboard: {
            title: string;
            overview: string;
            statistics: string;
            recentActivity: string;
            quickActions: string;
        };
        tenants: {
            title: string;
            list: string;
            create: string;
            edit: string;
            delete: string;
            activate: string;
            suspend: string;
            maintenance: string;
            fields: {
                subdomain: string;
                companyName: string;
                adminEmail: string;
                adminName: string;
                defaultLanguage: string;
                supportedLanguages: string;
                customDomain: string;
                timezone: string;
                currency: string;
                sslEnabled: string;
                notes: string;
            };
            searchPlaceholder: string;
            createSuccess: string;
            updateSuccess: string;
            deleteSuccess: string;
            activateSuccess: string;
            suspendSuccess: string;
        };
        users: {
            title: string;
            list: string;
            create: string;
            edit: string;
            delete: string;
            invite: string;
            profile: string;
            permissions: string;
            roles: string;
            fields: {
                fullName: string;
                email: string;
                role: string;
                status: string;
                lastLogin: string;
                permissions: string;
                avatar: string;
                phone: string;
                preferredLanguage: string;
            };
            searchPlaceholder: string;
            inviteSuccess: string;
            createSuccess: string;
            updateSuccess: string;
            deleteSuccess: string;
        };
        content: {
            title: string;
            list: string;
            create: string;
            edit: string;
            delete: string;
            publish: string;
            unpublish: string;
            draft: string;
            preview: string;
            translate: string;
            fields: {
                title: string;
                slug: string;
                content: string;
                excerpt: string;
                featuredImage: string;
                categories: string;
                tags: string;
                author: string;
                publishedAt: string;
                language: string;
                metaTitle: string;
                metaDescription: string;
                status: string;
            };
            searchPlaceholder: string;
            createSuccess: string;
            updateSuccess: string;
            deleteSuccess: string;
            publishSuccess: string;
            unpublishSuccess: string;
        };
        media: {
            title: string;
            library: string;
            upload: string;
            delete: string;
            edit: string;
            download: string;
            fields: {
                name: string;
                type: string;
                size: string;
                uploadedAt: string;
                altText: string;
                caption: string;
                folder: string;
            };
            searchPlaceholder: string;
            uploadSuccess: string;
            deleteSuccess: string;
            updateSuccess: string;
            dragDropText: string;
        };
        sites: {
            title: string;
            list: string;
            create: string;
            edit: string;
            delete: string;
            publish: string;
            preview: string;
            settings: string;
            themes: string;
            fields: {
                name: string;
                domain: string;
                theme: string;
                status: string;
                language: string;
                description: string;
                favicon: string;
                logo: string;
            };
            searchPlaceholder: string;
            createSuccess: string;
            updateSuccess: string;
            deleteSuccess: string;
            publishSuccess: string;
        };
    };
}

export interface TranslationModule {
    name: string;
    translations: Record<SupportedLanguage, any>;
}

export interface UserLanguagePreference {
    userId: number;
    language: SupportedLanguage;
    fallbackLanguage?: SupportedLanguage;
}

export interface TenantLanguageSettings {
    tenantId: number;
    defaultLanguage: SupportedLanguage;
    supportedLanguages: SupportedLanguage[];
    enableAutoDetection: boolean;
}