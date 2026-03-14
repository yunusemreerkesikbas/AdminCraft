import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaDialogContentComponent } from '@shared/components/spa-dialog/spa-dialog-content/spa-dialog-content.component';
import { SpaDialogFooterComponent } from '@shared/components/spa-dialog/spa-dialog-footer/spa-dialog-footer.component';
import { SpaDialogHeaderComponent } from '@shared/components/spa-dialog/spa-dialog-header/spa-dialog-header.component';
import { SpaDialogBase } from '@shared/components/spa-dialog-base';
import { take } from 'rxjs';
import { MediaBindDialogComponent } from '../media-bind-dialog/media-bind-dialog.component';
import { MediaDetailDialogComponent } from '../media-detail-dialog/media-detail-dialog.component';
import { Media, MediaUploadResultDialogData } from '../../media.types';

@Component({
    selector: 'app-media-upload-result-dialog',
    standalone: true,
    imports: [
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
    ],
    templateUrl: './media-upload-result-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MediaUploadResultDialogComponent extends SpaDialogBase<
    void,
    MediaUploadResultDialogData
> {
    #dialog = inject(MatDialog);

    protected readonly uploadedMediaSig = computed(() => this.data?.media ?? []);
    protected readonly boundMediaIdsSig = signal<number[]>([]);

    protected isBound(mediaId: number): boolean {
        return this.boundMediaIdsSig().includes(mediaId);
    }

    protected openBindDialog(media: Media): void {
        this.#dialog
            .open(MediaBindDialogComponent, {
                width: '640px',
                maxHeight: '90vh',
                disableClose: true,
                data: { media },
            })
            .afterClosed()
            .pipe(take(1))
            .subscribe((success) => {
                if (success) {
                    this.boundMediaIdsSig.update((ids) =>
                        ids.includes(media.id) ? ids : [...ids, media.id]
                    );
                }
            });
    }

    protected openDetails(media: Media): void {
        this.#dialog.open(MediaDetailDialogComponent, {
            width: '1100px',
            data: {
                mode: 'edit',
                media,
            },
        });
    }
}
