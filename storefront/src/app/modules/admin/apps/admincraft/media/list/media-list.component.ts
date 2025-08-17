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
import { fuseAnimations } from '@fuse/animations';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { MediaService } from '../media.service';
import {
    MediaFile,
    MediaPagination,
    MediaType,
    UploadMediaRequest,
    UpdateMediaRequest,
} from '../media.types';
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
    selector: 'media-list',
    templateUrl: './media-list.component.html',
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
    ],
})
export class MediaListComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild(MatPaginator) private _paginator: MatPaginator;
    @ViewChild(MatSort) private _sort: MatSort;

    mediaFiles$: Observable<MediaFile[]>;
    pagination$: Observable<MediaPagination>;

    isLoading: boolean = false;
    searchInputControl: UntypedFormControl = new UntypedFormControl();
    selectedMediaFile: MediaFile | null = null;
    selectedMediaForm: UntypedFormGroup;
    uploadForm: UntypedFormGroup;
    flashMessage: 'success' | 'error' | null = null;
    
    // Media type options
    mediaTypes: MediaType[] = [MediaType.IMAGE, MediaType.VIDEO, MediaType.AUDIO, MediaType.DOCUMENT, MediaType.OTHER];
    selectedFile: File | null = null;

    private _unsubscribeAll: Subject<any> = new Subject<any>();

    /**
     * Constructor
     */
    constructor(
        private _changeDetectorRef: ChangeDetectorRef,
        private _fuseConfirmationService: FuseConfirmationService,
        private _formBuilder: UntypedFormBuilder,
        private _mediaService: MediaService
    ) {}

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {
        // Create the selected media form
        this.selectedMediaForm = this._formBuilder.group({
            originalName: ['', [Validators.required]],
            altTextTr: [''],
            altTextEn: ['']
        });

        // Create the upload form
        this.uploadForm = this._formBuilder.group({
            file: [null, [Validators.required]],
            altTextTr: [''],
            altTextEn: ['']
        });

        // Get the media files
        this.mediaFiles$ = this._mediaService.mediaFiles$;
        this.pagination$ = this._mediaService.pagination$;

        // Subscribe to search input field value changes
        this.searchInputControl.valueChanges
            .pipe(
                takeUntil(this._unsubscribeAll),
                debounceTime(300),
                switchMap((query) => {
                    this.closeDetails();
                    this.isLoading = true;
                    return this._mediaService.getMediaFiles(0, 10, 'originalName', 'asc', query);
                }),
                map(() => {
                    this.isLoading = false;
                })
            )
            .subscribe();

        // Load initial data
        this._mediaService.getMediaFiles().subscribe();
    }

    /**
     * After view init
     */
    ngAfterViewInit(): void {
        if (this._sort && this._paginator) {
            // Set the initial sort
            this._sort.sort({
                id: 'originalName',
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

            // Get media files if sort or page changes
            merge(this._sort.sortChange, this._paginator.page)
                .pipe(
                    switchMap(() => {
                        this.closeDetails();
                        this.isLoading = true;
                        return this._mediaService.getMediaFiles(
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
     * Toggle media file details
     */
    toggleDetails(mediaFileId: number): void {
        // If the media file is already selected...
        if (this.selectedMediaFile && this.selectedMediaFile.id === mediaFileId) {
            // Close the details
            this.closeDetails();
            return;
        }

        // Get the media file by id
        this._mediaService
            .getMediaFileById(mediaFileId)
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((mediaFile) => {
                // Set the selected media file
                this.selectedMediaFile = mediaFile;

                // Fill the form
                this.selectedMediaForm.patchValue(mediaFile);

                // Mark for check
                this._changeDetectorRef.markForCheck();
            });
    }

    /**
     * Close the details
     */
    closeDetails(): void {
        this.selectedMediaFile = null;
    }

    /**
     * Show upload form
     */
    showUploadForm(): void {
        // Reset upload form
        this.uploadForm.reset();
        this.selectedFile = null;
        this.selectedMediaFile = null;

        // Mark for check
        this._changeDetectorRef.markForCheck();
    }

    /**
     * Handle file selection
     */
    onFileSelected(event: any): void {
        const file = event.target.files[0];
        if (file) {
            this.selectedFile = file;
            this.uploadForm.patchValue({ file: file });
        }
    }

    /**
     * Upload media file
     */
    uploadMediaFile(): void {
        if (this.selectedFile) {
            const uploadRequest: UploadMediaRequest = {
                file: this.selectedFile,
                altTextTr: this.uploadForm.get('altTextTr')?.value || undefined,
                altTextEn: this.uploadForm.get('altTextEn')?.value || undefined
            };

            this._mediaService.uploadMediaFile(uploadRequest)
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe({
                    next: () => {
                        // Show a success message
                        this.showFlashMessage('success');
                        
                        // Reset upload form
                        this.uploadForm.reset();
                        this.selectedFile = null;
                        
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
     * Update the selected media file using the form data
     */
    updateSelectedMediaFile(): void {
        if (this.selectedMediaFile) {
            // Get the media file object
            const media = this.selectedMediaForm.getRawValue() as UpdateMediaRequest;

            // Remove empty values
            Object.keys(media).forEach(key => {
                if (media[key] === '' || media[key] === null) {
                    delete media[key];
                }
            });

            this._mediaService.updateMediaFile(this.selectedMediaFile.id, media)
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
    }

    /**
     * Delete the selected media file
     */
    deleteSelectedMediaFile(): void {
        // Open the confirmation dialog
        const confirmation = this._fuseConfirmationService.open({
            title: 'Delete media file',
            message: 'Are you sure you want to remove this media file? This action cannot be undone!',
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
                // Get the media file object
                const mediaFile = this.selectedMediaFile;

                // Delete the media file on the server
                this._mediaService.deleteMediaFile(mediaFile.id)
                    .pipe(takeUntil(this._unsubscribeAll))
                    .subscribe(() => {
                        // Close the details
                        this.closeDetails();
                    });
            }
        });
    }

    /**
     * Get media type
     */
    getMediaType(mimeType: string): MediaType {
        return this._mediaService.getMediaType(mimeType);
    }

    /**
     * Format file size
     */
    formatFileSize(bytes: number): string {
        return this._mediaService.formatFileSize(bytes);
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