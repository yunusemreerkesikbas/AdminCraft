import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface LanguageDefinition {
    id: string;
    title: string;
    flag: string;
}

@Injectable({
    providedIn: 'root'
})
export class LanguageService {
    private _currentLanguage: BehaviorSubject<string> = new BehaviorSubject('tr');
    private _availableLanguages: LanguageDefinition[] = [
        {
            id: 'tr',
            title: 'Türkçe',
            flag: 'TR'
        },
        {
            id: 'en',
            title: 'English',
            flag: 'US'
        }
    ];

    /**
     * Constructor
     */
    constructor() {
        // Set the initial language from local storage or default to Turkish
        const savedLanguage = localStorage.getItem('admincraft-language') || 'tr';
        this._currentLanguage.next(savedLanguage);
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for current language
     */
    get currentLanguage$(): Observable<string> {
        return this._currentLanguage.asObservable();
    }

    /**
     * Getter for current language value
     */
    get currentLanguage(): string {
        return this._currentLanguage.value;
    }

    /**
     * Getter for available languages
     */
    get availableLanguages(): LanguageDefinition[] {
        return this._availableLanguages;
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Set the current language
     *
     * @param language
     */
    setCurrentLanguage(language: string): void {
        // Store the language in local storage
        localStorage.setItem('admincraft-language', language);

        // Update the current language
        this._currentLanguage.next(language);

        // Update the document language attribute
        document.documentElement.lang = language;
    }

    /**
     * Get language definition by id
     *
     * @param id
     */
    getLanguageById(id: string): LanguageDefinition | undefined {
        return this._availableLanguages.find(language => language.id === id);
    }

    /**
     * Get localized text based on current language
     * This is a simple implementation - you might want to use a proper i18n library
     *
     * @param key
     * @param params
     */
    getLocalizedText(key: string, params?: any): string {
        const translations: { [key: string]: { [lang: string]: string } } = {
            'tenant.management': {
                'tr': 'Kiracı Yönetimi',
                'en': 'Tenant Management'
            },
            'tenant.create': {
                'tr': 'Kiracı Oluştur',
                'en': 'Create Tenant'
            },
            'tenant.edit': {
                'tr': 'Kiracı Düzenle',
                'en': 'Edit Tenant'
            },
            'tenant.delete': {
                'tr': 'Kiracı Sil',
                'en': 'Delete Tenant'
            },
            'tenant.activate': {
                'tr': 'Kiracı Aktifleştir',
                'en': 'Activate Tenant'
            },
            'tenant.suspend': {
                'tr': 'Kiracı Askıya Al',
                'en': 'Suspend Tenant'
            },
            'tenant.maintenance': {
                'tr': 'Bakım Modu',
                'en': 'Maintenance Mode'
            },
            'fields.subdomain': {
                'tr': 'Alt Alan',
                'en': 'Subdomain'
            },
            'fields.companyName': {
                'tr': 'Şirket Adı',
                'en': 'Company Name'
            },
            'fields.adminEmail': {
                'tr': 'Admin E-posta',
                'en': 'Admin Email'
            },
            'fields.adminName': {
                'tr': 'Admin Adı',
                'en': 'Admin Name'
            },
            'fields.status': {
                'tr': 'Durum',
                'en': 'Status'
            },
            'fields.language': {
                'tr': 'Dil',
                'en': 'Language'
            },
            'actions.save': {
                'tr': 'Kaydet',
                'en': 'Save'
            },
            'actions.cancel': {
                'tr': 'İptal',
                'en': 'Cancel'
            },
            'actions.delete': {
                'tr': 'Sil',
                'en': 'Delete'
            },
            'actions.edit': {
                'tr': 'Düzenle',
                'en': 'Edit'
            },
            'status.PENDING': {
                'tr': 'Beklemede',
                'en': 'Pending'
            },
            'status.ACTIVE': {
                'tr': 'Aktif',
                'en': 'Active'
            },
            'status.SUSPENDED': {
                'tr': 'Askıya Alınmış',
                'en': 'Suspended'
            },
            'status.MAINTENANCE': {
                'tr': 'Bakım',
                'en': 'Maintenance'
            },
            'messages.success': {
                'tr': 'İşlem başarıyla tamamlandı',
                'en': 'Operation completed successfully'
            },
            'messages.error': {
                'tr': 'İşlem sırasında hata oluştu',
                'en': 'An error occurred during operation'
            }
        };

        const currentLang = this.currentLanguage;
        const translation = translations[key];
        
        if (translation && translation[currentLang]) {
            let text = translation[currentLang];
            
            // Simple parameter replacement
            if (params) {
                Object.keys(params).forEach(param => {
                    text = text.replace(`{{${param}}}`, params[param]);
                });
            }
            
            return text;
        }

        // Return the key if translation is not found
        return key;
    }
}