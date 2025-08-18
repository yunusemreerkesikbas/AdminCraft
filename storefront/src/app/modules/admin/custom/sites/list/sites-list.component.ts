import {
    AsyncPipe,
    NgClass,
    NgTemplateOutlet,
    DatePipe,
    CommonModule,
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
import { MatChipsModule } from '@angular/material/chips';
import { fuseAnimations } from '@fuse/animations';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { SitesService } from '../sites.service';
import {
    Site,
    SitePagination,
    Language,
    CreateSiteRequest,
    UpdateSiteRequest,
    Menu,
} from '../sites.types';
import {
    Observable,
    Subject,
    debounceTime,
    map,
    merge,
    switchMap,
    takeUntil,
} from 'rxjs';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'sites-list',
    templateUrl: './sites-list.component.html',
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
    standalone: true,
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
        MatChipsModule,
        AsyncPipe,
        DatePipe,
        TranslocoPipe,
    ],
})
export class SitesListComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild(MatPaginator) private _paginator: MatPaginator;
    @ViewChild(MatSort) private _sort: MatSort;

    sites$: Observable<Site[]>;
    pagination$: Observable<SitePagination>;
    menus$: Observable<Menu[]>;

    isLoading: boolean = false;
    searchInputControl: UntypedFormControl = new UntypedFormControl();
    selectedSite: Site | null = null;
    selectedSiteForm: UntypedFormGroup;
    flashMessage: 'success' | 'error' | null = null;
    
    // Language options
    languages: Language[] = [Language.TR, Language.EN];
    availableThemes: string[] = ['default', 'modern', 'classic', 'minimal'];

    private _unsubscribeAll: Subject<any> = new Subject<any>();

    /**
     * Constructor
     */
    constructor(
        private _changeDetectorRef: ChangeDetectorRef,
        private _fuseConfirmationService: FuseConfirmationService,
        private _formBuilder: UntypedFormBuilder,
        private _sitesService: SitesService,
        private _transloco: TranslocoService
    ) {}

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {
        // Create the selected site form
        this.selectedSiteForm = this._formBuilder.group({
            siteName: ['', [Validators.required]],
            description: [''],
            enabledLanguages: [[], [Validators.required]],
            defaultLanguage: [Language.TR, [Validators.required]],
            tenantId: [''],
            domain: [''],
            isActive: [true],
            theme: ['default'],
            logoUrl: [''],
            faviconUrl: ['']
        });

        // Get the sites
        this.sites$ = this._sitesService.sites$;
        this.pagination$ = this._sitesService.pagination$;
        this.menus$ = this._sitesService.menus$;

        // Subscribe to search input field value changes
        this.searchInputControl.valueChanges
            .pipe(
                takeUntil(this._unsubscribeAll),
                debounceTime(300),
                switchMap((query) => {
                    this.closeDetails();
                    this.isLoading = true;
                    return this._sitesService.getSites(0, 10, 'siteName', 'asc', query);
                }),
                map(() => {
                    this.isLoading = false;
                })
            )
            .subscribe();

        // Load initial data
        this._sitesService.getSites().subscribe();
    }

    /**
     * After view init
     */
    ngAfterViewInit(): void {
        if (this._sort && this._paginator) {
            // Set the initial sort
            this._sort.sort({
                id: 'siteName',
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

            // Get sites if sort or page changes
            merge(this._sort.sortChange, this._paginator.page)
                .pipe(
                    switchMap(() => {
                        this.closeDetails();
                        this.isLoading = true;
                        return this._sitesService.getSites(
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
     * Toggle site details
     */
    toggleDetails(siteId: number): void {
        // If the site is already selected...
        if (this.selectedSite && this.selectedSite.id === siteId) {
            // Close the details
            this.closeDetails();
            return;
        }

        // Get the site by id
        this._sitesService
            .getSiteById(siteId)
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((site) => {
                // Set the selected site
                this.selectedSite = site;

                // Fill the form
                this.selectedSiteForm.patchValue(site);

                // Load menus for this site
                this._sitesService.getMenusBySite(siteId).subscribe();

                // Mark for check
                this._changeDetectorRef.markForCheck();
            });
    }

    /**
     * Close the details
     */
    closeDetails(): void {
        this.selectedSite = null;
    }

    /**
     * Create site
     */
    createSite(): void {
        // Create the site
        this.selectedSite = null;
        this.selectedSiteForm.reset();
        this.selectedSiteForm.patchValue({
            defaultLanguage: Language.TR,
            enabledLanguages: [Language.TR],
            isActive: true,
            theme: 'default'
        });

        // Mark for check
        this._changeDetectorRef.markForCheck();
    }

    /**
     * Update the selected site using the form data
     */
    updateSelectedSite(): void {
        // Get the site object
        const site = this.selectedSiteForm.getRawValue() as CreateSiteRequest | UpdateSiteRequest;

        // Remove empty values
        Object.keys(site).forEach(key => {
            if (site[key] === '' || site[key] === null) {
                delete site[key];
            }
        });

        // If we have a site ID, update the existing site...
        if (this.selectedSite) {
            this._sitesService.updateSite(this.selectedSite.id, site as UpdateSiteRequest)
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
        // Otherwise, create a new site...
        else {
            this._sitesService.createSite(site as CreateSiteRequest)
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
     * Delete the selected site
     */
    deleteSelectedSite(): void {
        // Open the confirmation dialog
        const confirmation = this._fuseConfirmationService.open({
            title: this._transloco.translate('admin.sites.confirm.deleteTitle'),
            message: this._transloco.translate('admin.sites.confirm.deleteMsg'),
            actions: {
                confirm: {
                    label: this._transloco.translate('admin.sites.confirm.deleteLabel'),
                },
            },
        });

        // Subscribe to the confirmation dialog closed action
        confirmation.afterClosed().subscribe((result) => {
            // If the confirm button pressed...
            if (result === 'confirmed') {
                // Get the site object
                const site = this.selectedSite;

                // Delete the site on the server
                this._sitesService.deleteSite(site.id)
                    .pipe(takeUntil(this._unsubscribeAll))
                    .subscribe(() => {
                        // Close the details
                        this.closeDetails();
                    });
            }
        });
    }

    /**
     * Publish site
     */
    publishSite(): void {
        if (this.selectedSite) {
            this._sitesService.publishSite(this.selectedSite.id)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: (updatedSite) => {
                        this.selectedSite = updatedSite;
                        this.selectedSiteForm.patchValue(updatedSite);
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
     * Activate site
     */
    activateSite(): void {
        if (this.selectedSite) {
            this._sitesService.activateSite(this.selectedSite.id)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: (updatedSite) => {
                        this.selectedSite = updatedSite;
                        this.selectedSiteForm.patchValue(updatedSite);
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
     * Deactivate site
     */
    deactivateSite(): void {
        if (this.selectedSite) {
            this._sitesService.deactivateSite(this.selectedSite.id)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: (updatedSite) => {
                        this.selectedSite = updatedSite;
                        this.selectedSiteForm.patchValue(updatedSite);
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