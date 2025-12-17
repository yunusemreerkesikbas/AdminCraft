import { CommonModule, DatePipe, NgClass } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    ViewEncapsulation,
    inject,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { fuseAnimations } from '@fuse/animations';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { LanguageProvisionDialogComponent } from '@shared/components/language-provision-dialog/language-provision-dialog.component';
import { LanguageProvisionDialogData } from '@shared/components/language-provision-dialog/language-provision.types';
import { ModuleProvisionDialogComponent } from '@shared/components/module-provision-dialog/module-provision-dialog.component';
import { ModuleProvisionDialogData } from '@shared/components/module-provision-dialog/module-provision.types';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal';
import { ModalConfig } from '@shared/components/spa-generic-modal/spa-generic-modal.types';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions, ItemDialogSchema } from '@shared/types/item-dialog.types';
import { take } from 'rxjs';
import { TenantsService } from '../tenants.service';
import { AdminUserResponse, CreateTenantRequest, Language, Tenant, UpdateTenantRequest } from '../tenants.types';

@Component({
    selector: 'tenants-list',
    templateUrl: './tenants-list.component.html',
    styles: [
        /* language=SCSS */
        `
            .inventory-grid {
                grid-template-columns: 48px auto 40px;

                @screen sm {
                    grid-template-columns: 48px auto 112px 72px;
                }

                @screen md {
                    grid-template-columns: 48px 112px auto 112px 72px;
                }

                @screen lg {
                    grid-template-columns: 48px 112px auto 112px 96px 96px 72px;
                }
            }
        `,
    ],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        CommonModule,
        MatProgressBarModule,
        MatIconModule,
        FormsModule,
        MatButtonModule,
        MatTooltipModule,
        NgClass,
        DatePipe,
        TranslocoPipe,
    ],
})
export class TenantsListComponent extends BaseCrudListComponent<Tenant, CreateTenantRequest, UpdateTenantRequest> {
    protected override service = inject(TenantsService);
    protected override store = new CrudStore<Tenant>();

    #itemDialog = inject(ItemDialogService);
    #notify = inject(NotificationService);
    #dialog = inject(MatDialog);
    #transloco = inject(TranslocoService);


    createTenant(): void {
        const options: ItemDialogOptions<CreateTenantRequest> = {
            titleKey: 'admin.tenants.title',
            mode: 'create',
            schema: this.#buildTenantDialogSchema(),
            languages: [],
            initial: {
                companyName: '',
                subdomain: '',
                supportedLanguages: [Language.TR],
                defaultLanguage: Language.TR,
                customDomain: '',
                notes: ''
            },
            modalData: { disableClose: true, width: '720px' }
        };

        this.#itemDialog
            .open<CreateTenantRequest>(options)
            .pipe(take(1))
            .subscribe((result) => {
                if (!result) return;

                const payload: CreateTenantRequest = {
                    companyName: result.companyName || '',
                    subdomain: result.subdomain || '',
                    supportedLanguages: (result.supportedLanguages as Language[]) || [Language.TR],
                    defaultLanguage: (result.defaultLanguage as Language) || Language.TR,
                    customDomain: result.customDomain || undefined,
                    notes: result.notes || undefined
                };

                if (!payload.supportedLanguages.includes(payload.defaultLanguage)) {
                    this.#notify.warning('admin.tenants.validation.defaultLanguageInSupported');
                    return;
                }

                this.service
                    .create(payload)
                    .pipe(take(1))
                    .subscribe({
                        next: (tenant) => {
                            this.store.addItem(tenant);
                            this.#notify.success('admin.common.messages.operationSuccess');
                            
                            this.provisionTenant(tenant);
                        },
                        error: () => this.#notify.alert('admin.common.errors.unexpected')
                    });
            });
    }

