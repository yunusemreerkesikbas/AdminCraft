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
import { TranslocoPipe } from '@jsverse/transloco';
import { ModuleProvisionDialogComponent } from '@shared/components/module-provision-dialog/module-provision-dialog.component';
import { ModuleProvisionDialogData } from '@shared/components/module-provision-dialog/module-provision.types';
import { LanguageProvisionDialogComponent } from '@shared/components/language-provision-dialog/language-provision-dialog.component';
import { LanguageProvisionDialogData } from '@shared/components/language-provision-dialog/language-provision.types';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions, ItemDialogSchema } from '@shared/types/item-dialog.types';
import { take } from 'rxjs';
import { TenantsService } from '../tenants.service';
import { CreateTenantRequest, Language, Tenant, UpdateTenantRequest } from '../tenants.types';

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


    createTenant(): void {
        const options: ItemDialogOptions<CreateTenantRequest> = {
            titleKey: 'admin.tenants.title',
            mode: 'create',
            schema: this.#buildTenantDialogSchema(),
            languages: [],
            initial: {
                companyName: '',
                subdomain: '',
                adminName: '',
                adminEmail: '',
                phone: '',
                supportedLanguages: [Language.TR],
                defaultLanguage: Language.TR,
                customDomain: '',
                timezone: 'Europe/Istanbul',
                currency: 'TRY',
                sslEnabled: true,
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
                    adminName: result.adminName || '',
                    adminEmail: result.adminEmail || '',
                    phone: result.phone || undefined,
                    supportedLanguages: (result.supportedLanguages as Language[]) || [Language.TR],
                    defaultLanguage: (result.defaultLanguage as Language) || Language.TR,
                    customDomain: result.customDomain || undefined,
                    timezone: result.timezone || undefined,
                    currency: result.currency || undefined,
                    sslEnabled: result.sslEnabled ?? true,
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
                        },
                        error: () => this.#notify.alert('admin.common.errors.unexpected')
                    });
            });
    }

    editTenant(tenant: Tenant): void {
        const oldSupportedLanguages = tenant.supportedLanguages || [tenant.defaultLanguage];

        const options: ItemDialogOptions<UpdateTenantRequest, number> = {
            titleKey: 'admin.tenants.title',
            mode: 'edit',
            schema: this.#buildTenantDialogSchema(),
            languages: [],
            id: tenant.id,
            initial: {
                companyName: tenant.companyName,
                subdomain: tenant.subdomain,
                adminName: tenant.adminName,
                adminEmail: tenant.adminEmail,
                phone: tenant.phone || '',
                supportedLanguages: oldSupportedLanguages,
                defaultLanguage: tenant.defaultLanguage,
                customDomain: tenant.customDomain || '',
                timezone: tenant.timezone || '',
                currency: tenant.currency || '',
                sslEnabled: tenant.sslEnabled ?? true,
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

    #buildTenantDialogSchema(): ItemDialogSchema {
        const languageOptions = Object.values(Language).map((lang) => ({ value: lang as Language, label: lang }));

        return {
            general: [
                { key: 'companyName', type: 'text', labelKey: 'admin.tenants.fields.companyName', required: true, maxLength: 200 },
                { key: 'subdomain', type: 'text', labelKey: 'admin.tenants.fields.subdomain', required: true, maxLength: 50, placeholder: 'acme' },
                { key: 'adminName', type: 'text', labelKey: 'admin.tenants.fields.adminName', required: true, maxLength: 100 },
                { key: 'adminEmail', type: 'text', labelKey: 'admin.tenants.fields.adminEmail', required: true, maxLength: 150 },
                { key: 'phone', type: 'text', labelKey: 'admin.common.fields.phone', maxLength: 30 },
                { key: 'supportedLanguages', type: 'select', labelKey: 'admin.common.fields.supportedLanguages', required: true, options: languageOptions, multiple: true },
                { key: 'defaultLanguage', type: 'select', labelKey: 'admin.tenants.fields.defaultLanguage', required: true, options: languageOptions },
                { key: 'customDomain', type: 'text', labelKey: 'admin.tenants.fields.customDomain', maxLength: 200, placeholder: 'example.com' },
                { key: 'timezone', type: 'text', labelKey: 'admin.tenants.fields.timezone', maxLength: 100 },
                { key: 'currency', type: 'text', labelKey: 'admin.tenants.fields.currency', maxLength: 10 },
                { key: 'sslEnabled', type: 'checkbox', labelKey: 'admin.tenants.fields.sslEnabled' },
                { key: 'notes', type: 'textarea', labelKey: 'admin.tenants.fields.notes', maxLength: 1000, placeholder: '' }
            ],
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
}