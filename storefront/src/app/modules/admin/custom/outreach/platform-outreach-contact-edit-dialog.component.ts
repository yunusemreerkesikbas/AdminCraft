import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import {
    SpaSelectComponent,
    SpaSelectOption,
} from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import {
    OutreachContactVm,
    OutreachContactStatus,
    CreateOutreachContactPayload,
    UpdateOutreachContactPayload,
} from './platform-outreach.types';
import { PlatformOutreachService } from './platform-outreach.service';

export interface OutreachContactEditDialogData {
    mode: 'create' | 'edit';
    contact?: OutreachContactVm;
}

@Component({
    selector: 'spa-platform-outreach-contact-edit-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaTextareaComponent,
        SpaSelectComponent,
        SpaDialogComponent,
    ],
    templateUrl: './platform-outreach-contact-edit-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlatformOutreachContactEditDialogComponent {
    readonly #dialogRef = inject(MatDialogRef<PlatformOutreachContactEditDialogComponent>);
    readonly #fb = inject(FormBuilder);
    readonly #outreachService = inject(PlatformOutreachService);
    readonly #notify = inject(NotificationService);
    readonly data = inject<OutreachContactEditDialogData>(MAT_DIALOG_DATA);

    protected readonly isSubmittingSig = signal(false);
    protected readonly dialogTitleKeySig = computed(() =>
        this.data.mode === 'create'
            ? 'admin.outreach.contacts.dialog.createTitle'
            : 'admin.outreach.contacts.dialog.editTitle'
    );

    protected readonly statusOptions: SpaSelectOption<OutreachContactStatus>[] = [
        { value: 'ACTIVE', label: 'ACTIVE' },
        { value: 'UNSUBSCRIBED', label: 'UNSUBSCRIBED' },
    ];

    protected readonly form = this.#buildForm();

    #buildForm() {
        const contact = this.data.contact;
        return this.#fb.group({
            fullName: [contact?.fullName ?? '', [Validators.required]],
            email: [
                contact?.email ?? '',
                [Validators.required, Validators.email],
            ],
            companyName: [contact?.companyName ?? ''],
            city: [contact?.city ?? ''],
            notes: [contact?.notes ?? ''],
            status: [contact?.status ?? 'ACTIVE'],
        });
    }

    protected cancel(): void {
        this.#dialogRef.close(false);
    }

    protected save(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.isSubmittingSig.set(true);

        const raw = this.form.getRawValue();
        const request$ =
            this.data.mode === 'create'
                ? this.#outreachService.createContact(
                    this.#buildCreatePayload(raw)
                )
                : this.#outreachService.updateContact(
                    this.data.contact!.id,
                    this.#buildUpdatePayload(raw)
                );

        request$.pipe(take(1)).subscribe({
            next: () => {
                this.isSubmittingSig.set(false);
                this.#dialogRef.close(true);
            },
            error: (error: any) => {
                this.isSubmittingSig.set(false);
                this.#notify.alert(error?.error?.message ?? '');
            },
        });
    }

    #buildCreatePayload(raw: ReturnType<typeof this.form.getRawValue>): CreateOutreachContactPayload {
        return {
            fullName: String(raw.fullName ?? '').trim(),
            email: String(raw.email ?? '').trim().toLowerCase(),
            companyName: String(raw.companyName ?? '').trim() || undefined,
            city: String(raw.city ?? '').trim() || undefined,
            notes: String(raw.notes ?? '').trim() || undefined,
        };
    }

    #buildUpdatePayload(raw: ReturnType<typeof this.form.getRawValue>): UpdateOutreachContactPayload {
        return {
            ...this.#buildCreatePayload(raw),
            status: (raw.status ?? 'ACTIVE') as OutreachContactStatus,
        };
    }
}
