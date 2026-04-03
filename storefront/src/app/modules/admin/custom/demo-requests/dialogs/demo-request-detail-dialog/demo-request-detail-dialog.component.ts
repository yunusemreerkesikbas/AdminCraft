import { DatePipe } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    inject,
    ViewEncapsulation,
} from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { PlatformDemoRequestRow } from '../../demo-request.types';

@Component({
    selector: 'spa-demo-request-detail-dialog',
    standalone: true,
    imports: [TranslocoModule, SpaDialogComponent, DatePipe],
    templateUrl: './demo-request-detail-dialog.component.html',
    styleUrls: ['./demo-request-detail-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaDemoRequestDetailDialogComponent {
    protected readonly dialogRef = inject(MatDialogRef<SpaDemoRequestDetailDialogComponent>);
    protected readonly data = inject<PlatformDemoRequestRow>(MAT_DIALOG_DATA);

    protected close(): void {
        this.dialogRef.close();
    }
}
