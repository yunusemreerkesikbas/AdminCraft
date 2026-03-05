import { NgClass } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    inject,
    signal,
} from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { AdminPageHeaderComponent } from 'app/shared/components/admin-page-header/admin-page-header.component';
import { Subject, takeUntil } from 'rxjs';
import { ImpExService } from './impex.service';
import { ImpExResult } from './impex.types';

@Component({
    selector: 'spa-impex',
    standalone: true,
    templateUrl: './impex.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: { class: 'flex flex-col flex-auto' },
    imports: [
        NgClass,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaTextareaComponent,
    ],
})
export class SpaImpExComponent implements OnDestroy {
    readonly #impexService = inject(ImpExService);
    readonly #notificationService = inject(NotificationService);
    readonly #confirmationService = inject(ConfirmationService);
    readonly #destroy$ = new Subject<void>();

    protected readonly sqlControl = new FormControl('', [Validators.required]);
    protected readonly isRunningSig = signal(false);
    protected readonly resultSig = signal<ImpExResult | null>(null);
    protected readonly showDetailsSig = signal(false);

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    protected onRun(): void {
        if (this.sqlControl.invalid || !this.sqlControl.value?.trim()) {
            this.sqlControl.markAsTouched();
            return;
        }

        this.#confirmationService
            .confirm(
                'admin.impex.confirm.title',
                'admin.impex.confirm.message',
                'admin.impex.confirm.run',
                'warning'
            )
            .pipe(takeUntil(this.#destroy$))
            .subscribe((confirmed) => {
                if (confirmed) {
                    this.#execute();
                }
            });
    }

    protected onClear(): void {
        this.sqlControl.reset('');
        this.resultSig.set(null);
        this.showDetailsSig.set(false);
    }

    protected onToggleDetails(): void {
        this.showDetailsSig.update((v) => !v);
    }

    #execute(): void {
        this.isRunningSig.set(true);
        this.resultSig.set(null);
        this.showDetailsSig.set(false);

        this.#impexService
            .execute(this.sqlControl.value!)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: ({ message, result }) => {
                    this.resultSig.set(result);
                    this.isRunningSig.set(false);
                    if (result.status === 'SUCCESS') {
                        this.#notificationService.success(message);
                    } else {
                        this.#notificationService.warning(message);
                    }
                },
                error: (err) => {
                    this.isRunningSig.set(false);
                    this.#notificationService.alert(err.error.message);
                },
            });
    }
}
