import {
    AsyncPipe,
    CurrencyPipe,
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
import {
    MatCheckboxModule
} from '@angular/material/checkbox';
import { MatOptionModule, MatRippleModule } from '@angular/material/core';
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
import { InventoryService } from 'app/modules/admin/apps/ecommerce/inventory/inventory.service';
import {
    ApiResponse,
    CreateTenantRequest,
    Language,
    Tenant,
    TenantPagination,
    TenantResponse,
    TenantStatus,
    UpdateTenantRequest,
} from 'app/modules/admin/apps/ecommerce/inventory/tenant.types';
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
    selector: 'inventory-list',
    templateUrl: './inventory.component.html',
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
        MatSlideToggleModule,
        MatSelectModule,
        MatOptionModule,
        MatCheckboxModule,
        MatRippleModule,
        AsyncPipe,
        CurrencyPipe,
    ],
})
export class InventoryListComponent
    implements OnInit, AfterViewInit, OnDestroy
{
    @ViewChild(MatPaginator) private _paginator: MatPaginator;
    @ViewChild(MatSort) private _sort: MatSort;

    tenants$: Observable<Tenant[]>;

    flashMessage: 'success' | 'error' | null = null;
    isLoading: boolean = false;
    pagination: TenantPagination;
    searchInputControl: UntypedFormControl = new UntypedFormControl();
    selectedTenant: Tenant | null = null;
    selectedTenantForm: UntypedFormGroup;
    currentLanguage: string = 'tr';
    tenantStatuses = Object.values(TenantStatus);
    languages = Object.values(Language);
    private _unsubscribeAll: Subject<any> = new Subject<any>();

    /**
     * Constructor
     */
    constructor(
        private _changeDetectorRef: ChangeDetectorRef,
        private _fuseConfirmationService: FuseConfirmationService,
        private _formBuilder: UntypedFormBuilder,
        private _inventoryService: InventoryService
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
            id: [''],
            subdomain: ['', [Validators.required, Validators.minLength(3)]],
            companyName: ['', [Validators.required]],
            databaseName: [''],
            status: [TenantStatus.PENDING],
            defaultLanguage: [Language.TR, [Validators.required]],
            supportedLanguages: [[Language.TR], [Validators.required]],
            adminEmail: ['', [Validators.required, Validators.email]],
            adminName: ['', [Validators.required]],
            phone: [''],
            customDomain: [''],
            sslEnabled: [false],
            timezone: ['Europe/Istanbul'],
            currency: ['TRY'],
            notes: [''],
        });

        // Get the pagination
        this._inventoryService.tenantPagination$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((pagination: TenantPagination) => {
                // Update the pagination
                this.pagination = pagination;

                // Mark for check
                this._changeDetectorRef.markForCheck();
            });

        // Get the tenants
        this.tenants$ = this._inventoryService.tenants$;

        // Load initial tenants data
        this.loadTenants();

        // Subscribe to search input field value changes
        this.searchInputControl.valueChanges
            .pipe(
                takeUntil(this._unsubscribeAll),
                debounceTime(300),
                switchMap((query) => {
                    this.closeDetails();
                    this.isLoading = true;
                    return this.loadTenants();
                }),
                map(() => {
                    this.isLoading = false;
                })
            )
            .subscribe();
    }

    /**
     * After view init
     */
    ngAfterViewInit(): void {
        if (this._sort && this._paginator) {
            // Set the initial sort
            this._sort.sort({
                id: 'name',
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
                        return this.loadTenants();
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
     * Load tenants
     */
    loadTenants(): Observable<any> {
        return this._inventoryService.getTenants(undefined, this.currentLanguage);
    }

    /**
     * Toggle tenant details
     *
     * @param tenantId
     */
    toggleDetails(tenantId: number): void {
        // If the tenant is already selected...
        if (this.selectedTenant && this.selectedTenant.id === tenantId) {
            // Close the details
            this.closeDetails();
            return;
        }

        // Get the tenant by id
        this._inventoryService
            .getTenantById(tenantId, this.currentLanguage)
            .subscribe((response: ApiResponse<TenantResponse>) => {
                if (response.success) {
                    // Set the selected tenant
                    this.selectedTenant = response.data;

                    // Fill the form
                    this.selectedTenantForm.patchValue(response.data);

                    // Mark for check
                    this._changeDetectorRef.markForCheck();
                }
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
        // Reset form to default values
        this.selectedTenantForm.reset({
            status: TenantStatus.PENDING,
            defaultLanguage: Language.TR,
            supportedLanguages: [Language.TR],
            sslEnabled: false,
            timezone: 'Europe/Istanbul',
            currency: 'TRY',
        });
        
        // Set as new tenant
        this.selectedTenant = null;

        // Mark for check
        this._changeDetectorRef.markForCheck();
    }

    /**
     * Update the selected tenant using the form data
     */
    updateSelectedTenant(): void {
        if (!this.selectedTenant) {
            // Create new tenant
            const tenantData: CreateTenantRequest = this.selectedTenantForm.getRawValue();
            
            this._inventoryService
                .createTenant(tenantData, this.currentLanguage)
                .subscribe((response) => {
                    if (response.success) {
                        this.showFlashMessage('success');
                        this.closeDetails();
                        this.loadTenants().subscribe();
                    } else {
                        this.showFlashMessage('error');
                    }
                });
        } else {
            // Update existing tenant
            const tenantData: UpdateTenantRequest = this.selectedTenantForm.getRawValue();
            
            this._inventoryService
                .updateTenant(this.selectedTenant.id, tenantData, this.currentLanguage)
                .subscribe((response) => {
                    if (response.success) {
                        this.showFlashMessage('success');
                    } else {
                        this.showFlashMessage('error');
                    }
                });
        }
    }

    /**
     * Delete the selected tenant using the form data
     */
    deleteSelectedTenant(): void {
        if (!this.selectedTenant) return;

        // Open the confirmation dialog
        const confirmation = this._fuseConfirmationService.open({
            title: 'Delete tenant',
            message:
                'Are you sure you want to remove this tenant? This action cannot be undone!',
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
                // Delete the tenant on the server
                this._inventoryService
                    .deleteTenant(this.selectedTenant.id, this.currentLanguage)
                    .subscribe((response) => {
                        if (response.success) {
                            // Close the details
                            this.closeDetails();
                            this.showFlashMessage('success');
                        } else {
                            this.showFlashMessage('error');
                        }
                    });
            }
        });
    }

    // Removed activateTenant

    // Removed suspendTenant

    // Removed setMaintenanceMode

    // Removed checkSubdomainAvailability

    /**
     * Switch language
     */
    switchLanguage(language: string): void {
        this.currentLanguage = language;
        this.loadTenants().subscribe();
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
     *
     * @param index
     * @param item
     */
    trackByFn(index: number, item: any): any {
        return item.id || index;
    }
}
