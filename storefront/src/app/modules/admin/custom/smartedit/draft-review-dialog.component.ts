import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { SmartEditDraftGroup } from './smartedit.types';

export interface SmartEditDraftReviewDialogData {
    group: SmartEditDraftGroup;
}

@Component({
    selector: 'spa-smartedit-draft-review-dialog',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [MatDialogModule, MatButtonModule, MatIconModule, TranslocoModule],
    templateUrl: './draft-review-dialog.component.html',
    styleUrls: ['./draft-review-dialog.component.scss'],
})
export class SmartEditDraftReviewDialogComponent {
    readonly data = inject<SmartEditDraftReviewDialogData>(MAT_DIALOG_DATA);
    readonly #dialogRef = inject(
        MatDialogRef<SmartEditDraftReviewDialogComponent>
    );

    close(): void {
        this.#dialogRef.close();
    }
}
