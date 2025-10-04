import {
    AsyncPipe,
    CommonModule,
    DatePipe,
    NgClass
} from '@angular/common';
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy,
    OnInit,
    ViewChild,
    ViewEncapsulation,
    inject,
} from '@angular/core';
import { FormsModule, UntypedFormControl } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { fuseAnimations } from '@fuse/animations';
 
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { SpaSearchInputComponent } from '@shared/components/custom-ui/spa-search-input/spa-search-input.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions, ItemDialogSchema } from '@shared/types/item-dialog.types';
import {
    Observable,
    Subject,
    debounceTime,
    map,
    merge,
    switchMap,
    take,
    takeUntil,
} from 'rxjs';
import { TenantsService } from '../tenants.service';
import { CreateTenantRequest, Language, Tenant, TenantPagination, UpdateTenantRequest } from '../tenants.types';

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
        MatSortModule,
        MatPaginatorModule,
        NgClass,
        AsyncPipe,
        DatePipe,
        TranslocoPipe,
        SpaSearchInputComponent,
    ],
})
export class TenantsListComponent implements OnInit, AfterViewInit, OnDestroy {
    private _transloco = inject(TranslocoService);
    #itemDialog = inject(ItemDialogService);
    #notify = inject(NotificationService);
    @ViewChild(MatPaginator) private _paginator: MatPaginator;
    @ViewChild(MatSort) private _sort: MatSort;

    tenants$: Observable<Tenant[]>;
    pagination$: Observable<TenantPagination>;

    isLoading: boolean = false;
    searchInputControl: UntypedFormControl = new UntypedFormControl();
    

    private _unsubscribeAll: Subject<any> = new Subject<any>();

    constructor(
        private _changeDetectorRef: ChangeDetectorRef,
        private _tenantsService: TenantsService
    ) {}

    ngOnInit(): void {
        this.tenants$ = this._tenantsService.tenants$;
        this.pagination$ = this._tenantsService.pagination$;

        this.searchInputControl.valueChanges
            .pipe(
                takeUntil(this._unsubscribeAll),
                debounceTime(300),
                switchMap((query) => {
                    this.isLoading = true;
                    return this._tenantsService.getTenants(0, 10, 'companyName', 'asc', query);
                }),
                map(() => {
                    this.isLoading = false;
                })
            )
            .subscribe();

        this._tenantsService.getTenants().subscribe();
    }

    ngAfterViewInit(): void {
        if (this._sort && this._paginator) {
            this._sort.sort({
                id: 'companyName',
                start: 'asc',
                disableClear: true,
            });
            this._changeDetectorRef.markForCheck();
            this._sort.sortChange
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe(() => {
                    this._paginator.pageIndex = 0;
                });

            merge(this._sort.sortChange, this._paginator.page)
                .pipe(
                    switchMap(() => {
                        this.isLoading = true;
                        return this._tenantsService.getTenants(
                            this._paginator.pageIndex,
                            this._paginator.pageSize,
                            this._sort.active,
                            this._sort.direction as 'asc' | 'desc',
                            this.searchInputControl.value
                        );
                    }),
                    map(() => {
                        this.isLoading = false;
                    })
                )
                .subscribe();
        }
    }

    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }

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

                this._tenantsService
                    .createTenant(payload)
                    .pipe(take(1))
                    .subscribe({
                        next: () => this.#notify.success('admin.common.messages.operationSuccess'),
                        error: () => this.#notify.alert('admin.common.errors.unexpected')
                    });
            });
    }

    editTenant(tenant: Tenant): void {
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
                supportedLanguages: tenant.supportedLanguages || [tenant.defaultLanguage],
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

                this._tenantsService
                    .updateTenant(tenant.id, payload)
                    .pipe(take(1))
                    .subscribe({
                        next: () => this.#notify.success('admin.common.messages.operationSuccess'),
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
    
    trackByFn(index: number, item: any): any {
        return item.id || index;
    }
}