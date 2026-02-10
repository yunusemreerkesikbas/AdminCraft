import {
    ChangeDetectionStrategy,
    Component,
    TemplateRef,
    ViewChild,
    ViewEncapsulation,
    inject,
    signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { LanguageProvisionDialogComponent } from '@shared/components/language-provision-dialog/language-provision-dialog.component';
import { LanguageProvisionDialogData } from '@shared/components/language-provision-dialog/language-provision.types';
import { ModuleProvisionDialogComponent } from '@shared/components/module-provision-dialog/module-provision-dialog.component';
import { ModuleProvisionDialogData } from '@shared/components/module-provision-dialog/module-provision.types';
import { SpaAdminGridComponent, GridAction, GridColumn } from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal';
import { ModalConfig } from '@shared/components/spa-generic-modal/spa-generic-modal.types';
import { SpaStatusBadgeComponent } from '@shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { SyncJobStatus } from '@shared/types/platform.types';
import { PollingUtils } from '@shared/utils/polling.utils';
import { AdminPageHeaderComponent } from 'app/shared/components/admin-page-header/admin-page-header.component';
import { take } from 'rxjs';
import { TenantsService } from '../tenants.service';
import {
    TenantFormDialogComponent,
    TenantFormDialogData,
    TenantFormDialogResult,
} from '../dialogs/tenant-form-dialog/tenant-form-dialog.component';
import {
    AdminUserResponse,
    CreateTenantRequest,
    Language,
    Tenant,
    UpdateTenantRequest,
} from '../tenants.types';
import { TenantStore } from '../tenant.store';

@Component({
    selector: 'spa-tenants-list',
    templateUrl: './tenants-list.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoPipe,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
        AdminPageHeaderComponent,
        SpaStatusBadgeComponent,
    ],
})
export class TenantsListComponent extends BasePaginatedListComponent<
    Tenant,
    CreateTenantRequest,
    UpdateTenantRequest
