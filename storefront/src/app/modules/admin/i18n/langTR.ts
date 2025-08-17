export const langTR = {
    admin: {
        common: {
            grid: {
                title: 'Başlık',
                name: 'Ad',
                role: 'Rol',
                status: 'Durum',
                language: 'Dil',
                domain: 'Alan Adı',
                type: 'Tür',
                size: 'Boyut',
                uploaded: 'Yüklendi',
                created: 'Oluşturulma',
                details: 'Detaylar',
            },
            status: {
                active: 'Aktif',
                inactive: 'Pasif',
            },
            actions: {
                add: 'Ekle',
                update: 'Güncelle',
                delete: 'Sil',
                publish: 'Yayınla',
                archive: 'Arşivle',
                activate: 'Aktifleştir',
                deactivate: 'Pasifleştir',
                upload: 'Yükle',
                cancel: 'İptal',
                save: 'Kaydet',
                create: 'Oluştur',
                manage: 'Yönet',
            },
            messages: {
                operationSuccess: 'İşlem başarılı',
                operationError: 'Bir hata oluştu, tekrar deneyin!',
            },
            confirm: {
                delete: {
                    title: 'Silinsin mi?',
                    message:
                        'Bu öğeyi silmek istediğinize emin misiniz? Bu işlem geri alınamaz!',
                    label: 'Sil',
                },
            },
        },
        content: {
            title: 'İçerik Yönetimi',
            searchPlaceholder: 'İçerik ara',
            add: '@:admin.common.actions.add',
            grid: {
                title: '@:admin.common.grid.title',
                status: '@:admin.common.grid.status',
                language: '@:admin.common.grid.language',
                created: '@:admin.common.grid.created',
                details: '@:admin.common.grid.details',
            },
            status: {
                DRAFT: 'Taslak',
                PUBLISHED: 'Yayında',
                ARCHIVED: 'Arşivlendi',
            },
            form: {
                title: '@:admin.common.grid.title',
                slug: 'Slug',
                contentType: 'İçerik Tipi',
                status: '@:admin.common.grid.status',
                language: '@:admin.common.grid.language',
                parentContentId: 'Üst İçerik ID (çeviriler için)',
                metaTitle: 'Meta Başlık',
                metaDescription: 'Meta Açıklama',
                contentData: 'İçerik Verisi (JSON)',
            },
            actions: {
                publish: '@:admin.common.actions.publish',
                archive: '@:admin.common.actions.archive',
                delete: '@:admin.common.actions.delete',
                update: '@:admin.common.actions.update',
                add: '@:admin.common.actions.add',
            },
            messages: {},
            validation: {
                titleRequired: 'Başlık zorunludur',
                slugRequired: 'Slug zorunludur',
                slugPattern:
                    'Slug yalnızca küçük harf, sayı ve tire içermelidir',
                contentTypeRequired: 'İçerik tipi zorunludur',
            },
            confirm: {
                deleteTitle: 'İçerik silinsin mi?',
                deleteMsg:
                    'Bu içeriği silmek istediğinize emin misiniz? Bu işlem geri alınamaz!',
                deleteLabel: 'Sil',
            },
        },
        media: {
            title: 'Medya Yönetimi',
            searchPlaceholder: 'Medya dosyası ara',
            upload: '@:admin.common.actions.upload',
            grid: {
                fileName: 'Dosya Adı',
                type: '@:admin.common.grid.type',
                size: '@:admin.common.grid.size',
                uploaded: '@:admin.common.grid.uploaded',
                details: '@:admin.common.grid.details',
            },
            by: 'yükleyen',
            form: {
                originalName: 'Orijinal Ad',
                altTextTr: 'Alt Metin (Türkçe)',
                altTextEn: 'Alt Metin (İngilizce)',
                selectFile: 'Dosya Seç',
            },
            actions: {
                delete: '@:admin.common.actions.delete',
                update: '@:admin.common.actions.update',
                upload: '@:admin.common.actions.upload',
            },
            messages: {
                uploaded: 'Medya dosyası yüklendi',
            },
            validation: {
                originalNameRequired: 'Dosya adı zorunludur',
                fileRequired: 'Dosya zorunludur',
            },
            confirm: {
                deleteTitle: 'Medya dosyası silinsin mi?',
                deleteMsg:
                    'Bu medya dosyasını silmek istediğinize emin misiniz? Bu işlem geri alınamaz!',
                deleteLabel: 'Sil',
            },
        },
        sites: {
            title: 'Site Yönetimi',
            searchPlaceholder: 'Site ara',
            add: '@:admin.common.actions.add',
            grid: {
                siteName: 'Site Adı',
                domain: '@:admin.common.grid.domain',
                status: '@:admin.common.grid.status',
                created: '@:admin.common.grid.created',
                details: '@:admin.common.grid.details',
            },
            status: { active: '@:admin.common.status.active', inactive: '@:admin.common.status.inactive' },
            domain: { none: 'Alan adı eklenmemiş' },
            created: { published: 'Yayınlandı:' },
            menus: {
                title: 'Site Menülerı',
                manage: 'Menüleri Yönet',
                items: 'menü öğesi',
                empty: 'Bu site için menü bulunamadı',
            },
            form: {
                siteName: 'Site Adı',
                description: 'Açıklama',
                domain: '@:admin.common.grid.domain',
                tenantId: 'Tenant ID',
                defaultLanguage: 'Varsayılan Dil',
                enabledLanguages: 'Aktif Diller',
                theme: 'Tema',
                logoUrl: 'Logo URL',
                faviconUrl: 'Favicon URL',
                isActive: '@:admin.common.status.active',
            },
            actions: {
                publish: '@:admin.common.actions.publish',
                activate: '@:admin.common.actions.activate',
                deactivate: '@:admin.common.actions.deactivate',
                delete: '@:admin.common.actions.delete',
                update: '@:admin.common.actions.update',
                add: '@:admin.common.actions.add',
            },
            messages: {},
            validation: {
                siteNameRequired: 'Site adı zorunludur',
                defaultLanguageRequired: 'Varsayılan dil zorunludur',
                enabledLanguagesRequired: 'En az bir dil aktif olmalıdır',
            },
            confirm: {
                deleteTitle: 'Site silinsin mi?',
                deleteMsg:
                    'Bu siteyi silmek istediğinize emin misiniz? Bu işlem geri alınamaz!',
                deleteLabel: 'Sil',
            },
        },
        users: {
            title: 'Kullanıcı Yönetimi',
            searchPlaceholder: 'Kullanıcı ara',
            add: '@:admin.common.actions.add',
            grid: {
                name: '@:admin.common.grid.name',
                role: '@:admin.common.grid.role',
                status: '@:admin.common.grid.status',
                created: '@:admin.common.grid.created',
                details: '@:admin.common.grid.details',
            },
            status: { active: '@:admin.common.status.active', inactive: '@:admin.common.status.inactive' },
            created: { last: 'Son:' },
            form: {
                fullName: 'Ad Soyad',
                email: 'E-posta',
                password: 'Şifre',
                role: 'Rol',
                preferredLanguage: 'Tercih Edilen Dil',
                tenantId: 'Tenant ID',
                isActive: '@:admin.common.status.active',
            },
            actions: {
                activate: '@:admin.common.actions.activate',
                deactivate: '@:admin.common.actions.deactivate',
                delete: '@:admin.common.actions.delete',
                update: '@:admin.common.actions.update',
                add: '@:admin.common.actions.add',
                cancel: '@:admin.common.actions.cancel',
                changePassword: 'Şifre Değiştir',
                resetPassword: 'Şifre Sıfırla',
            },
            messages: {},
            password: {
                management: 'Şifre Yönetimi',
                current: 'Mevcut Şifre',
                new: 'Yeni Şifre',
                confirm: 'Şifreyi Doğrula',
                change: 'Şifreyi Değiştir',
                generated: 'Yeni şifre oluşturuldu:',
                note: 'Lütfen bu şifreyi güvenli bir yerde saklayın. Tekrar gösterilmeyecek.',
            },
            validation: {
                fullNameRequired: 'Ad soyad zorunludur',
                emailRequired: 'E-posta zorunludur',
                emailValid: 'E-posta geçerli olmalıdır',
                passwordRequired: 'Şifre zorunludur',
                newPasswordRequired: 'Yeni şifre zorunludur',
                newPasswordMin: 'Şifre en az 6 karakter olmalıdır',
                confirmPasswordRequired: 'Şifre doğrulama zorunludur',
                passwordsMismatch: 'Şifreler uyuşmuyor',
            },
            confirm: {
                deleteTitle: 'Kullanıcı silinsin mi?',
                deleteMsg:
                    'Bu kullanıcıyı silmek istediğinize emin misiniz? Bu işlem geri alınamaz!',
                deleteLabel: 'Sil',
                resetTitle: 'Şifre sıfırlansın mı?',
                resetMsg:
                    'Bu kullanıcının şifresini sıfırlamak istediğinize emin misiniz? Yeni bir şifre üretilecektir.',
                resetLabel: 'Sıfırla',
            },
        },
        tenants: {
            title: 'Tenant Yönetimi',
            searchPlaceholder: 'Tenant ara',
            add: 'Tenant Ekle',
            status: {
                ACTIVE: 'Aktif',
                PENDING: 'Beklemede',
                SUSPENDED: 'Askıda',
                MAINTENANCE: 'Bakımda'
            },
            grid: {
                subdomain: 'Alt Alan Adı',
                company: 'Şirket',
                status: 'Durum',
                language: 'Dil',
                created: 'Oluşturulma',
                details: 'Detaylar',
                empty: 'Tenant bulunamadı!'
            },
            actions: {
                activate: 'Aktifleştir',
                suspend: 'Askıya Al',
                maintenance: 'Bakım',
                reactivate: 'Yeniden Aktifleştir',
                endMaintenance: 'Bakımı Bitir',
                delete: 'Sil',
                update: 'Güncelle',
                create: 'Oluştur'
            },
            form: {
                companyName: 'Şirket Adı',
                subdomain: 'Alt Alan Adı',
                adminName: 'Yönetici Adı',
                adminEmail: 'Yönetici E-posta',
                phone: 'Telefon',
                defaultLanguage: 'Varsayılan Dil',
                customDomain: 'Özel Alan Adı',
                timezone: 'Zaman Dilimi',
                currency: 'Para Birimi',
                sslEnabled: 'SSL Etkin',
                notes: 'Notlar',
                domainSuffix: '.admincraft.com'
            },
            messages: {
                confirmDeleteTitle: 'Tenant silinsin mi?',
                confirmDeleteMsg:
                    'Tenantı silmek istediğinize emin misiniz? Bu işlem geri alınamaz!',
                operationSuccess: 'İşlem başarılı',
                operationError: 'Bir hata oluştu, tekrar deneyin!'
            }
        }
    }
};


