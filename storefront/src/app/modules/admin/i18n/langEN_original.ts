export const langEN = {
    admin: {
        common: {
            grid: {
                title: 'Title',
                name: 'Name',
                role: 'Role',
                status: 'Status',
                language: 'Language',
                domain: 'Domain',
                type: 'Type',
                size: 'Size',
                uploaded: 'Uploaded',
                created: 'Created',
                details: 'Details',
            },
            status: {
                active: 'Active',
                inactive: 'Inactive',
            },
            actions: {
                add: 'Add',
                update: 'Update',
                delete: 'Delete',
                publish: 'Publish',
                archive: 'Archive',
                activate: 'Activate',
                deactivate: 'Deactivate',
                upload: 'Upload',
                cancel: 'Cancel',
                save: 'Save',
                create: 'Create',
                manage: 'Manage',
            },
            messages: {
                operationSuccess: 'Operation successful',
                operationError: 'An error occurred, try again!',
            },
            confirm: {
                delete: {
                    title: 'Delete',
                    message:
                        'Are you sure you want to delete this item? This action cannot be undone!',
                    label: 'Delete',
                },
            },
        },
        content: {
            title: 'Content Management',
            searchPlaceholder: 'Search contents',
            add: '@:admin.common.actions.add',
            grid: {
                title: '@:admin.common.grid.title',
                status: '@:admin.common.grid.status',
                language: '@:admin.common.grid.language',
                created: '@:admin.common.grid.created',
                details: '@:admin.common.grid.details',
            },
            status: {
                DRAFT: 'Draft',
                PUBLISHED: 'Published',
                ARCHIVED: 'Archived',
            },
            form: {
                title: '@:admin.common.grid.title',
                slug: 'Slug',
                contentType: 'Content Type',
                status: '@:admin.common.grid.status',
                language: '@:admin.common.grid.language',
                parentContentId: 'Parent Content ID (for translations)',
                metaTitle: 'Meta Title',
                metaDescription: 'Meta Description',
                contentData: 'Content Data (JSON)',
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
                titleRequired: 'Title is required',
                slugRequired: 'Slug is required',
                slugPattern:
                    'Slug must contain only lowercase letters, numbers, and hyphens',
                contentTypeRequired: 'Content type is required',
            },
            confirm: {
                deleteTitle: 'Delete content',
                deleteMsg:
                    'Are you sure you want to remove this content? This action cannot be undone!',
                deleteLabel: 'Delete',
            },
        },
        media: {
            title: 'Media Management',
            searchPlaceholder: 'Search media files',
            upload: '@:admin.common.actions.upload',
            grid: {
                fileName: 'File Name',
                type: '@:admin.common.grid.type',
                size: '@:admin.common.grid.size',
                uploaded: '@:admin.common.grid.uploaded',
                details: '@:admin.common.grid.details',
            },
            by: 'by',
            form: {
                originalName: 'Original Name',
                altTextTr: 'Alt Text (Turkish)',
                altTextEn: 'Alt Text (English)',
                selectFile: 'Select File',
            },
            actions: {
                delete: '@:admin.common.actions.delete',
                update: '@:admin.common.actions.update',
                upload: '@:admin.common.actions.upload',
            },
            messages: {
                uploaded: 'Media file uploaded',
            },
            validation: {
                originalNameRequired: 'Original name is required',
                fileRequired: 'File is required',
            },
            confirm: {
                deleteTitle: 'Delete media file',
                deleteMsg:
                    'Are you sure you want to remove this media file? This action cannot be undone!',
                deleteLabel: 'Delete',
            },
        },
        sites: {
            title: 'Site Management',
            searchPlaceholder: 'Search sites',
            add: '@:admin.common.actions.add',
            grid: {
                siteName: 'Site Name',
                domain: '@:admin.common.grid.domain',
                status: '@:admin.common.grid.status',
                created: '@:admin.common.grid.created',
                details: '@:admin.common.grid.details',
            },
            status: { active: '@:admin.common.status.active', inactive: '@:admin.common.status.inactive' },
            domain: { none: 'No domain set' },
            created: { published: 'Published:' },
            menus: {
                title: 'Site Menus',
                manage: 'Manage Menus',
                items: 'menu items',
                empty: 'No menus found for this site',
            },
            form: {
                siteName: 'Site Name',
                description: 'Description',
                domain: '@:admin.common.grid.domain',
                tenantId: 'Tenant ID',
                defaultLanguage: 'Default Language',
                enabledLanguages: 'Enabled Languages',
                theme: 'Theme',
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
                siteNameRequired: 'Site name is required',
                defaultLanguageRequired: 'Default language is required',
                enabledLanguagesRequired: 'At least one language must be enabled',
            },
            confirm: {
                deleteTitle: 'Delete site',
                deleteMsg:
                    'Are you sure you want to remove this site? This action cannot be undone!',
                deleteLabel: 'Delete',
            },
        },
        users: {
            title: 'User Management',
            searchPlaceholder: 'Search users',
            add: '@:admin.common.actions.add',
            grid: {
                name: '@:admin.common.grid.name',
                role: '@:admin.common.grid.role',
                status: '@:admin.common.grid.status',
                created: '@:admin.common.grid.created',
                details: '@:admin.common.grid.details',
            },
            status: { active: '@:admin.common.status.active', inactive: '@:admin.common.status.inactive' },
            created: { last: 'Last:' },
            form: {
                fullName: 'Full Name',
                email: 'Email',
                password: 'Password',
                role: 'Role',
                preferredLanguage: 'Preferred Language',
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
                changePassword: 'Change Password',
                resetPassword: 'Reset Password',
            },
            messages: {},
            password: {
                management: 'Password Management',
                current: 'Current Password',
                new: 'New Password',
                confirm: 'Confirm Password',
                change: 'Change Password',
                generated: 'New password generated:',
                note:
                    'Please save this password securely. It will not be shown again.',
            },
            validation: {
                fullNameRequired: 'Full name is required',
                emailRequired: 'Email is required',
                emailValid: 'Email must be valid',
                passwordRequired: 'Password is required',
                newPasswordRequired: 'New password is required',
                newPasswordMin: 'Password must be at least 6 characters',
                confirmPasswordRequired: 'Confirm password is required',
                passwordsMismatch: 'Passwords do not match',
            },
            confirm: {
                deleteTitle: 'Delete user',
                deleteMsg:
                    "Are you sure you want to remove this user's password? A new password will be generated.",
                deleteLabel: 'Delete',
                resetTitle: 'Reset password',
                resetMsg:
                    "Are you sure you want to reset this user's password? A new password will be generated.",
                resetLabel: 'Reset',
            },
        },
        tenants: {
            title: 'Tenant Management',
            searchPlaceholder: 'Search tenants',
            add: 'Add Tenant',
            status: {
                ACTIVE: 'Active',
                PENDING: 'Pending',
                SUSPENDED: 'Suspended',
                MAINTENANCE: 'Maintenance'
            },
            grid: {
                subdomain: 'Subdomain',
                company: 'Company',
                status: 'Status',
                language: 'Language',
                created: 'Created',
                details: 'Details',
                empty: 'There are no tenants!'
            },
            actions: {
                activate: 'Activate',
                suspend: 'Suspend',
                maintenance: 'Maintenance',
                reactivate: 'Reactivate',
                endMaintenance: 'End Maintenance',
                delete: 'Delete',
                update: 'Update',
                create: 'Create'
            },
            form: {
                companyName: 'Company Name',
                subdomain: 'Subdomain',
                adminName: 'Admin Name',
                adminEmail: 'Admin Email',
                phone: 'Phone',
                defaultLanguage: 'Default Language',
                customDomain: 'Custom Domain',
                timezone: 'Timezone',
                currency: 'Currency',
                sslEnabled: 'SSL Enabled',
                notes: 'Notes',
                domainSuffix: '.admincraft.com'
            },
            messages: {
                confirmDeleteTitle: 'Delete tenant',
                confirmDeleteMsg:
                    'Are you sure you want to remove this tenant? This action cannot be undone!',
                operationSuccess: 'Operation successful',
                operationError: 'An error occurred, try again!'
            }
        }
    }
};


