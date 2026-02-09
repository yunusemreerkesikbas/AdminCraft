import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { SpaDialogBase, SpaDialogData } from '@shared/components/spa-dialog-base';

export interface JobErrorDialogData extends SpaDialogData {
    tenantSubdomain: string;
    type: string;
    error: string;
    createdAt: string;
}

@Component({
    selector: 'spa-job-error-dialog',
    standalone: true,
    imports: [SpaDialogComponent, MatIconModule, TranslocoModule, DatePipe],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <spa-dialog
            [title]="'admin.platform.dashboard.recentJobs.errorDialog.title' | transloco"
            [showSubmit]="false"
            [cancelLabel]="'admin.platform.dashboard.recentJobs.errorDialog.close'"
            [contentType]="'form'"
            (cancelled)="cancel()"
            (closed)="cancel()"
        >
            <div class="flex max-h-[80vh] flex-col gap-4">
                <div class="flex items-center gap-2 text-red-600">
                    <mat-icon>error</mat-icon>
                    <span class="text-sm font-medium">
                        {{ 'admin.platform.dashboard.recentJobs.errorDialog.errorMessage' | transloco }}
                    </span>
                </div>

                <div class="grid gap-4 sm:grid-cols-2">
                    <div>
                        <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                            {{ 'admin.platform.dashboard.recentJobs.tenantSubdomain' | transloco }}
                        </p>
                        <p class="mt-1 text-sm text-gray-900 dark:text-white">
                            {{ dialogData.tenantSubdomain }}
                        </p>
                    </div>

                    <div>
                        <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                            {{ 'admin.platform.dashboard.recentJobs.type' | transloco }}
                        </p>
                        <p class="mt-1 text-sm text-gray-900 dark:text-white">
                            {{ dialogData.type }}
                        </p>
                    </div>

                    <div>
                        <p class="text-sm font-medium text-gray-500 dark:text-gray-400">
                            {{ 'admin.platform.dashboard.recentJobs.createdAt' | transloco }}
                        </p>
                        <p class="mt-1 text-sm text-gray-900 dark:text-white">
                            {{ dialogData.createdAt | date: 'medium' }}
                        </p>
                    </div>
                </div>

                <div class="rounded-lg bg-red-50 p-4 dark:bg-red-900/20">
                    <p class="whitespace-pre-wrap font-mono text-sm text-red-800 dark:text-red-200">
                        {{ dialogData.error }}
                    </p>
                </div>
            </div>
        </spa-dialog>
    `,
})
export class JobErrorDialogComponent extends SpaDialogBase<void, JobErrorDialogData> {
    protected readonly dialogData: JobErrorDialogData = this.data ?? {
        tenantSubdomain: '',
        type: '',
        error: '',
        createdAt: '',
    };
}
