import {
    AsyncPipe,
    CommonModule,
    DatePipe,
    NgClass,
    NgTemplateOutlet,
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
import {
    FormsModule,
    ReactiveFormsModule,
    UntypedFormBuilder,
    UntypedFormControl,
    UntypedFormGroup,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { TenantLanguagesService, TenantLanguagesState } from '@core/language/tenant-languages.service';
import { fuseAnimations } from '@fuse/animations';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSearchInputComponent } from '@shared/components/custom-ui/spa-search-input/spa-search-input.component';
import { SpaSelectComponent, SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaToggleComponent } from '@shared/components/custom-ui/spa-toggle/spa-toggle.component';
import { NotificationService } from '@shared/notifications/notification.service';
import {
    Observable,
    Subject,
    debounceTime,
    map,
    merge,
    of,
    switchMap,
    takeUntil,
} from 'rxjs';
import { TenantsService } from '../tenants.service';
import {
    CreateTenantRequest,
    LANGUAGE_LABELS,
    Language,
    Tenant,
    TenantPagination,
    TenantStatus,
    UpdateTenantRequest,
} from '../tenants.types';

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
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatSortModule,
        NgTemplateOutlet,
        MatPaginatorModule,
        NgClass,
        MatSelectModule,
        MatSlideToggleModule,
        AsyncPipe,
        DatePipe,
        TranslocoPipe,
        SpaInputComponent,
        SpaSelectComponent,
        SpaTextareaComponent,
        SpaToggleComponent,
        SpaSearchInputComponent,
    ],
})
export class TenantsListComponent implements OnInit, AfterViewInit, OnDestroy {
    private _transloco = inject(TranslocoService);
    #notify = inject(NotificationService);
    @ViewChild(MatPaginator) private _paginator: MatPaginator;
    @ViewChild(MatSort) private _sort: MatSort;

    tenants$: Observable<Tenant[]>;
    pagination$: Observable<TenantPagination>;

    isLoading: boolean = false;
    searchInputControl: UntypedFormControl = new UntypedFormControl();
    selectedTenant: Tenant | null = null;
    selectedTenantForm: UntypedFormGroup;
    
    // Language and status options
    languages: Language[] = [Language.TR, Language.EN];
    statuses: TenantStatus[] = [TenantStatus.PENDING, TenantStatus.ACTIVE, TenantStatus.SUSPENDED, TenantStatus.MAINTENANCE];
    languageOptions: SpaSelectOption<Language>[] = [];
    private _allowedLangs: Language[] = [Language.TR, Language.EN];

    private _initialLanguages: TenantLanguagesState | null = null;

    private _unsubscribeAll: Subject<any> = new Subject<any>();

