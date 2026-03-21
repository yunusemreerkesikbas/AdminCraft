import { Component, Inject, OnInit, signal, computed, DestroyRef, inject, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslocoPipe } from '@jsverse/transloco';
import { interval, switchMap, takeWhile } from 'rxjs';
import { TenantsService } from '@modules/admin/custom/tenants/tenants.service';
import { LanguageProvisionDialogData, LanguageProvisioningJob } from './language-provision.types';
import { NotificationService } from '@shared/notifications/notification.service';

@Component({
    selector: 'spa-language-provision-dialog',
    standalone: true,
    imports: [
        MatDialogModule,
        MatButtonModule,
        MatProgressBarModule,
        TranslocoPipe
    ],
    templateUrl: './language-provision-dialog.component.html',
    styleUrls: ['./language-provision-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LanguageProvisionDialogComponent implements OnInit {
    #tenantsService = inject(TenantsService);
    #destroyRef = inject(DestroyRef);
    #notification = inject(NotificationService);

    protected languageJob = signal<LanguageProvisioningJob | null>(null);
    protected isConfirmationPhase = signal<boolean>(false);
    protected isProgressPhase = signal<boolean>(false);
    protected progressPercentage = computed(() => {
        const job = this.languageJob();
        if (!job || !job.totalItems || job.totalItems === 0) return 0;
        return Math.round((job.processedItems / job.totalItems) * 100);
    });

    protected statusClass = computed(() => {
        const status = this.languageJob()?.status;
        if (status === 'COMPLETED') return 'text-green-600';
        if (status === 'FAILED') return 'text-red-600';
        return 'text-blue-600';
    });

    protected progressMode = computed(() =>
        this.languageJob()?.totalItems ? 'determinate' : 'indeterminate'
    );

    protected progressColor = computed(() =>
        this.languageJob()?.status === 'FAILED' ? 'warn' : 'primary'
    );

    protected isCompleted = computed(() =>
        this.languageJob()?.status === 'COMPLETED'
    );

    protected isFailed = computed(() =>
        this.languageJob()?.status === 'FAILED'
    );

    protected canClose = computed(() =>
        this.isCompleted() || this.isFailed()
    );

    constructor(
        private dialogRef: MatDialogRef<LanguageProvisionDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: LanguageProvisionDialogData
    ) {}

    ngOnInit(): void {
        if (!this.data.jobUuid) {
            this.isConfirmationPhase.set(true);
        } else {
            this.isProgressPhase.set(true);
            this.#initializeLanguageJob();
            this.#startPolling(this.data.jobUuid);
        }
    }

    #initializeLanguageJob(): void {
        if (this.data.jobUuid && this.data.status) {
            this.languageJob.set({
                uuid: this.data.jobUuid,
                status: this.data.status,
                processedItems: this.data.processedItems || 0,
                totalItems: this.data.totalItems || 0,
                errorMessage: this.data.errorMessage
            });
        }
    }

    #startPolling(jobUuid: string): void {
        interval(2000)
            .pipe(
                switchMap(() => this.#tenantsService.getLanguageProvisioningJobStatus(jobUuid)),
                takeWhile(
                    (response) =>
                        response.data?.status !== 'COMPLETED' &&
                        response.data?.status !== 'FAILED',
                    true
                ),
                takeUntilDestroyed(this.#destroyRef)
            )
            .subscribe({
                next: (response) => {
                    const job = response.data;
                    if (!job) {
                        this.#notification.alert(response.message ?? '');
                        return;
                    }
                    this.languageJob.set(job);
                    if (job.status === 'COMPLETED') {
                        this.#notification.success(response.message ?? '');
                    } else if (job.status === 'FAILED') {
                        this.#notification.alert(response.message ?? job.errorMessage ?? '');
                    }
                },
                error: (err) => {
                    const current = this.languageJob();
                    this.#notification.alert(err?.error?.message ?? '');
                    if (current) {
                        this.languageJob.set({
                            ...current,
                            status: 'FAILED',
                            errorMessage: err?.error?.message ?? ''
                        });
                    }
                }
            });
    }

    protected onConfirm(): void {
        this.dialogRef.close(true);
    }

    protected onCancel(): void {
        this.dialogRef.close(false);
    }

    protected close(): void {
        this.dialogRef.close();
    }
}
