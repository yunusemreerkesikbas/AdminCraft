import { DatePipe } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    ViewEncapsulation,
    inject,
} from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { PlatformContactRequestRow } from '../../contact-request.types';

@Component({
    selector: 'spa-contact-request-detail-dialog',
    standalone: true,
    imports: [TranslocoModule, SpaDialogComponent, DatePipe],
    templateUrl: './contact-request-detail-dialog.component.html',
    styleUrls: ['./contact-request-detail-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaContactRequestDetailDialogComponent {
    protected readonly dialogRef = inject(MatDialogRef<SpaContactRequestDetailDialogComponent>);
    protected readonly data = inject<PlatformContactRequestRow>(MAT_DIALOG_DATA);

    protected close(): void {
        this.dialogRef.close();
    }
}