    editTenant(tenant: Tenant): void {
        const oldSupportedLanguages: Language[] = tenant.supportedLanguages 
            ? tenant.supportedLanguages.map(lang => lang.code as Language)
            : [tenant.defaultLanguage];

        const options: ItemDialogOptions<UpdateTenantRequest, number> = {
            titleKey: 'admin.tenants.title',
            mode: 'edit',
            schema: this.#buildTenantDialogSchema(true),
            languages: [],
            id: tenant.id,
            initial: {
                companyName: tenant.companyName,
                supportedLanguages: oldSupportedLanguages,
                defaultLanguage: tenant.defaultLanguage,
                customDomain: tenant.customDomain || '',
                notes: tenant.notes || ''
            },
            modalData: { disableClose: true, width: '720px' }
        };

        this.#itemDialog
            .open<UpdateTenantRequest, number>(options)
            .pipe(take(1))
            .subscribe((result) => {
                if (!result) return;

                const payload: UpdateTenantRequest = { ...result } as UpdateTenantRequest;

                if (payload.supportedLanguages && payload.defaultLanguage && !payload.supportedLanguages.includes(payload.defaultLanguage)) {
                    this.#notify.warning('admin.tenants.validation.defaultLanguageInSupported');
                    return;
                }

                Object.keys(payload).forEach((k) => (payload as any)[k] === '' && delete (payload as any)[k]);

                const newLanguages = (payload.supportedLanguages || []).filter(
                    lang => !oldSupportedLanguages.includes(lang)
                );

                this.service
                    .update(tenant.id, payload)
                    .pipe(take(1))
                    .subscribe({
                        next: (updatedTenant) => {
                            this.store.updateItem(tenant.id, updatedTenant);
                            this.#notify.success('admin.common.messages.operationSuccess');

                            if (newLanguages.length > 0) {
                                this.#showProvisioningModal(tenant.id, newLanguages);
                            }
                        },
                        error: () => this.#notify.alert('admin.common.errors.unexpected')
                    });
            });
    }

    #showProvisioningModal(tenantId: number, newLanguages: Language[]): void {
        const modalData: LanguageProvisionDialogData = {
            tenantId,
            tenantName: '',
            newLanguages
        };

