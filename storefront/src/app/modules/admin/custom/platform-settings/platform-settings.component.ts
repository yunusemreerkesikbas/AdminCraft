import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { FormUtils } from '@shared/utils/form.utils';
import { Subject, takeUntil } from 'rxjs';
import { PlatformSettingsService } from './platform-settings.service';
import { PatchPlatformSettingsRequest, PlatformSettingsResponse } from './platform-settings.types';

@Component({
    selector: 'spa-platform-settings',
    templateUrl: './platform-settings.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatButtonModule,
        MatFormFieldModule,
        MatIconModule,
        MatSelectModule,
        TranslocoModule,
        SpaInputComponent,
    ],
})
export class SpaPlatformSettingsComponent implements OnInit, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #service = inject(PlatformSettingsService);
    readonly #notify = inject(NotificationService);
    readonly #destroy$ = new Subject<void>();

    readonly loadingSig = signal<boolean>(true);
    readonly savingSig = signal<boolean>(false);

    form: FormGroup = this.#fb.group({
        platformName: ['', [Validators.required, Validators.maxLength(100)]],
        defaultLanguage: ['', Validators.required],
        defaultCurrency: ['', Validators.required],
        emailFromAddress: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
        emailFromName: ['', [Validators.required, Validators.maxLength(100)]],
    });

    readonly languages = [
        { value: 'TR', label: 'Turkish' },
        { value: 'EN', label: 'English' },
    ];

    readonly currencies = [
        { value: 'TRY', label: 'TRY - Turkish Lira' },
        { value: 'USD', label: 'USD - US Dollar' },
        { value: 'EUR', label: 'EUR - Euro' },
        { value: 'GBP', label: 'GBP - British Pound' },
    ];

    ngOnInit(): void {
        this.#loadSettings();
    }

    onSave(): void {
        if (this.form.invalid || this.form.pristine) return;

        const payload = FormUtils.getDirtyValues<PatchPlatformSettingsRequest>(this.form);

        this.savingSig.set(true);
        this.#service
            .patchSettings(payload)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.#populateForm(data);
                    this.savingSig.set(false);
                    this.#notify.success('admin.platform.settings.messages.saveSuccess');
                },
                error: (err) => {
                    console.error('[PlatformSettings] Save failed:', err);
                    this.savingSig.set(false);
                    this.#notify.alert('admin.platform.settings.messages.saveFailed');
                },
            });
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    #loadSettings(): void {
        this.loadingSig.set(true);
        this.#service
            .getSettings()
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.#populateForm(data);
                    this.loadingSig.set(false);
                },
                error: (err) => {
                    console.error('[PlatformSettings] Load failed:', err);
                    this.loadingSig.set(false);
                    this.#notify.alert('admin.platform.settings.messages.loadFailed');
                },
            });
    }

    #populateForm(data: PlatformSettingsResponse): void {
        this.form.patchValue({
            platformName: data.platformName,
            defaultLanguage: data.defaultLanguage,
            defaultCurrency: data.defaultCurrency,
            emailFromAddress: data.emailFromAddress,
            emailFromName: data.emailFromName,
        });
        this.form.markAsPristine();
    }
}
