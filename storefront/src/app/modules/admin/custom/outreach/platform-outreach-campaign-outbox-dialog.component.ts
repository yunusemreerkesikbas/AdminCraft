import { DatePipe, NgClass } from '@angular/common';
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
import { take } from 'rxjs';
import {
    OutreachCampaignVm,
    OutreachCampaignOutboxEntryVm,
    OutreachCampaignContactStatus,
} from './platform-outreach.types';
import { PlatformOutreachService } from './platform-outreach.service';

export interface OutreachCampaignOutboxDialogData {
    campaignId: number;
    campaignName: string;
    campaign: OutreachCampaignVm;
}

@Component({
    selector: 'spa-platform-outreach-campaign-outbox-dialog',
    standalone: true,
    imports: [
        DatePipe,
        NgClass,
        MatButtonModule,
        MatDialogModule,
        MatIconModule,
        TranslocoModule,
    ],
    templateUrl: './platform-outreach-campaign-outbox-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlatformOutreachCampaignOutboxDialogComponent implements OnInit {
    readonly #dialogRef = inject(MatDialogRef<PlatformOutreachCampaignOutboxDialogComponent>);
    readonly #outreachService = inject(PlatformOutreachService);
    readonly data = inject<OutreachCampaignOutboxDialogData>(MAT_DIALOG_DATA);

    protected readonly loadingSig = signal(true);
    protected readonly errorSig = signal(false);
    protected readonly entriesSig = signal<OutreachCampaignOutboxEntryVm[]>([]);

    ngOnInit(): void {
        this.#outreachService
            .getCampaignOutbox(this.data.campaignId)
            .pipe(take(1))
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

    protected statusClass(status: OutreachCampaignContactStatus): string {
        switch (status) {
            case 'SENT':
                return 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-200';
            case 'FAILED':
                return 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-200';
            default:
                return 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-400';
        }
    }

    protected onClose(): void {
        this.#dialogRef.close();
    }
}
