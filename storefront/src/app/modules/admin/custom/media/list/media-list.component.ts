import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    input,
    OnInit,
    output,
    TemplateRef,
    ViewChild,
} from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseSelectablePaginatedListComponent, BulkDeleteRunnerService } from '@core/crud';
import { UserService } from '@core/user/user.service';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import {
    GridAction,
    GridActionEvent,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { VIEWER_ROLE } from '@shared/constants';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { take, takeUntil } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { MediaDetailDialogComponent } from '../dialogs/media-detail-dialog/media-detail-dialog.component';
import { MediaUploadResultDialogComponent } from '../dialogs/media-upload-result-dialog/media-upload-result-dialog.component';
import { MediaUploadDialogComponent } from '../dialogs/media-upload-dialog/media-upload-dialog.component';
import { MediaService } from '../media.service';
import { MediaStore } from '../media.store';
import {
    Media,
    MediaDetailDialogData,
    UpdateMediaRequest,
} from '../media.types';

@Component({
    selector: 'spa-media-list',
    standalone: true,
    imports: [
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatPaginatorModule,
        MatProgressBarModule,
        MatTooltipModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
        MatSelectModule,
        MatCheckboxModule,
    ],
    templateUrl: './media-list.component.html',
    styles: [],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MediaListComponent
    extends BaseSelectablePaginatedListComponent<Media, FormData, UpdateMediaRequest>
    implements OnInit
{
    readonly selectionMode = input(false);
    readonly mediaSelect = output<Media>();

    @ViewChild('previewTemplate', { static: true })
    previewTemplate!: TemplateRef<unknown>;
    @ViewChild('nameTemplate', { static: true })
    nameTemplate!: TemplateRef<unknown>;
    @ViewChild('headerSelectAllTemplate', { static: true })
    headerSelectAllTemplate!: TemplateRef<unknown>;
    @ViewChild('rowCheckboxTemplate', { static: true })
    rowCheckboxTemplate!: TemplateRef<unknown>;

    protected override service = inject(MediaService);
    protected override store = inject(MediaStore);
    #matDialog = inject(MatDialog);
    #notificationService = inject(NotificationService);
    #confirmationService = inject(ConfirmationService);
    #tenantContext = inject(TenantContextService);
    #userService = inject(UserService);
    #bulkDeleteRunner = inject(BulkDeleteRunnerService);

    protected override readonly defaultSort = 'createdAt,desc';
    protected override readonly defaultPageSize = 24;

    protected searchInputControl = new FormControl('');
    protected paginatedItemsSig = computed(() => this.store.items());
    protected readonly selectedIdsSig = this.store.selectedIdsSig;
    protected readonly selectedCountSig = this.store.selectedCountSig;
    protected readonly isViewerSig = computed(() => this.#userService.user()?.role === VIEWER_ROLE);

    protected columns: GridColumn<Media>[] = [];
    protected actions: GridAction<Media>[] = [];

    protected override onInit(): void {
        this.#setupSearchDebounce();
        this.#initGridConfig();
    }

    protected showSelectionUi(): boolean {
        return !this.selectionMode() && !this.isViewerSig();
    }

    #initGridConfig(): void {
        const baseColumns: GridColumn<Media>[] = [
            {
                key: 'preview',
                label: '',
                type: 'custom',
                width: '80px',
                template: this.previewTemplate,
            },
            {
                key: 'name',
                label: 'admin.media.fields.name',
                type: 'custom',
                width: '1fr',
                template: this.nameTemplate,
            },
            {
                key: 'fileType',
                label: 'admin.common.grid.type',
                type: 'text',
                width: '100px',
                hideOn: 'sm',
                getValue: (item) => item.fileType.toUpperCase(),
            },
            {
                key: 'fileSizeFormatted',
                label: 'admin.common.grid.size',
                type: 'text',
                width: '100px',
                hideOn: 'sm',
            },
            {
                key: 'createdAt',
                label: 'admin.common.grid.uploaded',
                type: 'date',
                width: '120px',
                hideOn: 'sm',
                getSecondaryValue: (item) =>
                    item.uploaderName ? `by ${item.uploaderName}` : '',
            },
        ];

        this.columns =
            this.isViewerSig() || this.selectionMode()
                ? baseColumns
                : [
                      {
                          key: 'select',
                          label: '',
                          type: 'custom',
                          width: '52px',
                          cssClass: 'flex items-center justify-center',
                          headerTemplate: this.headerSelectAllTemplate,
                          template: this.rowCheckboxTemplate,
                      },
                      ...baseColumns,
                  ];

        this.actions = [
            {
                icon: 'heroicons_outline:pencil-square',
                label: 'admin.common.actions.edit',
                action: 'edit',
                show: () => !this.selectionMode() && !this.isViewerSig(),
            },
            {
                icon: 'heroicons_outline:trash',
                label: 'admin.common.actions.delete',
                action: 'delete',
                color: 'warn',
                show: () => !this.selectionMode() && !this.isViewerSig(),
            },
            {
                icon: 'heroicons_outline:plus-circle',
                label: 'admin.common.actions.select',
                action: 'select',
                color: 'primary',
                show: () => this.selectionMode(),
            },
        ];
    }

    #setupSearchDebounce(): void {
        this.searchInputControl.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((query) => {
                this.onSearchInput(query || '');
                this.store.setSearchQuery(query || '');
            });
    }

    openUploadDialog(): void {
        if (this.isViewerSig()) {
            return;
        }
        this.#matDialog
            .open(MediaUploadDialogComponent, {
                width: '700px',
                maxHeight: '90vh',
                disableClose: true,
                data: {
                    languages:
                        this.#tenantContext
                            .tenant()
                            ?.supportedLanguages?.map((l) => l.code) || ['EN'],
                },
            })
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result && result.length > 0) {
                    this.loadItems();
                    this.#matDialog.open(MediaUploadResultDialogComponent, {
                        width: '720px',
                        maxHeight: '90vh',
                        disableClose: false,
                        data: {
                            media: result,
                        },
                    });
                }
            });
    }

    openDetailDialog(media: Media): void {
        if (this.selectionMode()) {
            this.selectMedia(media);
            return;
        }

        this.#matDialog
            .open(MediaDetailDialogComponent, {
                width: '1100px',
                data: {
                    mode: 'edit',
                    media,
                } as MediaDetailDialogData,
            })
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.loadItems();
                }
            });
    }

    selectMedia(media: Media): void {
        this.mediaSelect.emit(media);
    }

    protected override canSelect(_item: Media): boolean {
        return true;
    }

    protected onToggleSelectAllOnPage(event: { checked: boolean }): void {
        this.toggleSelectAllOnPage(event.checked);
    }

    protected onRowCheckboxChange(item: Media): void {
        this.store.toggleSelected(item.id);
    }

    protected pageSelectionState(): { allSelected: boolean; indeterminate: boolean } {
        return super.pageSelectionState();
    }

    protected deleteSelectedMedia(): void {
        if (this.isViewerSig() || this.selectionMode()) {
            return;
        }
        const ids = [...this.store.selectedIdsSig()];
        if (ids.length === 0) {
            return;
        }
        this.#confirmationService
            .confirm(
                'admin.media.bulkDeleteConfirmTitle',
                'admin.media.bulkDeleteConfirmMessage',
                'admin.common.actions.confirm',
                'warning',
                { count: ids.length }
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (!confirmed) {
                    return;
                }
                this.store.setLoading(true);
                this.#bulkDeleteRunner
                    .run(ids, {
                        bulkDelete$: () =>
                            this.service.bulkDeleteMediaWithResponse(ids).pipe(take(1)),
                    })
                    .pipe(
                        finalize(() => this.store.setLoading(false)),
                        takeUntil(this.destroy$)
                    )
                    .subscribe({
                        next: (result) => {
                            if (result.message) {
                                this.#notificationService.success(result.message);
                            }
                            this.store.clearSelection();
                            this.loadItems();
                        },
                        error: (error) => {
                            const msg =
                                error?.error?.message ?? error?.message ?? '';
                            this.#notificationService.alert(msg);
                        },
                    });
            });
    }

    deleteMedia(media: Media): void {
        if (this.isViewerSig()) {
            return;
        }
        const confirmation = this.#confirmationService.confirm(
            'admin.media.dialogs.delete.title',
            'admin.media.dialogs.delete.confirm'
        );
        confirmation.pipe(take(1)).subscribe((result) => {
            if (result) {
                this.store.setLoading(true);
                this.service
                    .deleteMediaWithResponse(media.id)
                    .pipe(
                        finalize(() => this.store.setLoading(false)),
                        takeUntil(this.destroy$)
                    )
                    .subscribe({
                        next: (response) => {
                            this.store.removeItem(media.id);
                            this.store.clearPageSelection([media.id]);
                            this.#notificationService.success(response.message!);
                        },
                        error: (error) => {
                            this.store.setError(this.extractErrorMessage(error));
                            this.#notificationService.alert(error?.error?.message);
                        },
                    });
            }
        });
    }

    protected onGridAction(event: GridActionEvent<Media>): void {
        const { action, item } = event;
        switch (action) {
            case 'edit':
                this.openDetailDialog(item);
                break;
            case 'delete':
                this.deleteMedia(item);
                break;
            case 'select':
                this.selectMedia(item);
                break;
        }
    }

    override onPageChange(event: PageEvent): void {
        super.onPageChange(event);
    }

    onSortDropdownChange(sortCode: string): void {
        this.onSortChange(sortCode);
    }

    getMediaTypeClass(media: Media): string {
        switch (media.fileType) {
            case 'image':
                return 'bg-green-100 text-green-800 dark:bg-green-600 dark:text-green-50';
            case 'video':
                return 'bg-blue-100 text-blue-800 dark:bg-blue-600 dark:text-blue-50';
            case 'audio':
                return 'bg-purple-100 text-purple-800 dark:bg-purple-600 dark:text-purple-50';
            case 'document':
                return 'bg-orange-100 text-orange-800 dark:bg-orange-600 dark:text-orange-50';
            default:
                return 'bg-gray-100 text-gray-800 dark:bg-gray-600 dark:text-gray-50';
        }
    }
}
