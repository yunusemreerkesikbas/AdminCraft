import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
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
        MediaListComponent
    ],
    templateUrl: './media-picker-dialog.component.html',
    styleUrl: './media-picker-dialog.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MediaPickerDialogComponent {
    #dialogRef = inject(MatDialogRef<MediaPickerDialogComponent>);

    select(media: Media): void {
        this.#dialogRef.close([media]);
    }

    close(): void {
        this.#dialogRef.close();
    }
}
