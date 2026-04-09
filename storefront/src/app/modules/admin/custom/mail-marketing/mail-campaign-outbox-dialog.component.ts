import { CommonModule, DatePipe, NgClass } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { Observable } from 'rxjs';
import { MailCampaignVm, MailOutboxEntryVm } from './mail-marketing.types';

export interface MailCampaignOutboxDialogData {
    campaign: MailCampaignVm;
    getCampaignOutbox: (id: number) => Observable<MailOutboxEntryVm[]>;
}

@Component({
    selector: 'spa-mail-campaign-outbox-dialog',
    standalone: true,
    templateUrl: './mail-campaign-outbox-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        DatePipe,
        NgClass,
        MatButtonModule,
        MatDialogModule,
        MatIconModule,
        TranslocoModule,
    ],
})
export class MailCampaignOutboxDialogComponent implements OnInit {
    readonly #dialogRef = inject(MatDialogRef<MailCampaignOutboxDialogComponent>);
    readonly data = inject<MailCampaignOutboxDialogData>(MAT_DIALOG_DATA);

    protected readonly loadingSig = signal(true);
    protected readonly errorSig = signal(false);
    protected readonly entriesSig = signal<MailOutboxEntryVm[]>([]);

    ngOnInit(): void {
        this.data
            .getCampaignOutbox(this.data.campaign.id)
            .subscribe({
                next: (entries) => {
                    this.entriesSig.set(entries);
                    this.loadingSig.set(false);
                },
                error: () => {
                    this.loadingSig.set(false);
                    this.errorSig.set(true);
                },
            });
    }

    protected onClose(): void {
        this.#dialogRef.close();
    }
}
