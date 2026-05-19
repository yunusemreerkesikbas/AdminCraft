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
import { SpaToggleComponent } from '@shared/components/custom-ui/spa-toggle/spa-toggle.component';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import {
    OutreachTemplateVm,
    CreateOutreachTemplatePayload,
    UpdateOutreachTemplatePayload,
} from './platform-outreach.types';
import { PlatformOutreachService } from './platform-outreach.service';

export interface OutreachTemplateEditDialogData {
    mode: 'create' | 'edit';
    template?: OutreachTemplateVm;
}

@Component({
    selector: 'spa-platform-outreach-template-edit-dialog',
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
        SpaToggleComponent,
        SpaDialogComponent,
    ],
    templateUrl: './platform-outreach-template-edit-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlatformOutreachTemplateEditDialogComponent {
    readonly #dialogRef = inject(MatDialogRef<PlatformOutreachTemplateEditDialogComponent>);
    readonly #fb = inject(FormBuilder);
    readonly #outreachService = inject(PlatformOutreachService);
    readonly #notify = inject(NotificationService);
    readonly data = inject<OutreachTemplateEditDialogData>(MAT_DIALOG_DATA);

    protected readonly isSubmittingSig = signal(false);
    protected readonly dialogTitleKeySig = computed(() =>
        this.data.mode === 'create'
            ? 'admin.outreach.templates.dialog.createTitle'
            : 'admin.outreach.templates.dialog.editTitle'
    );

    protected readonly languageOptions: SpaSelectOption<string>[] = [
        { value: 'TR', label: 'TR' },
        { value: 'EN', label: 'EN' },
    ];

    protected readonly availableVariables = [
        '{{contactName}}',
        '{{companyName}}',
        '{{city}}',
        '{{email}}',
        '{{fromName}}',
    ];

    protected readonly form = this.#buildForm();

    #buildForm() {
        const tmpl = this.data.template;
        return this.#fb.group({
            name: [tmpl?.name ?? '', [Validators.required]],
            language: [tmpl?.language ?? 'TR', [Validators.required]],
            subject: [tmpl?.subject ?? '', [Validators.required]],
            content: [tmpl?.content ?? '', [Validators.required]],
            isActive: [tmpl?.isActive ?? true],
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
                ? this.#outreachService.createTemplate(this.#buildCreatePayload(raw))
                : this.#outreachService.updateTemplate(
                    this.data.template!.id,
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

    #buildCreatePayload(raw: ReturnType<typeof this.form.getRawValue>): CreateOutreachTemplatePayload {
        return {
            name: String(raw.name ?? '').trim(),
            language: String(raw.language ?? 'TR'),
            subject: String(raw.subject ?? '').trim(),
            content: String(raw.content ?? '').trim(),
        };
    }

    #buildUpdatePayload(raw: ReturnType<typeof this.form.getRawValue>): UpdateOutreachTemplatePayload {
        return {
            ...this.#buildCreatePayload(raw),
            isActive: !!raw.isActive,
        };
    }
}
