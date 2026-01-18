import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { MediaListComponent } from '../../list/media-list.component';
import { Media } from '../../media.types';

@Component({
    selector: 'app-media-picker-dialog',
    standalone: true,
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        MediaListComponent,
    ],
    templateUrl: './media-picker-dialog.component.html',
    styleUrl: './media-picker-dialog.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MediaPickerDialogComponent {
    readonly #dialogRef = inject(MatDialogRef<MediaPickerDialogComponent>);
    protected data = inject(MAT_DIALOG_DATA) as {
        mode: 'single' | 'multiple';
        selectedIds?: number[];
    };

    protected selectedItems = new Map<number, Media>();

    select(media: Media): void {
        if (this.data.mode === 'multiple') {
            if (this.selectedItems.has(media.id)) {
                this.selectedItems.delete(media.id);
            } else {
                this.selectedItems.set(media.id, media);
            }
        } else {
            this.#dialogRef.close([media]);
        }
    }

    submit(): void {
        this.#dialogRef.close(Array.from(this.selectedItems.values()));
    }

    close(): void {
        this.#dialogRef.close();
    }
}
