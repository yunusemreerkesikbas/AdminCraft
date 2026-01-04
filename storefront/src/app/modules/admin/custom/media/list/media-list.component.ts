import { CommonModule, DatePipe } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    EventEmitter,
    inject,
    Input,
    Output,
    signal
} from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseCrudListComponent } from '@core/crud/base-crud-list.component';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { debounceTime, Observable, take, takeUntil } from 'rxjs';
import { MediaDetailDialogComponent } from '../dialogs/media-detail-dialog/media-detail-dialog.component';
import { MediaUploadDialogComponent } from '../dialogs/media-upload-dialog/media-upload-dialog.component';
import { MediaService } from '../media.service';
import { MediaStore } from '../media.store';
import { Media, MediaDetailDialogData, UpdateMediaRequest } from '../media.types';

@Component({
    selector: 'app-media-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        DatePipe,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatPaginatorModule,
        MatProgressBarModule,
        MatTooltipModule,
        TranslocoModule
    ],
    templateUrl: './media-list.component.html',
    styles: [
    `
        .inventory-grid {
            grid-template-columns: minmax(200px, 2fr) 96px;

            @screen md {
                grid-template-columns: minmax(200px, 2fr) 112px;
            }

            @screen lg {
                grid-template-columns: minmax(240px, 3fr) 120px 140px 140px 96px;
            }

            @screen xl {
                grid-template-columns: minmax(240px, 3fr) 120px 140px 120px 140px 96px;
            }
        }
    `,
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MediaListComponent extends BaseCrudListComponent<Media, FormData, UpdateMediaRequest> {

    @Input() selectionMode = false;
    @Output() onMediaSelect = new EventEmitter<Media>();

    protected override service = inject(MediaService);
    protected override store = inject(MediaStore);
    #matDialog = inject(MatDialog);
    #notificationService = inject(NotificationService);
    #confirmationService = inject(ConfirmationService);
    #transloco = inject(TranslocoService);
    #tenantContext = inject(TenantContextService);

    protected pageSizeSig = signal(24);
    protected pageIndexSig = signal(0);
    protected totalItemsSig = computed(() => this.store.page()?.totalElements ?? 0);
    protected searchInputControl = new FormControl('');


    protected paginatedItemsSig = computed(() => this.store.filteredItems()); 

    protected override onInit(): void {
        this.#setupSearchDebounce();
    }

    protected override loadItems(): void {
        this.store.setLoading(true);
        this.service.listPaged({
            page: this.pageIndexSig(),
            size: this.pageSizeSig(),
            sort: 'createdAt,desc'
        }).subscribe({
            next: (page) => {
                this.store.setPagedItems(page);
                this.store.setLoading(false);
            },
            error: (error) => {
                this.onLoadError(error);
                this.store.setLoading(false);
            }
        });
    }

    protected override fetchItems(): Observable<Media[]> {
        return this.service.list(); 
    }

    #setupSearchDebounce(): void {
        this.searchInputControl.valueChanges.pipe(
            debounceTime(300),
            takeUntil(this.destroy$)
        ).subscribe(query => {
            this.onSearchChange(query || '');
        });
    }

    protected override onSearchChange(query: string): void {
        super.onSearchChange(query);
        this.store.setSearchQuery(query);
        this.pageIndexSig.set(0);
    }

    openUploadDialog(): void {
        this.#matDialog.open(MediaUploadDialogComponent, {
            width: '700px',
            maxHeight: '90vh',
            disableClose: true,
            data: {
                languages: this.#tenantContext.tenant()?.supportedLanguages?.map(l => l.code) || ['EN']
            }
        }).afterClosed().pipe(take(1)).subscribe(result => {
            if (result && result.length > 0) {
                this.loadItems();
                this.#notificationService.success('admin.media.messages.uploadSuccess');
            }
        });
    }

    openDetailDialog(media: Media): void {
        if (this.selectionMode) {
            this.selectMedia(media);
            return;
        }

        this.#matDialog.open(MediaDetailDialogComponent, {
            width: '800px',
            data: {
                mode: 'edit',
                media
            } as MediaDetailDialogData
        }).afterClosed().pipe(take(1)).subscribe(result => {
            if (result) {
                this.#notificationService.success('admin.media.messages.updateSuccess');
                this.loadItems();
            }
        });
    }

    selectMedia(media: Media): void {
        this.onMediaSelect.emit(media);
    }

    deleteMedia(media: Media): void {
        const confirmation = this.#confirmationService.confirm(
            'admin.media.dialogs.delete.title',
            'admin.media.dialogs.delete.confirm'
        );
        confirmation.pipe(takeUntil(this.destroy$)).subscribe((result) => {
            if (result) {
                this.deleteItem(media);
            }
        });
    }

    protected override onDeleteSuccess(item: Media): void {
        this.#notificationService.success('admin.media.messages.deleteSuccess');
    }

    protected override onDeleteError(error: any): void {
         this.#notificationService.alert('admin.media.messages.deleteError');
    }

    onPageChange(event: PageEvent): void {
        this.pageIndexSig.set(event.pageIndex);
        this.pageSizeSig.set(event.pageSize);
        this.loadItems();
    }

    getMediaTypeClass(media: Media): string {
        switch (media.fileType) {
            case 'image': return 'bg-green-100 text-green-800 dark:bg-green-600 dark:text-green-50';
            case 'video': return 'bg-blue-100 text-blue-800 dark:bg-blue-600 dark:text-blue-50';
            case 'audio': return 'bg-purple-100 text-purple-800 dark:bg-purple-600 dark:text-purple-50';
            case 'document': return 'bg-orange-100 text-orange-800 dark:bg-orange-600 dark:text-orange-50';
            default: return 'bg-gray-100 text-gray-800 dark:bg-gray-600 dark:text-gray-50';
        }
    }
}