    /**
     * Constructor
     */
    constructor(
        private _changeDetectorRef: ChangeDetectorRef,
        private _fuseConfirmationService: FuseConfirmationService,
        private _formBuilder: UntypedFormBuilder,
        private _tenantsService: TenantsService,
        private _tenantLanguages: TenantLanguagesService
    ) {}

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {
        // Create the selected tenant form
        this.selectedTenantForm = this._formBuilder.group({
            companyName: ['', [Validators.required]],
            subdomain: ['', [Validators.required, Validators.pattern(/^[a-z0-9-]+$/)]],
            adminName: ['', [Validators.required]],
            adminEmail: ['', [Validators.required, Validators.email]],
            phone: [''],
            defaultLanguage: [Language.TR, [Validators.required]],
            supportedLanguages: [[Language.TR], [Validators.required]],
            customDomain: [''],
            timezone: ['Europe/Istanbul'],
            currency: ['TRY'],
            sslEnabled: [true],
            notes: ['']
        });

        // Load language catalog from backend and build options
        this._tenantLanguages
            .loadCatalog()
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((list) => {
                this._allowedLangs = list && list.length ? list : [Language.TR, Language.EN];
                this.languageOptions = this._allowedLangs.map((l) => ({ value: l, label: LANGUAGE_LABELS[l] }));
                // Ensure form values respect allowed languages
                const curDef = this.selectedTenantForm.get('defaultLanguage')?.value as Language;
                const curSup: Language[] = (this.selectedTenantForm.get('supportedLanguages')?.value as Language[]) || [];
                const filteredSup = curSup.filter((l) => this._allowedLangs.includes(l));
                const ensured = filteredSup.includes(curDef) ? filteredSup : [...filteredSup, curDef].filter((l) => this._allowedLangs.includes(l));
                this.selectedTenantForm.get('supportedLanguages')?.setValue(this.#uniqueLanguages(ensured));
                if (!this._allowedLangs.includes(curDef)) {
                    const fallback = this._allowedLangs[0];
                    this.selectedTenantForm.get('defaultLanguage')?.setValue(fallback);
                }
                this._changeDetectorRef.markForCheck();
            });

        // Get the tenants
        this.tenants$ = this._tenantsService.tenants$;
        this.pagination$ = this._tenantsService.pagination$;

        // Subscribe to search input field value changes
        this.searchInputControl.valueChanges
            .pipe(
                takeUntil(this._unsubscribeAll),
                debounceTime(300),
                switchMap((query) => {
                    this.closeDetails();
                    this.isLoading = true;
                    return this._tenantsService.getTenants(0, 10, 'companyName', 'asc', query);
                }),
                map(() => {
                    this.isLoading = false;
                })
            )
            .subscribe();

        // Load initial data
        this._tenantsService.getTenants().subscribe();
    }

    /**
     * After view init
     */
    ngAfterViewInit(): void {
        if (this._sort && this._paginator) {
            // Set the initial sort
            this._sort.sort({
                id: 'companyName',
                start: 'asc',
                disableClear: true,
            });

            // Mark for check
            this._changeDetectorRef.markForCheck();

            // If the user changes the sort order...
            this._sort.sortChange
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe(() => {
                    // Reset back to the first page
                    this._paginator.pageIndex = 0;

                    // Close the details
                    this.closeDetails();
                });

            // Get tenants if sort or page changes
            merge(this._sort.sortChange, this._paginator.page)
                .pipe(
                    switchMap(() => {
                        this.closeDetails();
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

    /**
     * On destroy
     */
    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Toggle tenant details
     */
    toggleDetails(tenantId: number): void {
        // If the tenant is already selected...
        if (this.selectedTenant && this.selectedTenant.id === tenantId) {
            // Close the details
            this.closeDetails();
            return;
        }

        // Get the tenant by id
        this._tenantsService
            .getTenantById(tenantId)
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((tenant) => {
                // Set the selected tenant
                this.selectedTenant = tenant;

                // Fill the form
                this.selectedTenantForm.patchValue({
                    ...tenant,
                    supportedLanguages: tenant.supportedLanguages?.length
                        ? tenant.supportedLanguages
                        : [tenant.defaultLanguage]
                });

                // Load authoritative tenant languages via service (header uses tenant id)
                if (tenant?.id) {
                    this._tenantLanguages
                        .loadTenantLanguages()
                        .pipe(takeUntil(this._unsubscribeAll))
                        .subscribe((state) => {
                            this._initialLanguages = state;
                            this.selectedTenantForm
                                .get('defaultLanguage')
                                ?.setValue(state.defaultLanguage);
                            this.selectedTenantForm
                                .get('supportedLanguages')
                                ?.setValue(state.supported);
                            this._changeDetectorRef.markForCheck();
                        });
                }

                // Mark for check
                this._changeDetectorRef.markForCheck();
            });
    }

    /**
     * Close the details
     */
    closeDetails(): void {
        this.selectedTenant = null;
    }

    /**
     * Create tenant
     */
    createTenant(): void {
        // Create the tenant
        this.selectedTenant = null;
        this.selectedTenantForm.reset();
        this.selectedTenantForm.patchValue({
            defaultLanguage: Language.TR,
            supportedLanguages: [Language.TR],
            timezone: 'Europe/Istanbul',
            currency: 'TRY',
            sslEnabled: true
        });

        // Mark for check
        this._changeDetectorRef.markForCheck();
    }

    /**
     * Update the selected tenant using the form data
     */
    updateSelectedTenant(): void {
        // Get the tenant object
        const tenant = this.selectedTenantForm.getRawValue() as CreateTenantRequest | UpdateTenantRequest;

        // Remove empty values
        Object.keys(tenant).forEach(key => {
            if (tenant[key] === '' || tenant[key] === null) {
                delete tenant[key];
            }
        });

        // If we have a tenant ID, update the existing tenant...
        if (this.selectedTenant) {
            const currentDefault = this.selectedTenantForm.get('defaultLanguage')?.value as Language;
            const currentSupported = (this.selectedTenantForm.get('supportedLanguages')?.value as Language[]) || [];

            // Ensure default is in supported set
            const ensuredSupported = currentSupported.includes(currentDefault)
                ? currentSupported
                : [...currentSupported, currentDefault];

            const filteredSupported = this.#uniqueLanguages(
                ensuredSupported.filter((l) => this._allowedLangs.includes(l))
            );

            const needsLangUpdate = this._initialLanguages
                ? (this._initialLanguages.defaultLanguage !== currentDefault) ||
                  (this.#diffLanguages(this._initialLanguages.supported, filteredSupported))
                : true;

            this._tenantsService.updateTenant(this.selectedTenant.id, tenant as UpdateTenantRequest)
                .pipe(
                    switchMap(() => needsLangUpdate
                        ? this._tenantLanguages.updateTenantLanguages({
                              defaultLanguage: currentDefault,
                              supported: filteredSupported,
                          })
                        : of(null)
                    ),
                    takeUntil(this._unsubscribeAll)
                )
                .subscribe({
                    next: () => {
                        this.#notify.success('admin.common.messages.operationSuccess');
                        this._initialLanguages = {
                            defaultLanguage: currentDefault,
                            supported: filteredSupported,
                        };
                    },
                    error: () => {
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
        // Otherwise, create a new tenant...
        else {
            this._tenantsService.createTenant(tenant as CreateTenantRequest)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: () => {
                        this.#notify.success('admin.common.messages.operationSuccess');
                        // Close details
                        this.closeDetails();
                    },
                    error: () => {
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
    }

    /**
     * Delete the selected tenant
     */
    deleteSelectedTenant(): void {
        // Open the confirmation dialog
        const confirmation = this._fuseConfirmationService.open({
            title: this._transloco.translate(
                'admin.tenants.messages.confirmDeleteTitle'
            ),
            message: this._transloco.translate(
                'admin.tenants.messages.confirmDeleteMsg'
            ),
            actions: {
                confirm: {
                    label: this._transloco.translate(
                        'admin.tenants.actions.delete'
                    ),
                },
            },
        });

        // Subscribe to the confirmation dialog closed action
        confirmation.afterClosed().subscribe((result) => {
            // If the confirm button pressed...
            if (result === 'confirmed') {
                // Get the tenant object
                const tenant = this.selectedTenant;

                // Delete the tenant on the server
                this._tenantsService.deleteTenant(tenant.id)
                    .pipe(takeUntil(this._unsubscribeAll))
                    .subscribe({
                        next: () => {
                            // Close the details
                            this.closeDetails();
                            this.#notify.success('admin.common.messages.operationSuccess');
                        },
                        error: () => {
                            this.#notify.alert('admin.common.errors.unexpected');
                        }
                    });
            }
        });
    }

    /**
     * Activate tenant
     */
    activateTenant(): void {
        if (this.selectedTenant) {
            this._tenantsService.activateTenant(this.selectedTenant.id)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: (updatedTenant) => {
                        this.selectedTenant = updatedTenant;
                        this.selectedTenantForm.patchValue(updatedTenant);
                        this.#notify.success('admin.common.messages.operationSuccess');
                        this._changeDetectorRef.markForCheck();
                    },
                    error: () => {
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
    }

    /**
     * Suspend tenant
     */
    suspendTenant(): void {
        if (this.selectedTenant) {
            this._tenantsService.suspendTenant(this.selectedTenant.id)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: (updatedTenant) => {
                        this.selectedTenant = updatedTenant;
                        this.selectedTenantForm.patchValue(updatedTenant);
                        this.#notify.success('admin.common.messages.operationSuccess');
                        this._changeDetectorRef.markForCheck();
                    },
                    error: () => {
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
    }

    /**
     * Set maintenance mode
     */
    setMaintenanceMode(): void {
        if (this.selectedTenant) {
            this._tenantsService.setMaintenanceMode(this.selectedTenant.id)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: (updatedTenant) => {
                        this.selectedTenant = updatedTenant;
                        this.selectedTenantForm.patchValue(updatedTenant);
                        this.#notify.success('admin.common.messages.operationSuccess');
                        this._changeDetectorRef.markForCheck();
                    },
                    error: () => {
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
    }

    /**
     * Check subdomain availability
     */
    checkSubdomainAvailability(): void {
        const subdomain = this.selectedTenantForm.get('subdomain')?.value;
        if (subdomain && subdomain.length > 2) {
            // If we're editing an existing tenant and the subdomain hasn't changed, don't check
            if (this.selectedTenant && this.selectedTenant.subdomain === subdomain) {
                // Clear any existing errors for the current tenant's subdomain
                const control = this.selectedTenantForm.get('subdomain');
                if (control?.hasError('unavailable')) {
                    const errors = { ...control.errors };
                    delete errors.unavailable;
                    control.setErrors(Object.keys(errors).length > 0 ? errors : null);
                }
                return;
            }

            this._tenantsService.checkSubdomainAvailability(subdomain)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe((available) => {
                    if (!available) {
                        this.selectedTenantForm.get('subdomain')?.setErrors({ unavailable: true });
                    }
                });
        }
    }

    onDefaultLanguageChange(lang: Language): void {
        const supportedCtrl = this.selectedTenantForm.get('supportedLanguages');
        const list: Language[] = (supportedCtrl?.value as Language[]) || [];
        if (!list.includes(lang)) {
            supportedCtrl?.setValue([...list, lang]);
        }
    }

    onSupportedLanguagesChange(list: Language[]): void {
        const def = this.selectedTenantForm.get('defaultLanguage')?.value as Language;
        const filtered = (list || []).filter((l) => this._allowedLangs.includes(l));
        const ensured = filtered.includes(def) ? filtered : [...filtered, def];
        this.selectedTenantForm.get('supportedLanguages')?.setValue(this.#uniqueLanguages(ensured));
    }

    #uniqueLanguages(list: Language[]): Language[] {
        return Array.from(new Set(list));
    }

    #diffLanguages(a: Language[], b: Language[]): boolean {
        const sa = new Set(a);
        const sb = new Set(b);
        if (sa.size !== sb.size) return true;
        for (const v of sa) if (!sb.has(v)) return true;
        return false;
    }

    /**
     * Track by function for ngFor loops
     */
    trackByFn(index: number, item: any): any {
        return item.id || index;
    }
}