        const dialogRef = this.#dialog.open(LanguageProvisionDialogComponent, {
            data: modalData,
            disableClose: true,
            width: '500px'
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe((confirmed: boolean) => {
            if (!confirmed) return;

            this.service
                .provisionLanguages(tenantId, { languages: newLanguages })
                .pipe(take(1))
                .subscribe({
                    next: (job) => {
                        this.#dialog.open(LanguageProvisionDialogComponent, {
                            data: {
                                tenantId,
                                tenantName: '',
                                newLanguages,
                                jobUuid: job.uuid,
                                status: job.status,
                                processedItems: job.processedItems,
                                totalItems: job.totalItems
                            } as LanguageProvisionDialogData,
                            disableClose: true,
                            width: '500px'
                        });
                    },
                    error: () => this.#notify.alert('admin.common.errors.unexpected')
                });
        });
    }

    #buildTenantDialogSchema(isEdit: boolean = false): ItemDialogSchema {
        const languageOptions = Object.values(Language).map((lang) => ({ value: lang as Language, label: lang }));

        const baseFields = [
            { key: 'companyName', type: 'text' as const, labelKey: 'admin.tenants.fields.companyName', required: true, maxLength: 200, placeholder: 'Acme Inc.' },
        ];

        const subdomainField = !isEdit ? [
            { key: 'subdomain', type: 'text' as const, labelKey: 'admin.tenants.fields.subdomain', required: true, maxLength: 50, placeholder: 'acme' }
        ] : [];

        const commonFields = [
            { key: 'supportedLanguages', type: 'select' as const, labelKey: 'admin.common.fields.supportedLanguages', required: true, options: languageOptions, multiple: true },
            { key: 'defaultLanguage', type: 'select' as const, labelKey: 'admin.tenants.fields.defaultLanguage', required: true, options: languageOptions },
            { key: 'customDomain', type: 'text' as const, labelKey: 'admin.tenants.fields.customDomain', maxLength: 200, placeholder: 'example.com' },
            { key: 'notes', type: 'textarea' as const, labelKey: 'admin.tenants.fields.notes', maxLength: 1000, placeholder: '' }
        ];

        return {
            general: [...baseFields, ...subdomainField, ...commonFields],
            i18n: []
        };
    }

    provisionTenant(tenant: Tenant): void {
        const dialogData: ModuleProvisionDialogData = {
            tenantId: tenant.id,
            tenantName: tenant.companyName
        };

        const dialogRef = this.#dialog.open(ModuleProvisionDialogComponent, {
            data: dialogData,
            width: '800px',
            disableClose: true
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe((success) => {
            if (success) {
                this.#notify.success('admin.provisioning.success');
                this.refresh();
            }
        });
    }

    generateAdminUser(tenant: Tenant): void {
        this.service.generateAdminUser(tenant.id)
            .pipe(take(1))
            .subscribe({
                next: (response) => {
                    const modalConfig: ModalConfig<AdminUserResponse> = {
                        type: 'success',
                        variant: 'credentials',
                        title: this.#transloco.translate('admin.tenants.modal.adminCreated'),
                        icon: 'celebration',
                        data: response,
                        disableClose: true,
                        sections: [
                            {
                                type: 'info-box',
                                title: this.#transloco.translate('admin.tenants.modal.tenantLabel'),
                                content: response.subdomain
                            },
                            {
                                type: 'copyable-fields',
                                fields: [
                                    {
                                        icon: 'email',
                                        label: this.#transloco.translate('admin.tenants.modal.emailLabel'),
                                        value: response.email,
                                        type: 'text'
                                    },
                                    {
                                        icon: 'key',
                                        label: this.#transloco.translate('admin.tenants.modal.passwordLabel'),
                                        value: response.temporaryPassword,
                                        type: 'password'
                                    },
                                    {
                                        icon: 'link',
                                        label: this.#transloco.translate('admin.tenants.modal.loginUrlLabel'),
                                        value: response.loginUrl,
                                        type: 'link'
                                    }
                                ]
                            },
                            {
                                type: 'alert-box',
                                alertType: 'warning',
                                title: this.#transloco.translate('admin.tenants.modal.importantTitle'),
                                content: this.#transloco.translate('admin.tenants.modal.passwordWarning')
                            }
                        ]
                    };

                    const dialogRef = this.#dialog.open(SpaGenericModalComponent, {
                        data: modalConfig,
                        disableClose: true,
                        width: '600px'
                    });

                    dialogRef.afterClosed().pipe(take(1)).subscribe(() => {
                        this.refresh();
                    });
                },
                error: (err) => {
                    const message = err.error?.message || this.#transloco.translate('admin.tenants.errors.adminGenerationFailed');
                    this.#notify.alert(message);
                }
            });
    }

    syncMigrations(tenant: Tenant): void {
        if (tenant.status !== 'ACTIVE') {
            this.#notify.warning('admin.tenants.messages.syncOnlyActive');
            return;
        }

        this.#notify.info('admin.tenants.messages.syncStarted');

        this.service.syncMigrations(tenant.id)
            .pipe(take(1))
            .subscribe({
                next: (job) => {
                    this.#pollSyncJob(job.jobId);
                },
                error: (err) => {
                    const message = err.error?.message || this.#transloco.translate('admin.tenants.messages.syncFailed');
                    this.#notify.alert(message);
                }
            });
    }

    #pollSyncJob(jobId: number): void {
        const pollInterval = setInterval(() => {
            this.service.getProvisioningJobById(jobId)
                .pipe(take(1))
                .subscribe({
                    next: (job) => {
                        if (job.status === 'succeeded') {
                            clearInterval(pollInterval);
                            this.#notify.success('admin.tenants.messages.syncSuccess');
                        } else if (job.status === 'failed') {
                            clearInterval(pollInterval);
                            this.#notify.alert(job.error || 'admin.tenants.messages.syncFailed');
                        }
                    },
                    error: () => {
                        clearInterval(pollInterval);
                        this.#notify.alert('admin.tenants.messages.syncFailed');
                    }
                });
        }, 2000);
    }
}