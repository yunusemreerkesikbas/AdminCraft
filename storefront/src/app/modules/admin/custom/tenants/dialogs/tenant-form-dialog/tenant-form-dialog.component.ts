import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import {
    VALIDATION_LIMITS,
    VALIDATION_PATTERNS,
} from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import {
    CreateTenantRequest,
    Currency,
    CURRENCY_LABELS,
    Language,
    Tenant,
    UpdateTenantRequest,
} from '../../tenants.types';
import { TenantsService } from '../../tenants.service';

export interface TenantFormDialogData {
    tenant?: Tenant;
    mode: 'create' | 'edit';
}

export interface TenantFormDialogResult {
    updated: boolean;
    supportedLanguages?: Language[];
    tenant?: Tenant;
}

@Component({
    selector: 'spa-tenant-form-dialog',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaTextareaComponent,
        SpaDialogComponent,
    ],
    templateUrl: './tenant-form-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TenantFormDialogComponent implements OnInit {
    #fb = inject(FormBuilder);
    #dialogRef = inject(MatDialogRef<TenantFormDialogComponent>);
    protected data = inject<TenantFormDialogData>(MAT_DIALOG_DATA);
    #tenantsService = inject(TenantsService);
    #notificationService = inject(NotificationService);

    form!: FormGroup;
    mode: 'create' | 'edit' = this.data.mode;
    isLoadingSig = signal(false);

    languageOptions = Object.values(Language).map((lang) => ({
        value: lang,
        labelKey: `admin.common.languages.${lang.toLowerCase()}`,
    }));

    currencyOptions = Object.values(Currency).map((currency) => ({
        value: currency,
        label: CURRENCY_LABELS[currency],
    }));

    ngOnInit(): void {
        this.#buildForm();
        if (this.isEditMode && this.data.tenant) {
            const supportedLanguages = this.data.tenant.supportedLanguages
                ? this.data.tenant.supportedLanguages.map(
                      (lang) => lang.code as Language
                  )
                : [this.data.tenant.defaultLanguage];

            this.form.patchValue({
                companyName: this.data.tenant.companyName,
                subdomain: this.data.tenant.subdomain,
                supportedLanguages,
                defaultLanguage: this.data.tenant.defaultLanguage,
                currency: this.data.tenant.currency,
                customDomain: this.data.tenant.customDomain || '',
                notes: this.data.tenant.notes || '',
            });
            this.form.get('subdomain')?.disable();
        }
    }

    readonly isEditMode = this.data.mode === 'edit';

    #buildForm(): void {
        this.form = this.#fb.group({
            companyName: [
                '',
                [
                    Validators.required,
                    Validators.maxLength(VALIDATION_LIMITS.TENANT_COMPANY_NAME_MAX),
                ],
            ],
            subdomain: [
                '',
                [
                    Validators.required,
                    Validators.minLength(3),
                    Validators.maxLength(VALIDATION_LIMITS.TENANT_SUBDOMAIN_MAX),
                    Validators.pattern(VALIDATION_PATTERNS.SUBDOMAIN),
                ],
            ],
            supportedLanguages: [[Language.TR], [Validators.required]],
            defaultLanguage: [Language.TR, [Validators.required]],
            currency: [Currency.TRY, [Validators.required]],
            customDomain: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.TENANT_CUSTOM_DOMAIN_MAX)],
            ],
            notes: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.TENANT_NOTES_MAX)],
            ],
        });
    }

    onSubmit(): void {
        if (!this.form.valid) {
            return;
        }

        const formValue = this.form.getRawValue();
        const supportedLanguages = (formValue.supportedLanguages ||
            []) as Language[];
        const defaultLanguage = formValue.defaultLanguage as Language;

        if (
            defaultLanguage &&
            supportedLanguages.length > 0 &&
            !supportedLanguages.includes(defaultLanguage)
        ) {
            this.#notificationService.warning(
                'admin.tenants.validation.defaultLanguageInSupported'
            );
            return;
        }

        this.isLoadingSig.set(true);

        if (this.isEditMode) {
            const payload: UpdateTenantRequest = {
                companyName: formValue.companyName,
                supportedLanguages,
                defaultLanguage,
                currency: formValue.currency,
                customDomain: formValue.customDomain,
                notes: formValue.notes,
            };

            (Object.keys(payload) as Array<keyof UpdateTenantRequest>).forEach(
                (k) =>
                    payload[k] === '' &&
                    delete payload[k]
            );

            this.#tenantsService
                .updateWithResponse(this.data.tenant!.id, payload)
                .pipe(take(1))
                .subscribe({
                    next: (response) => {
                        this.isLoadingSig.set(false);
                        this.#notificationService.success(
                            response.message ||
                                'admin.common.messages.operationSuccess'
                        );
                        this.#dialogRef.close({
                            updated: true,
                            supportedLanguages,
                            tenant: response.data,
                        });
                    },
                    error: (error) => {
                        this.isLoadingSig.set(false);
                        this.#notificationService.alert(
                            error.error?.message ||
                                'admin.common.errors.unexpected'
                        );
                    },
                });
            return;
        }

        const payload: CreateTenantRequest = {
            companyName: formValue.companyName,
            subdomain: formValue.subdomain,
            supportedLanguages,
            defaultLanguage,
            currency: formValue.currency,
            customDomain: formValue.customDomain || undefined,
            notes: formValue.notes || undefined,
        };

        this.#tenantsService
            .createWithResponse(payload)
            .pipe(take(1))
            .subscribe({
                next: (response) => {
                    this.isLoadingSig.set(false);
                    this.#notificationService.success(
                        response.message ||
                            'admin.common.messages.operationSuccess'
                    );
                    this.#dialogRef.close({
                        updated: true,
                        supportedLanguages,
                        tenant: response.data,
                    });
                },
                error: (error) => {
                    this.isLoadingSig.set(false);
                    this.#notificationService.alert(
                        error.error?.message ||
                            'admin.common.errors.unexpected'
                    );
                },
            });
    }

    onCancel(): void {
        this.#dialogRef.close();
    }
}
