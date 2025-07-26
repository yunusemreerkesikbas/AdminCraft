import {
    AsyncPipe,
    NgClass,
    NgTemplateOutlet,
    DatePipe,
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
import { fuseAnimations } from '@fuse/animations';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { TenantsService } from '../tenants.service';
import {
    Tenant,
    TenantPagination,
    TenantStatus,
    Language,
    CreateTenantRequest,
    UpdateTenantRequest,
} from '../tenants.types';
import {
    Observable,
    Subject,
    debounceTime,
    map,
    merge,
    switchMap,
    takeUntil,
} from 'rxjs';

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
    ],
})
export class TenantsListComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild(MatPaginator) private _paginator: MatPaginator;
    @ViewChild(MatSort) private _sort: MatSort;

    tenants$: Observable<Tenant[]>;
    pagination$: Observable<TenantPagination>;

    isLoading: boolean = false;
    searchInputControl: UntypedFormControl = new UntypedFormControl();
    selectedTenant: Tenant | null = null;
    selectedTenantForm: UntypedFormGroup;
    flashMessage: 'success' | 'error' | null = null;
    
    // Language and status options
    languages: Language[] = [Language.TR, Language.EN];
    statuses: TenantStatus[] = [TenantStatus.PENDING, TenantStatus.ACTIVE, TenantStatus.SUSPENDED, TenantStatus.MAINTENANCE];

    private _unsubscribeAll: Subject<any> = new Subject<any>();

    /**
     * Constructor
     */
    constructor(
        private _changeDetectorRef: ChangeDetectorRef,
        private _fuseConfirmationService: FuseConfirmationService,
        private _formBuilder: UntypedFormBuilder,
        private _tenantsService: TenantsService
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
            customDomain: [''],
            timezone: ['Europe/Istanbul'],
            currency: ['TRY'],
            sslEnabled: [true],
            notes: ['']
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
                this.selectedTenantForm.patchValue(tenant);

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
            this._tenantsService.updateTenant(this.selectedTenant.id, tenant as UpdateTenantRequest)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: () => {
                        // Show a success message
                        this.showFlashMessage('success');
                    },
                    error: () => {
                        // Show an error message
                        this.showFlashMessage('error');
                    }
                });
        }
        // Otherwise, create a new tenant...
        else {
            this._tenantsService.createTenant(tenant as CreateTenantRequest)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: () => {
                        // Show a success message
                        this.showFlashMessage('success');
                        
                        // Close details
                        this.closeDetails();
                    },
                    error: () => {
                        // Show an error message
                        this.showFlashMessage('error');
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
            title: 'Delete tenant',
            message: 'Are you sure you want to remove this tenant? This action cannot be undone!',
            actions: {
                confirm: {
                    label: 'Delete',
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
                    .subscribe(() => {
                        // Close the details
                        this.closeDetails();
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
                        this.showFlashMessage('success');
                        this._changeDetectorRef.markForCheck();
                    },
                    error: () => {
                        this.showFlashMessage('error');
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
                        this.showFlashMessage('success');
                        this._changeDetectorRef.markForCheck();
                    },
                    error: () => {
                        this.showFlashMessage('error');
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
                        this.showFlashMessage('success');
                        this._changeDetectorRef.markForCheck();
                    },
                    error: () => {
                        this.showFlashMessage('error');
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
            this._tenantsService.checkSubdomainAvailability(subdomain)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe((available) => {
                    if (!available) {
                        this.selectedTenantForm.get('subdomain')?.setErrors({ unavailable: true });
                    }
                });
        }
    }

    /**
     * Show flash message
     */
    showFlashMessage(type: 'success' | 'error'): void {
        // Show the message
        this.flashMessage = type;

        // Mark for check
        this._changeDetectorRef.markForCheck();

        // Hide it after 3 seconds
        setTimeout(() => {
            this.flashMessage = null;

            // Mark for check
            this._changeDetectorRef.markForCheck();
        }, 3000);
    }

    /**
     * Track by function for ngFor loops
     */
    trackByFn(index: number, item: any): any {
        return item.id || index;
    }
}