import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    signal,
    ViewEncapsulation,
} from '@angular/core';
import {
    FormArray,
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import {
    SpaSelectComponent,
    SpaSelectOption,
} from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import {
    MailSubscriberAdminVm,
    MailSubscriberStatus,
    MailSubscriberSubscriptionVm,
    UpsertMailSubscriberPayload,
} from '../mail-marketing.types';
import { PlatformMailSubscriberAdminService } from '../platform-mail-subscriber-admin.service';
import { TenantMailSubscriberAdminService } from '../tenant-mail-subscriber-admin.service';

type MailScope = 'tenant' | 'platform';
type SupportedLanguage = 'TR' | 'EN';

export interface MailSubscriberEditDialogData {
    mode: 'create' | 'edit';
    scope: MailScope;
    templateTypes: string[];
    subscriber?: MailSubscriberAdminVm;
}

@Component({
    selector: 'spa-mail-subscriber-edit-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaCheckboxComponent,
        SpaInputComponent,
        SpaSelectComponent,
        SpaDialogComponent,
    ],
    templateUrl: './mail-subscriber-edit-dialog.component.html',
    styleUrls: ['./mail-subscriber-edit-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailSubscriberEditDialogComponent {
    readonly #dialogRef = inject(
        MatDialogRef<MailSubscriberEditDialogComponent>
    );
    readonly #fb = inject(FormBuilder);
    readonly #notificationService = inject(NotificationService);
    readonly #translocoService = inject(TranslocoService);
    readonly #tenantService = inject(TenantMailSubscriberAdminService);
    readonly #platformService = inject(PlatformMailSubscriberAdminService);
    readonly data = inject<MailSubscriberEditDialogData>(MAT_DIALOG_DATA);

    protected readonly isSubmittingSig = signal(false);
    protected readonly dialogTitleKeySig = computed(() =>
        this.data.mode === 'create'
            ? 'admin.mailMarketing.subscribers.dialog.createTitle'
            : 'admin.mailMarketing.subscribers.dialog.editTitle'
    );
    protected readonly languageOptions: SpaSelectOption<string>[] = [
        { value: 'TR', label: 'TR' },
        { value: 'EN', label: 'EN' },
    ];
    protected readonly statusOptions: SpaSelectOption<MailSubscriberStatus>[] =
        [
            {
                value: 'PENDING_CONFIRMATION',
                labelKey:
                    'admin.mailMarketing.subscriberStatus.PENDING_CONFIRMATION',
            },
            {
                value: 'ACTIVE',
                labelKey: 'admin.mailMarketing.subscriberStatus.ACTIVE',
            },
            {
                value: 'UNSUBSCRIBED',
                labelKey: 'admin.mailMarketing.subscriberStatus.UNSUBSCRIBED',
            },
        ];
    protected readonly templateTypeOptions: SpaSelectOption<string>[] =
        this.data.templateTypes.map((templateType) => ({
            value: templateType,
            label: templateType,
        }));
    protected readonly form = this.#buildForm();

    protected get subscriptionsArray(): FormArray<FormGroup> {
        return this.form.get('subscriptions') as FormArray<FormGroup>;
    }

    #buildForm(): FormGroup {
        const subscriber = this.data.subscriber;
        const form = this.#fb.group({
            email: [
                subscriber?.email ?? '',
                [Validators.required, Validators.email],
            ],
            status: [subscriber?.status ?? 'ACTIVE', [Validators.required]],
            subscriptions: this.#fb.array([]),
        });
        if (this.data.mode === 'edit') {
            form.get('email')?.disable();
        }

        const formArray = form.get('subscriptions') as FormArray<FormGroup>;
        const subscriptions =
            subscriber?.subscriptions && subscriber.subscriptions.length > 0
                ? subscriber.subscriptions
                : [this.#defaultSubscription()];
        subscriptions.forEach((subscription) =>
            formArray.push(this.#createSubscriptionGroup(subscription))
        );
        return form;
    }

    protected addSubscription(): void {
        this.subscriptionsArray.push(
            this.#createSubscriptionGroup(this.#defaultSubscription())
        );
    }

    protected removeSubscription(index: number): void {
        if (this.subscriptionsArray.length <= 1) {
            return;
        }
        this.subscriptionsArray.removeAt(index);
    }

    protected templateOptionsFor(index: number): SpaSelectOption<string>[] {
        const current = this.subscriptionsArray
            .at(index)
            ?.get('templateType')?.value;
        const used = new Set(
            this.subscriptionsArray.controls
                .map((group) => group.get('templateType')?.value)
                .filter((value) => value && value !== current)
        );
        return this.templateTypeOptions.map((option) => ({
            ...option,
            disabled: used.has(option.value),
        }));
    }

    protected canAddSubscription(): boolean {
        return this.subscriptionsArray.length < this.templateTypeOptions.length;
    }

    protected cancel(): void {
        this.#dialogRef.close(false);
    }

    protected save(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.form.setErrors(null);
        const payload = this.#buildPayload();
        if (this.#hasDuplicateTemplates(payload.subscriptions)) {
            this.form.setErrors({ duplicateTemplate: true });
            return;
        }

        this.isSubmittingSig.set(true);
        if (this.data.mode === 'edit' && !this.data.subscriber) {
            this.isSubmittingSig.set(false);
            this.form.setErrors({ invalidContext: true });
            return;
        }

        const request$ = this.data.mode === 'create'
            ? this.#activeService().create(payload)
            : this.#activeService().update(this.data.subscriber.id, payload);

        request$.pipe(take(1)).subscribe({
            next: () => {
                this.isSubmittingSig.set(false);
                this.#dialogRef.close(true);
            },
            error: (error: any) => {
                this.isSubmittingSig.set(false);
                this.#notificationService.alert(error?.error?.message ?? '');
            },
        });
    }

    #activeService():
        | TenantMailSubscriberAdminService
        | PlatformMailSubscriberAdminService {
        return this.data.scope === 'platform'
            ? this.#platformService
            : this.#tenantService;
    }

    #buildPayload(): UpsertMailSubscriberPayload {
        const emailControl = this.form.get('email');
        const email =
            this.data.subscriber?.email ??
            String(emailControl?.value ?? '')
                .trim()
                .toLowerCase();
        const status = this.form.get('status')?.value as MailSubscriberStatus;

        const subscriptions = this.subscriptionsArray.controls.map((group) => ({
            templateType: String(
                group.get('templateType')?.value ?? ''
            ).toUpperCase(),
            preferredLanguage: this.#normalizeLanguage(
                group.get('preferredLanguage')?.value ??
                    this.#translocoService.getActiveLang()
            ),
            source: this.#normalizeSource(group.get('source')?.value),
            permission: !!group.get('permission')?.value,
        }));

        return {
            email,
            status,
            subscriptions,
        };
    }

    #hasDuplicateTemplates(
        subscriptions: MailSubscriberSubscriptionVm[]
    ): boolean {
        const templateSet = new Set<string>();
        for (const subscription of subscriptions) {
            if (templateSet.has(subscription.templateType)) {
                return true;
            }
            templateSet.add(subscription.templateType);
        }
        return false;
    }

    #createSubscriptionGroup(
        subscription: MailSubscriberSubscriptionVm
    ): FormGroup {
        return this.#fb.group({
            templateType: [subscription.templateType, [Validators.required]],
            preferredLanguage: [
                this.#normalizeLanguage(
                    subscription.preferredLanguage ??
                        this.#translocoService.getActiveLang()
                ),
                [Validators.required],
            ],
            source: [subscription.source ?? '', [Validators.maxLength(120)]],
            permission: [subscription.permission ?? true],
        });
    }

    #defaultSubscription(): MailSubscriberSubscriptionVm {
        return {
            templateType:
                this.templateTypeOptions[0]?.value ?? 'NEWSLETTER_DEFAULT',
            preferredLanguage: this.#normalizeLanguage(
                this.#translocoService.getActiveLang()
            ),
            source: null,
            permission: true,
        };
    }

    #normalizeSource(rawValue: unknown): string | null {
        const value = String(rawValue ?? '').trim();
        return value.length ? value : null;
    }

    #normalizeLanguage(language: unknown): SupportedLanguage {
        return String(language ?? '')
            .trim()
            .toUpperCase() === 'TR'
            ? 'TR'
            : 'EN';
    }
}
