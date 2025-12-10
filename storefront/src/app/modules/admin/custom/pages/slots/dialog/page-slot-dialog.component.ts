import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { PageSlotListComponent } from '../list/page-slot-list.component';

@Component({
    selector: 'spa-page-slot-dialog',
    templateUrl: './page-slot-dialog.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        PageSlotListComponent,
        TranslocoModule
    ]
})
export class PageSlotDialogComponent {
    constructor(
        public dialogRef: MatDialogRef<PageSlotDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { pageId: number }
    ) {}

    close(): void {
        this.dialogRef.close();
    }
}
