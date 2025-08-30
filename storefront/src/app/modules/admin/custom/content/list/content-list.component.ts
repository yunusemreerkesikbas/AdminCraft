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
import { fuseAnimations } from '@fuse/animations';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import {
    Observable,
    Subject,
    debounceTime,
    map,
    merge,
    switchMap,
    takeUntil,
} from 'rxjs';
import { ContentService } from '../content.service';
import {
    Content,
    ContentPagination,
    ContentStatus,
    ContentType,
    CreateContentRequest,
    Language,
    UpdateContentRequest,
} from '../content.types';

@Component({
    selector: 'content-list',
    templateUrl: './content-list.component.html',
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
        AsyncPipe,
        DatePipe,
        TranslocoPipe,
    ],
})
export class ContentListComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild(MatPaginator) private _paginator: MatPaginator;
    @ViewChild(MatSort) private _sort: MatSort;

    contents$: Observable<Content[]>;
    pagination$: Observable<ContentPagination>;
    contentTypes$: Observable<ContentType[]>;

    isLoading: boolean = false;
    searchInputControl: UntypedFormControl = new UntypedFormControl();
    selectedContent: Content | null = null;
    selectedContentForm: UntypedFormGroup;
    #notify = inject(NotificationService);
    
    // Language and status options
    languages: Language[] = [Language.TR, Language.EN];
    statuses: ContentStatus[] = [ContentStatus.DRAFT, ContentStatus.PUBLISHED, ContentStatus.ARCHIVED];

    private _unsubscribeAll: Subject<any> = new Subject<any>();

    /**
     * Constructor
     */
    constructor(
        private _changeDetectorRef: ChangeDetectorRef,
        private _fuseConfirmationService: FuseConfirmationService,
        private _formBuilder: UntypedFormBuilder,
        private _contentService: ContentService,
        private _transloco: TranslocoService
    ) {}

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {
        // Create the selected content form
        this.selectedContentForm = this._formBuilder.group({
            title: ['', [Validators.required]],
            slug: ['', [Validators.required, Validators.pattern(/^[a-z0-9-]+$/)]],
            data: [''],
            status: [ContentStatus.DRAFT, [Validators.required]],
            language: [Language.TR, [Validators.required]],
            parentContentId: [''],
            contentTypeId: ['', [Validators.required]],
            metaTitle: [''],
            metaDescription: ['']
        });

        // Get the contents
        this.contents$ = this._contentService.contents$;
        this.pagination$ = this._contentService.pagination$;
        this.contentTypes$ = this._contentService.contentTypes$;

        // Subscribe to search input field value changes
        this.searchInputControl.valueChanges
            .pipe(
                takeUntil(this._unsubscribeAll),
                debounceTime(300),
                switchMap((query) => {
                    this.closeDetails();
                    this.isLoading = true;
                    return this._contentService.getContents(0, 10, 'title', 'asc', query);
                }),
                map(() => {
                    this.isLoading = false;
                })
            )
            .subscribe();

        // Load initial data
        this._contentService.getContents().subscribe();
        this._contentService.getContentTypes().subscribe();
    }

    /**
     * After view init
     */
    ngAfterViewInit(): void {
        if (this._sort && this._paginator) {
            // Set the initial sort
            this._sort.sort({
                id: 'title',
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

            // Get contents if sort or page changes
            merge(this._sort.sortChange, this._paginator.page)
                .pipe(
                    switchMap(() => {
                        this.closeDetails();
                        this.isLoading = true;
                        return this._contentService.getContents(
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
     * Toggle content details
     */
    toggleDetails(contentId: number): void {
        // If the content is already selected...
        if (this.selectedContent && this.selectedContent.id === contentId) {
            // Close the details
            this.closeDetails();
            return;
        }

        // Get the content by id
        this._contentService
            .getContentById(contentId)
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((content) => {
                // Set the selected content
                this.selectedContent = content;

                // Fill the form
                this.selectedContentForm.patchValue(content);

                // Mark for check
                this._changeDetectorRef.markForCheck();
            });
    }

    /**
     * Close the details
     */
    closeDetails(): void {
        this.selectedContent = null;
    }

    /**
     * Create content
     */
    createContent(): void {
        // Create the content
        this.selectedContent = null;
        this.selectedContentForm.reset();
        this.selectedContentForm.patchValue({
            status: ContentStatus.DRAFT,
            language: Language.TR
        });

        // Mark for check
        this._changeDetectorRef.markForCheck();
    }

    /**
     * Update the selected content using the form data
     */
    updateSelectedContent(): void {
        // Get the content object
        const content = this.selectedContentForm.getRawValue() as CreateContentRequest | UpdateContentRequest;

        // Remove empty values
        Object.keys(content).forEach(key => {
            if (content[key] === '' || content[key] === null) {
                delete content[key];
            }
        });

        // If we have a content ID, update the existing content...
        if (this.selectedContent) {
            this._contentService.updateContent(this.selectedContent.id, content as UpdateContentRequest)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: () => {
                        this.#notify.success('admin.common.messages.operationSuccess');
                    },
                    error: () => {
                        this.#notify.alert('admin.common.errors.unexpected');
                    }
                });
        }
        // Otherwise, create a new content...
        else {
            this._contentService.createContent(content as CreateContentRequest)
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
     * Delete the selected content
     */
    deleteSelectedContent(): void {
        // Open the confirmation dialog
        const confirmation = this._fuseConfirmationService.open({
            title: this._transloco.translate('admin.content.confirm.deleteTitle'),
            message: this._transloco.translate('admin.content.confirm.deleteMsg'),
            actions: {
                confirm: {
                    label: this._transloco.translate('admin.content.confirm.deleteLabel'),
                },
            },
        });

        // Subscribe to the confirmation dialog closed action
        confirmation.afterClosed().subscribe((result) => {
            // If the confirm button pressed...
            if (result === 'confirmed') {
                // Get the content object
                const content = this.selectedContent;

                // Delete the content on the server
                this._contentService.deleteContent(content.id)
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
     * Publish content
     */
    publishContent(): void {
        if (this.selectedContent) {
            this._contentService.publishContent(this.selectedContent.id)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: (updatedContent) => {
                        this.selectedContent = updatedContent;
                        this.selectedContentForm.patchValue(updatedContent);
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
     * Archive content
     */
    archiveContent(): void {
        if (this.selectedContent) {
            this._contentService.archiveContent(this.selectedContent.id)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: (updatedContent) => {
                        this.selectedContent = updatedContent;
                        this.selectedContentForm.patchValue(updatedContent);
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
     * Generate slug from title
     */
    generateSlug(): void {
        const title = this.selectedContentForm.get('title')?.value;
        if (title) {
            const slug = title
                .toLowerCase()
                .replace(/[^a-z0-9\s-]/g, '')
                .replace(/\s+/g, '-')
                .replace(/-+/g, '-')
                .trim();
            this.selectedContentForm.get('slug')?.setValue(slug);
        }
    }

    

    /**
     * Track by function for ngFor loops
     */
    trackByFn(index: number, item: any): any {
        return item.id || index;
    }
}