> {
    @ViewChild('infoTemplate', { static: true }) infoTemplate!: TemplateRef<any>;

    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected override service = inject(TenantsService);
    protected override store = inject(TenantStore);

    #notify = inject(NotificationService);
    #confirmation = inject(ConfirmationService);
    #dialog = inject(MatDialog);
    #transloco = inject(TranslocoService);
    #router = inject(Router);


    protected readonly columnsSig = signal<GridColumn<Tenant>[]>([]);
    protected readonly actionsSig = signal<GridAction<Tenant>[]>([]);

    protected override onInit(): void {
        this.columnsSig.set([
            {
                key: 'info',
                label: 'admin.tenants.fields.companyName',
                type: 'custom',
                template: this.infoTemplate,
                width: '1fr',
            },
            {
                key: 'status',
                label: 'admin.common.grid.status',
                type: 'status',
                hideOn: 'sm',
                getValue: (tenant) => tenant.status,
                width: '140px',
            },
            {
                key: 'createdAt',
                label: 'admin.common.grid.created',
                type: 'date',
                hideOn: 'md',
                width: '160px',
            },
        ]);
    }

    protected viewTenantDetail(tenant: Tenant): void {
        const lang = this.#transloco.getActiveLang();
        this.#router.navigate(['/', lang, 'tenants', tenant.id]);
    }

    protected createTenant(): void {
        const dialogRef = this.#dialog.open<
            TenantFormDialogComponent,
            TenantFormDialogData
        >(TenantFormDialogComponent, {
            width: '800px',
            maxHeight: '70vh',
            autoFocus: false,
            panelClass: 'spa-compact-dialog',
            data: { mode: 'create' },
        });

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((result: TenantFormDialogResult | undefined) => {
                if (result?.updated) {
                    this.refresh();
                    if (result.tenant) {
                        this.provisionTenant(result.tenant);
                    }
                }
            });
    }

    protected editTenant(tenant: Tenant): void {
        const oldSupportedLanguages: Language[] = tenant.supportedLanguages
            ? tenant.supportedLanguages.map((lang) => lang.code as Language)
            : [tenant.defaultLanguage];
        const dialogRef = this.#dialog.open<
            TenantFormDialogComponent,
            TenantFormDialogData
        >(TenantFormDialogComponent, {
            width: '800px',
            maxHeight: '70vh',
            autoFocus: false,
            panelClass: 'spa-compact-dialog',
            data: { mode: 'edit', tenant },
        });

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((result: TenantFormDialogResult | undefined) => {
                if (result?.updated) {
                    this.refresh();
                    const newLanguages = (result.supportedLanguages || []).filter(
                        (lang) => !oldSupportedLanguages.includes(lang)
                    );
                    if (newLanguages.length > 0) {
                        this.#showProvisioningModal(tenant.id, newLanguages);
                    }
                }
            });
    }

    #showProvisioningModal(tenantId: number, newLanguages: Language[]): void {
        const modalData: LanguageProvisionDialogData = {
            tenantId,
            tenantName: '',
            newLanguages,
        };

        const dialogRef = this.#dialog.open(LanguageProvisionDialogComponent, {
            data: modalData,
            disableClose: true,
            width: '500px',
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe((confirmed: boolean) => {
            if (!confirmed) {
                return;
            }

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
                                totalItems: job.totalItems,
                            } as LanguageProvisionDialogData,
                            disableClose: true,
                            width: '500px',
                        });
                    },
                    error: (err) => {
                        const message =
                            err?.error?.message ||
                            'admin.common.errors.unexpected';
                        this.#notify.alert(message);
                    },
                });
        });
    }

    protected provisionTenant(tenant: Tenant): void {
        const dialogData: ModuleProvisionDialogData = {
            tenantId: tenant.id,
            tenantName: tenant.companyName,
        };

        const dialogRef = this.#dialog.open(ModuleProvisionDialogComponent, {
            data: dialogData,
            width: '800px',
            disableClose: true,
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe((success) => {
            if (success) {
                this.#notify.success('admin.provisioning.success');
                this.refresh();
            }
        });
    }

    protected generateAdminUser(tenant: Tenant): void {
        this.service
            .generateAdminUser(tenant.id)
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
                                content: response.subdomain,
                            },
                            {
                                type: 'copyable-fields',
                                fields: [
                                    {
                                        icon: 'email',
                                        label: this.#transloco.translate('admin.tenants.modal.emailLabel'),
                                        value: response.email,
                                        type: 'text',
                                    },
                                    {
                                        icon: 'key',
                                        label: this.#transloco.translate('admin.tenants.modal.passwordLabel'),
                                        value: response.temporaryPassword,
                                        type: 'password',
                                    },
                                    {
                                        icon: 'link',
                                        label: this.#transloco.translate('admin.tenants.modal.loginUrlLabel'),
                                        value: response.loginUrl,
                                        type: 'link',
                                    },
                                ],
                            },
                            {
                                type: 'alert-box',
                                alertType: 'warning',
                                title: this.#transloco.translate('admin.tenants.modal.importantTitle'),
                                content: this.#transloco.translate('admin.tenants.modal.passwordWarning'),
                            },
                        ],
                    };

                    const dialogRef = this.#dialog.open(SpaGenericModalComponent, {
                        data: modalConfig,
                        disableClose: true,
                        width: '600px',
                    });

                    dialogRef.afterClosed().pipe(take(1)).subscribe(() => {
                        this.refresh();
                    });
                },
                error: (err) => {
                    const message =
                        err.error?.message ||
                        this.#transloco.translate('admin.tenants.errors.adminGenerationFailed');
                    this.#notify.alert(message);
                },
            });
    }

    protected syncMigrations(tenant: Tenant): void {
        if (tenant.status !== 'ACTIVE') {
            this.#notify.warning('admin.tenants.messages.syncOnlyActive');
            return;
        }

        this.#confirmation
            .confirm(
                'admin.tenants.confirm.syncTitle',
                'admin.tenants.confirm.syncMessage',
                'admin.common.actions.sync',
                'warning'
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (!confirmed) {
                    return;
                }

                this.#notify.info('admin.tenants.messages.syncStarted');

                this.service
                    .syncMigrations(tenant.id)
                    .pipe(take(1))
                    .subscribe({
                        next: (job) => {
                            this.#pollSyncJob(job.jobId);
                        },
                        error: (err) => {
                            const message =
                                err.error?.message ||
                                this.#transloco.translate('admin.tenants.messages.syncFailed');
                            this.#notify.alert(message);
                        },
                    });
            });
    }

    #pollSyncJob(jobId: number): void {
        PollingUtils.poll(
            () => this.service.getProvisioningJobById(jobId),
            2000,
            (job) => job.status === SyncJobStatus.SUCCEEDED || job.status === SyncJobStatus.FAILED,
            this.destroy$
        ).subscribe({
            next: (job) => {
                if (job.status === SyncJobStatus.SUCCEEDED) {
                    this.#notify.success('admin.tenants.messages.syncCompleted');
                } else if (job.status === SyncJobStatus.FAILED) {
                    this.#notify.alert(job.error || 'admin.tenants.messages.syncFailed');
                }
            },
            error: (err) => {
                this.#notify.alert('admin.tenants.messages.syncFailed');
            },
        });
    }
}
